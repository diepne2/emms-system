package com.emms.backend.service.analytics;

import com.emms.backend.dto.analystic.asset.*;
import com.emms.backend.entity.Asset;
import com.emms.backend.entity.AssetDowntime;
import com.emms.backend.repository.AssetDowntimeRepository;
import com.emms.backend.repository.AssetRepository;
import com.emms.backend.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AssetAnalysisService {

    private static final Long ASSET_NOT_FOUND_ID = -1L;

    private final AssetRepository assetRepository;
    private final AssetDowntimeRepository assetDowntimeRepository;
    private final WorkOrderRepository workOrderRepository;

    public AssetAnalysisService(
            AssetRepository assetRepository,
            AssetDowntimeRepository assetDowntimeRepository,
            WorkOrderRepository workOrderRepository
    ) {
        this.assetRepository = assetRepository;
        this.assetDowntimeRepository = assetDowntimeRepository;
        this.workOrderRepository = workOrderRepository;
    }

    public AssetOverview getAssetOverview(LocalDate fromDate, LocalDate toDate, String assetName) {
        validateDateRange(fromDate, toDate);

        Long assetId = resolveAssetId(assetName);
        if (isAssetNotFound(assetId)) {
            return new AssetOverview();
        }

        List<AssetDowntime> downtimes = findDowntimes(fromDate, toDate, assetId);

        long totalDowntimeSeconds = downtimes.stream()
                .mapToLong(this::getDowntimeDurationSeconds)
                .sum();

        long periodSeconds = getPeriodSeconds(fromDate, toDate);
        long totalUptimeSeconds = Math.max(periodSeconds - totalDowntimeSeconds, 0L);

        double mttrSeconds = calculateMttrSeconds(downtimes);
        double mtbfSeconds = calculateMtbfSeconds(periodSeconds, totalDowntimeSeconds, downtimes.size());

        BigDecimal totalCost = workOrderRepository.sumActualCostByAssetAndDateRange(
                assetId,
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay()
        );

        AssetOverview dto = new AssetOverview();
        dto.setMtbf(round(mtbfSeconds));
        dto.setMttr(round(mttrSeconds));
        dto.setTotalDowntime(totalDowntimeSeconds);
        dto.setTotalUptime(totalUptimeSeconds);
        dto.setTotalCost(totalCost == null ? BigDecimal.ZERO : totalCost);

        return dto;
    }

    public AssetStats getAssetStats(LocalDate fromDate, LocalDate toDate, String assetName) {
        validateDateRange(fromDate, toDate);

        Long assetId = resolveAssetId(assetName);
        if (isAssetNotFound(assetId)) {
            AssetStats dto = new AssetStats();
            dto.setTotalDowntime(0L);
            dto.setAvailability(0.0);
            dto.setDowntimeEvents(0);
            return dto;
        }

        List<AssetDowntime> downtimes = findDowntimes(fromDate, toDate, assetId);

        long totalDowntimeSeconds = downtimes.stream()
                .mapToLong(this::getDowntimeDurationSeconds)
                .sum();

        long periodSeconds = getPeriodSeconds(fromDate, toDate);
        long uptimeSeconds = Math.max(periodSeconds - totalDowntimeSeconds, 0L);

        double availability = periodSeconds <= 0
                ? 100.0
                : ((double) uptimeSeconds / periodSeconds) * 100.0;

        AssetStats dto = new AssetStats();
        dto.setTotalDowntime(totalDowntimeSeconds);
        dto.setAvailability(round(availability));
        dto.setDowntimeEvents(downtimes.size());

        return dto;
    }

    public List<DowntimesByAsset> getDowntimesByAsset(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<Asset> assets = assetRepository.findAll();
        List<AssetDowntime> downtimes = findDowntimes(fromDate, toDate, null);

        Map<Long, Long> countByAssetId = new HashMap<>();

        for (AssetDowntime downtime : downtimes) {
            if (downtime.getAsset() == null || downtime.getAsset().getId() == null) {
                continue;
            }

            Long currentAssetId = downtime.getAsset().getId();
            countByAssetId.put(currentAssetId, countByAssetId.getOrDefault(currentAssetId, 0L) + 1);
        }

        long totalEvents = downtimes.size();
        List<DowntimesByAsset> result = new ArrayList<>();

        for (Asset asset : assets) {
            if (asset == null || asset.getId() == null) {
                continue;
            }

            long count = countByAssetId.getOrDefault(asset.getId(), 0L);
            double percentage = totalEvents == 0 ? 0.0 : ((double) count / totalEvents) * 100.0;

            DowntimesByAsset dto = new DowntimesByAsset();
            dto.setId(asset.getId());
            dto.setName(asset.getName());
            dto.setDowntimeCount((int) count);
            dto.setDowntimePercentage(round(percentage));

            result.add(dto);
        }

        result.sort(
                Comparator.comparing(
                        DowntimesByAsset::getDowntimeCount,
                        Comparator.nullsLast(Integer::compareTo)
                ).reversed()
        );

        return result;
    }

    public List<DowntimesByDate> getDowntimesByDate(LocalDate fromDate, LocalDate toDate, String assetName) {
        validateDateRange(fromDate, toDate);

        Long assetId = resolveAssetId(assetName);
        if (isAssetNotFound(assetId)) {
            return emptyDowntimesByDate(fromDate, toDate);
        }

        List<AssetDowntime> downtimes = findDowntimes(fromDate, toDate, assetId);
        Map<LocalDate, Long> downtimeByDate = initLongDateMap(fromDate, toDate);

        for (AssetDowntime downtime : downtimes) {
            Map<LocalDate, Long> split = splitDowntimeByDate(downtime);

            for (Map.Entry<LocalDate, Long> entry : split.entrySet()) {
                LocalDate date = entry.getKey();
                Long seconds = entry.getValue();

                if (downtimeByDate.containsKey(date)) {
                    downtimeByDate.put(date, downtimeByDate.get(date) + seconds);
                }
            }
        }

        Map<LocalDate, BigDecimal> costByDate = workOrderRepository.sumActualCostGroupByDateAndAsset(
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay(),
                assetId
        );

        if (costByDate == null) {
            costByDate = Map.of();
        }

        List<DowntimesByDate> result = new ArrayList<>();

        for (Map.Entry<LocalDate, Long> entry : downtimeByDate.entrySet()) {
            DowntimesByDate dto = new DowntimesByDate();
            dto.setDate(entry.getKey());
            dto.setTotalDowntime(entry.getValue());
            dto.setTotalWorkOrderCost(costByDate.getOrDefault(entry.getKey(), BigDecimal.ZERO));
            result.add(dto);
        }

        return result;
    }

    public List<DowntimesMeantimeByDate> getDowntimesMeantimeByDate(
            LocalDate fromDate,
            LocalDate toDate,
            String assetName
    ) {
        validateDateRange(fromDate, toDate);

        Long assetId = resolveAssetId(assetName);
        if (isAssetNotFound(assetId)) {
            return emptyDowntimesMeantimeByDate(fromDate, toDate);
        }

        List<AssetDowntime> downtimes = findDowntimes(fromDate, toDate, assetId);
        Map<LocalDate, List<Long>> grouped = initListDateMap(fromDate, toDate);

        for (AssetDowntime downtime : downtimes) {
            Map<LocalDate, Long> split = splitDowntimeByDate(downtime);

            for (Map.Entry<LocalDate, Long> entry : split.entrySet()) {
                LocalDate date = entry.getKey();
                Long seconds = entry.getValue();

                if (grouped.containsKey(date)) {
                    grouped.get(date).add(seconds);
                }
            }
        }

        List<DowntimesMeantimeByDate> result = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Long>> entry : grouped.entrySet()) {
            List<Long> values = entry.getValue();

            double avgHours = values.isEmpty()
                    ? 0.0
                    : values.stream().mapToLong(Long::longValue).average().orElse(0.0) / 3600.0;

            DowntimesMeantimeByDate dto = new DowntimesMeantimeByDate();
            dto.setDate(entry.getKey());
            dto.setAverageDowntimeHours(round(avgHours));
            result.add(dto);
        }

        return result;
    }

    public Meantimes getMeantimes(LocalDate fromDate, LocalDate toDate, String assetName) {
        validateDateRange(fromDate, toDate);

        Long assetId = resolveAssetId(assetName);
        if (isAssetNotFound(assetId)) {
            Meantimes dto = new Meantimes();
            dto.setMtbfHours(0.0);
            dto.setMaintenanceIntervalHours(0.0);
            return dto;
        }

        List<AssetDowntime> downtimes = findDowntimes(fromDate, toDate, assetId);

        long totalDowntimeSeconds = downtimes.stream()
                .mapToLong(this::getDowntimeDurationSeconds)
                .sum();

        long periodSeconds = getPeriodSeconds(fromDate, toDate);
        double mtbfSeconds = calculateMtbfSeconds(periodSeconds, totalDowntimeSeconds, downtimes.size());

        Double maintenanceIntervalHours = workOrderRepository.calculateAverageMaintenanceIntervalHours(
                assetId,
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay()
        );

        Meantimes dto = new Meantimes();
        dto.setMtbfHours(round(mtbfSeconds / 3600.0));
        dto.setMaintenanceIntervalHours(round(maintenanceIntervalHours == null ? 0.0 : maintenanceIntervalHours));

        return dto;
    }

    public List<MTBFByAsset> getMtbfByAsset(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<Asset> assets = assetRepository.findAll();
        List<MTBFByAsset> result = new ArrayList<>();

        long periodSeconds = getPeriodSeconds(fromDate, toDate);

        for (Asset asset : assets) {
            if (asset == null || asset.getId() == null) {
                continue;
            }

            List<AssetDowntime> downtimes = findDowntimes(fromDate, toDate, asset.getId());

            long totalDowntimeSeconds = downtimes.stream()
                    .mapToLong(this::getDowntimeDurationSeconds)
                    .sum();

            double mtbfSeconds = calculateMtbfSeconds(
                    periodSeconds,
                    totalDowntimeSeconds,
                    downtimes.size()
            );

            MTBFByAsset dto = new MTBFByAsset();
            dto.setId(asset.getId());
            dto.setName(asset.getName());
            dto.setMtbfHours(round(mtbfSeconds / 3600.0));

            result.add(dto);
        }

        result.sort(
                Comparator.comparing(
                        MTBFByAsset::getMtbfHours,
                        Comparator.nullsLast(Double::compareTo)
                ).reversed()
        );

        return result;
    }

    private Long resolveAssetId(String assetName) {
        if (assetName == null || assetName.trim().isEmpty()) {
            return null;
        }

        String keyword = assetName.trim();

        Optional<Asset> exactAsset = assetRepository.findByNameIgnoreCase(keyword);
        if (exactAsset.isPresent()) {
            return exactAsset.get().getId();
        }

        List<Asset> matchedAssets = assetRepository.findByNameContainingIgnoreCase(keyword);
        if (matchedAssets == null || matchedAssets.isEmpty()) {
            return ASSET_NOT_FOUND_ID;
        }

        return matchedAssets.get(0).getId();
    }

    private boolean isAssetNotFound(Long assetId) {
        return ASSET_NOT_FOUND_ID.equals(assetId);
    }

    private List<AssetDowntime> findDowntimes(LocalDate fromDate, LocalDate toDate, Long assetId) {
        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.plusDays(1).atStartOfDay();

        List<AssetDowntime> result;

        if (assetId != null) {
            result = assetDowntimeRepository.findByAssetIdAndDateRange(assetId, start, end);
        } else {
            result = assetDowntimeRepository.findByDateRange(start, end);
        }

        return result == null ? List.of() : result;
    }

    private List<DowntimesByDate> emptyDowntimesByDate(LocalDate fromDate, LocalDate toDate) {
        Map<LocalDate, Long> map = initLongDateMap(fromDate, toDate);
        List<DowntimesByDate> result = new ArrayList<>();

        for (LocalDate date : map.keySet()) {
            DowntimesByDate dto = new DowntimesByDate();
            dto.setDate(date);
            dto.setTotalDowntime(0L);
            dto.setTotalWorkOrderCost(BigDecimal.ZERO);
            result.add(dto);
        }

        return result;
    }

    private List<DowntimesMeantimeByDate> emptyDowntimesMeantimeByDate(LocalDate fromDate, LocalDate toDate) {
        Map<LocalDate, List<Long>> map = initListDateMap(fromDate, toDate);
        List<DowntimesMeantimeByDate> result = new ArrayList<>();

        for (LocalDate date : map.keySet()) {
            DowntimesMeantimeByDate dto = new DowntimesMeantimeByDate();
            dto.setDate(date);
            dto.setAverageDowntimeHours(0.0);
            result.add(dto);
        }

        return result;
    }

    private long getDowntimeDurationSeconds(AssetDowntime downtime) {
        if (downtime == null || downtime.getStartsOn() == null) {
            return 0L;
        }

        LocalDateTime start = downtime.getStartsOn();
        LocalDateTime end = downtime.getEndsOn() != null
                ? downtime.getEndsOn()
                : LocalDateTime.now();

        if (end.isBefore(start)) {
            return 0L;
        }

        return Duration.between(start, end).getSeconds();
    }

    private Map<LocalDate, Long> splitDowntimeByDate(AssetDowntime downtime) {
        Map<LocalDate, Long> result = new HashMap<>();

        if (downtime == null || downtime.getStartsOn() == null) {
            return result;
        }

        LocalDateTime start = downtime.getStartsOn();
        LocalDateTime end = downtime.getEndsOn() != null
                ? downtime.getEndsOn()
                : LocalDateTime.now();

        if (end.isBefore(start)) {
            return result;
        }

        LocalDateTime cursor = start;

        while (!cursor.toLocalDate().isAfter(end.toLocalDate())) {
            LocalDate currentDate = cursor.toLocalDate();
            LocalDateTime dayEnd = currentDate.plusDays(1).atStartOfDay();
            LocalDateTime effectiveEnd = end.isBefore(dayEnd) ? end : dayEnd;

            long seconds = Duration.between(cursor, effectiveEnd).getSeconds();

            if (seconds > 0) {
                result.put(currentDate, result.getOrDefault(currentDate, 0L) + seconds);
            }

            cursor = dayEnd;
        }

        return result;
    }

    private double calculateMttrSeconds(List<AssetDowntime> downtimes) {
        if (downtimes == null || downtimes.isEmpty()) {
            return 0.0;
        }

        long totalDowntimeSeconds = downtimes.stream()
                .mapToLong(this::getDowntimeDurationSeconds)
                .sum();

        return (double) totalDowntimeSeconds / downtimes.size();
    }

    private double calculateMtbfSeconds(long periodSeconds, long totalDowntimeSeconds, int failures) {
        if (failures <= 0 || periodSeconds <= 0) {
            return 0.0;
        }

        long uptimeSeconds = Math.max(periodSeconds - totalDowntimeSeconds, 0L);
        return (double) uptimeSeconds / failures;
    }

    private long getPeriodSeconds(LocalDate fromDate, LocalDate toDate) {
        return Duration.between(
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay()
        ).getSeconds();
    }

    private Map<LocalDate, Long> initLongDateMap(LocalDate fromDate, LocalDate toDate) {
        Map<LocalDate, Long> map = new LinkedHashMap<>();

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            map.put(date, 0L);
        }

        return map;
    }

    private Map<LocalDate, List<Long>> initListDateMap(LocalDate fromDate, LocalDate toDate) {
        Map<LocalDate, List<Long>> map = new LinkedHashMap<>();

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            map.put(date, new ArrayList<>());
        }

        return map;
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("fromDate and toDate must not be null");
        }

        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("toDate must be greater than or equal to fromDate");
        }
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public List<AssetOption> getAssetOptions() {
        return assetRepository.findAll()
            .stream()
            .map(asset -> new AssetOption(
                    asset.getId(),
                    asset.getName(),
                    asset.getStatus() == null ? null : asset.getStatus().name()
            ))
            .toList();
    }
}