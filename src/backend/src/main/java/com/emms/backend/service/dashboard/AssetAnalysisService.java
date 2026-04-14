package com.emms.backend.service.dashboard;

import com.emms.backend.dto.dashboard.asset.AssetOverview;
import com.emms.backend.dto.dashboard.asset.AssetStats;
import com.emms.backend.dto.dashboard.asset.DowntimesByAsset;
import com.emms.backend.dto.dashboard.asset.DowntimesByDate;
import com.emms.backend.dto.dashboard.asset.DowntimesMeantimeByDate;
import com.emms.backend.dto.dashboard.asset.Meantimes;
import com.emms.backend.dto.dashboard.asset.MTBFByAsset;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AssetAnalysisService {

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

    public AssetOverview getAssetOverview(LocalDate fromDate, LocalDate toDate, Long assetId) {
        validateDateRange(fromDate, toDate);

        List<AssetDowntime> downtimes = findDowntimes(fromDate, toDate, assetId);

        long totalDowntime = downtimes.stream()
                .mapToLong(this::getDowntimeDurationSeconds)
                .sum();

        long totalUptime = calculateTotalUptimeSeconds(fromDate, toDate, totalDowntime);

        double mttr = calculateMttrSeconds(downtimes);
        double mtbf = calculateMtbfSeconds(fromDate, toDate, downtimes, totalDowntime);

        BigDecimal totalCost = workOrderRepository.sumActualCostByAssetAndDateRange(
                assetId,
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay()
        );
        if (totalCost == null) {
            totalCost = BigDecimal.ZERO;
        }

        AssetOverview overview = new AssetOverview();
        overview.setMtbf(round(mtbf));
        overview.setMttr(round(mttr));
        overview.setTotalDowntime(totalDowntime);
        overview.setTotalUptime(totalUptime);
        overview.setTotalCost(totalCost);

        return overview;
    }

    public AssetStats getAssetStats(LocalDate fromDate, LocalDate toDate, Long assetId) {
        validateDateRange(fromDate, toDate);

        List<AssetDowntime> downtimes = findDowntimes(fromDate, toDate, assetId);

        long totalDowntime = downtimes.stream()
                .mapToLong(this::getDowntimeDurationSeconds)
                .sum();

        long periodSeconds = getPeriodSeconds(fromDate, toDate);
        double availability = periodSeconds <= 0
                ? 100.0
                : ((double) (periodSeconds - totalDowntime) / (double) periodSeconds) * 100.0;

        AssetStats stats = new AssetStats();
        stats.setTotalDowntime(totalDowntime);
        stats.setAvailability(round(availability));
        stats.setDowntimeEvents(downtimes.size());

        return stats;
    }

    public List<DowntimesByAsset> getDowntimesByAsset(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<Asset> assets = assetRepository.findAll();
        List<AssetDowntime> allDowntimes = findDowntimes(fromDate, toDate, null);
        int totalEvents = allDowntimes.size();

        Map<Long, Integer> countByAssetId = new LinkedHashMap<>();
        for (AssetDowntime downtime : allDowntimes) {
            Asset asset = downtime.getAsset();
            if (asset == null || asset.getId() == null) {
                continue;
            }
            countByAssetId.put(
                    asset.getId(),
                    countByAssetId.getOrDefault(asset.getId(), 0) + 1
            );
        }

        List<DowntimesByAsset> result = new ArrayList<>();
        for (Asset asset : assets) {
            int count = countByAssetId.getOrDefault(asset.getId(), 0);
            double percentage = totalEvents == 0 ? 0.0 : ((double) count / (double) totalEvents) * 100.0;

            DowntimesByAsset dto = new DowntimesByAsset();
            dto.setId(asset.getId());
            dto.setName(asset.getName());
            dto.setDowntimeCount(count);
            dto.setDowntimePercentage(round(percentage));
            result.add(dto);
        }

        result.sort(
                Comparator.comparing(
                        DowntimesByAsset::getDowntimeCount,
                        Comparator.nullsFirst(Integer::compareTo)
                ).reversed()
        );
        return result;
    }

    public List<DowntimesByDate> getDowntimesByDate(LocalDate fromDate, LocalDate toDate, Long assetId) {
        validateDateRange(fromDate, toDate);

        List<AssetDowntime> downtimes = findDowntimes(fromDate, toDate, assetId);

        Map<LocalDate, Long> downtimeByDate = new LinkedHashMap<>();
        for (LocalDate date : enumerateDates(fromDate, toDate)) {
            downtimeByDate.put(date, 0L);
        }

        for (AssetDowntime downtime : downtimes) {
            LocalDate date = getDowntimeDate(downtime);
            if (date == null) {
                continue;
            }
            downtimeByDate.put(
                    date,
                    downtimeByDate.getOrDefault(date, 0L) + getDowntimeDurationSeconds(downtime)
            );
        }

        Map<LocalDate, BigDecimal> costByDate = workOrderRepository.sumActualCostGroupByDateAndAsset(
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay(),
                assetId
        );

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

    public List<DowntimesMeantimeByDate> getDowntimesMeantimeByDate(LocalDate fromDate, LocalDate toDate, Long assetId) {
        validateDateRange(fromDate, toDate);

        List<AssetDowntime> downtimes = findDowntimes(fromDate, toDate, assetId);

        Map<LocalDate, List<Long>> grouped = new LinkedHashMap<>();
        for (LocalDate date : enumerateDates(fromDate, toDate)) {
            grouped.put(date, new ArrayList<>());
        }

        for (AssetDowntime downtime : downtimes) {
            LocalDate date = getDowntimeDate(downtime);
            if (date == null) {
                continue;
            }
            grouped.computeIfAbsent(date, k -> new ArrayList<>())
                    .add(getDowntimeDurationSeconds(downtime));
        }

        List<DowntimesMeantimeByDate> result = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Long>> entry : grouped.entrySet()) {
            List<Long> values = entry.getValue();

            double avgHours = 0.0;
            if (!values.isEmpty()) {
                long sum = values.stream().mapToLong(Long::longValue).sum();
                avgHours = (sum / (double) values.size()) / 3600.0;
            }

            DowntimesMeantimeByDate dto = new DowntimesMeantimeByDate();
            dto.setDate(entry.getKey());
            dto.setAverageDowntimeHours(round(avgHours));
            result.add(dto);
        }

        return result;
    }

    public Meantimes getMeantimes(LocalDate fromDate, LocalDate toDate, Long assetId) {
        validateDateRange(fromDate, toDate);

        List<AssetDowntime> downtimes = findDowntimes(fromDate, toDate, assetId);

        long totalDowntime = downtimes.stream()
                .mapToLong(this::getDowntimeDurationSeconds)
                .sum();

        double mtbfSeconds = calculateMtbfSeconds(fromDate, toDate, downtimes, totalDowntime);

        Double maintenanceIntervalHours = workOrderRepository.calculateAverageMaintenanceIntervalHours(
                assetId,
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay()
        );

        if (maintenanceIntervalHours == null) {
            maintenanceIntervalHours = 0.0;
        }

        Meantimes dto = new Meantimes();
        dto.setMtbfHours(round(mtbfSeconds / 3600.0));
        dto.setMaintenanceIntervalHours(round(maintenanceIntervalHours));

        return dto;
    }

    public List<MTBFByAsset> getMtbfByAsset(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<Asset> assets = assetRepository.findAll();
        List<MTBFByAsset> result = new ArrayList<>();

        for (Asset asset : assets) {
            List<AssetDowntime> downtimes = findDowntimes(fromDate, toDate, asset.getId());
            long totalDowntime = downtimes.stream()
                    .mapToLong(this::getDowntimeDurationSeconds)
                    .sum();

            double mtbfSeconds = calculateMtbfSeconds(fromDate, toDate, downtimes, totalDowntime);

            MTBFByAsset dto = new MTBFByAsset();
            dto.setId(asset.getId());
            dto.setName(asset.getName());
            dto.setMtbfHours(round(mtbfSeconds / 3600.0));
            result.add(dto);
        }

        result.sort(
                Comparator.comparing(
                        MTBFByAsset::getMtbfHours,
                        Comparator.nullsFirst(Double::compareTo)
                ).reversed()
        );
        return result;
    }

    private List<AssetDowntime> findDowntimes(LocalDate fromDate, LocalDate toDate, Long assetId) {
        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.plusDays(1).atStartOfDay();

        if (assetId != null) {
            return assetDowntimeRepository.findByAssetIdAndDateRange(assetId, start, end);
        }
        return assetDowntimeRepository.findByDateRange(start, end);
    }

    private long calculateTotalUptimeSeconds(LocalDate fromDate, LocalDate toDate, long totalDowntime) {
        long periodSeconds = getPeriodSeconds(fromDate, toDate);
        return Math.max(periodSeconds - totalDowntime, 0L);
    }

    private double calculateMttrSeconds(List<AssetDowntime> downtimes) {
        if (downtimes == null || downtimes.isEmpty()) {
            return 0.0;
        }

        long totalDowntime = downtimes.stream()
                .mapToLong(this::getDowntimeDurationSeconds)
                .sum();

        return (double) totalDowntime / downtimes.size();
    }

    private double calculateMtbfSeconds(LocalDate fromDate, LocalDate toDate, List<AssetDowntime> downtimes, long totalDowntime) {
        int failures = downtimes == null ? 0 : downtimes.size();
        if (failures == 0) {
            return 0.0;
        }

        long periodSeconds = getPeriodSeconds(fromDate, toDate);
        long uptime = Math.max(periodSeconds - totalDowntime, 0L);

        return (double) uptime / failures;
    }

    private long getPeriodSeconds(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.plusDays(1).atStartOfDay();
        return Duration.between(start, end).getSeconds();
    }

    private List<LocalDate> enumerateDates(LocalDate fromDate, LocalDate toDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = fromDate;
        while (!current.isAfter(toDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        return dates;
    }

    private LocalDate getDowntimeDate(AssetDowntime downtime) {
        if (downtime == null) {
            return null;
        }

        if (downtime.getStartsOn() != null) {
            return downtime.getStartsOn().toLocalDate();
        }

        if (downtime.getCreatedAt() != null) {
            return downtime.getCreatedAt().toLocalDate();
        }

        return null;
    }

    private long getDowntimeDurationSeconds(AssetDowntime downtime) {
        if (downtime == null) {
            return 0L;
        }

        if (downtime.getDurationSeconds() != null && downtime.getDurationSeconds() > 0) {
            return downtime.getDurationSeconds();
        }

        if (downtime.getStartsOn() != null && downtime.getEndsOn() != null) {
            return Duration.between(downtime.getStartsOn(), downtime.getEndsOn()).getSeconds();
        }

        return 0L;
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
}