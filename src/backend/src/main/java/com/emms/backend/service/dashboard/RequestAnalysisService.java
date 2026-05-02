package com.emms.backend.service.dashboard;

import com.emms.backend.dto.dashboard.request.CountByCategory;
import com.emms.backend.dto.dashboard.request.RequestStats;
import com.emms.backend.dto.dashboard.request.RequestStatsByPriority;
import com.emms.backend.dto.dashboard.request.RequestsByMonth;
import com.emms.backend.dto.dashboard.request.RequestsResolvedByDate;
import com.emms.backend.entity.Request;
import com.emms.backend.entity.Request.Status;
import com.emms.backend.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class RequestAnalysisService {

    private final RequestRepository requestRepository;

    public RequestAnalysisService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public RequestStats getRequestStats(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<Request> requests = requestRepository.findByCreatedAtBetween(
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay()
        );

        int approvedCount = 0;
        int pendingCount = 0;
        int cancelledCount = 0;

        long totalCycleHours = 0L;
        int resolvedItems = 0;

        for (Request request : requests) {
            Status status = request.getStatus();

            if (isApproved(status)) {
                approvedCount++;
            } else if (isPending(status)) {
                pendingCount++;
            } else if (isCancelled(status) || request.isCancelled()) {
                cancelledCount++;
            }

            if (request.getCreatedAt() != null
                    && request.getUpdatedAt() != null
                    && isApproved(status)) {

                long hours = Duration.between(request.getCreatedAt(), request.getUpdatedAt()).toHours();
                if (hours >= 0) {
                    totalCycleHours += hours;
                    resolvedItems++;
                }
            }
        }

        double averageCycleTimeHours = resolvedItems == 0
                ? 0.0
                : round((double) totalCycleHours / resolvedItems);

        RequestStats dto = new RequestStats();
        dto.setApprovedCount(approvedCount);
        dto.setPendingCount(pendingCount);
        dto.setCancelledCount(cancelledCount);
        dto.setAverageCycleTimeHours(averageCycleTimeHours);

        return dto;
    }

    public List<CountByCategory> getCountByCategory(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<Request> requests = requestRepository.findByCreatedAtBetween(
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay()
        );

        Map<Long, CountByCategory> countMap = new LinkedHashMap<>();

        for (Request request : requests) {
            if (request.getLocation() == null || request.getLocation().getId() == null) {
                continue;
            }

            Long locationId = request.getLocation().getId();

            CountByCategory dto = countMap.get(locationId);
            if (dto == null) {
                dto = new CountByCategory();
                dto.setId(locationId);
                dto.setName(request.getLocation().getName());
                dto.setRequestCount(0);
                countMap.put(locationId, dto);
            }

            dto.setRequestCount(dto.getRequestCount() + 1);
        }

        List<CountByCategory> result = new ArrayList<>(countMap.values());
        result.sort(Comparator.comparing(CountByCategory::getRequestCount).reversed());
        return result;
    }

    public List<RequestsResolvedByDate> getRequestsResolvedByDate(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<Request> requests = requestRepository.findByCreatedAtBetweenOrUpdatedAtBetween(
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay(),
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay()
        );

        Map<LocalDate, Integer> receivedMap = initDateMap(fromDate, toDate);
        Map<LocalDate, Integer> resolvedMap = initDateMap(fromDate, toDate);

        for (Request request : requests) {
            if (request.getCreatedAt() != null) {
                LocalDate createdDate = request.getCreatedAt().toLocalDate();
                if (receivedMap.containsKey(createdDate)) {
                    receivedMap.put(createdDate, receivedMap.get(createdDate) + 1);
                }
            }

            if (request.getUpdatedAt() != null && isApproved(request.getStatus())) {
                LocalDate resolvedDate = request.getUpdatedAt().toLocalDate();
                if (resolvedMap.containsKey(resolvedDate)) {
                    resolvedMap.put(resolvedDate, resolvedMap.get(resolvedDate) + 1);
                }
            }
        }

        List<RequestsResolvedByDate> result = new ArrayList<>();

        for (LocalDate date : receivedMap.keySet()) {
            RequestsResolvedByDate dto = new RequestsResolvedByDate();
            dto.setDate(date);
            dto.setReceivedCount(receivedMap.get(date));
            dto.setResolvedCount(resolvedMap.get(date));
            result.add(dto);
        }

        return result;
    }

    public List<RequestsByMonth> getRequestsByMonth(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<Request> requests = requestRepository.findByCreatedAtBetween(
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay()
        );

        Map<YearMonth, List<Long>> cycleTimesByMonth = new LinkedHashMap<>();

        for (YearMonth month = YearMonth.from(fromDate);
             !month.isAfter(YearMonth.from(toDate));
             month = month.plusMonths(1)) {
            cycleTimesByMonth.put(month, new ArrayList<>());
        }

        for (Request request : requests) {
            if (request.getCreatedAt() == null || request.getUpdatedAt() == null) {
                continue;
            }

            YearMonth month = YearMonth.from(request.getCreatedAt());

            if (!cycleTimesByMonth.containsKey(month)) {
                continue;
            }

            long days = Duration.between(request.getCreatedAt(), request.getUpdatedAt()).toDays();

            if (days >= 0) {
                cycleTimesByMonth.get(month).add(days);
            }
        }

        List<RequestsByMonth> result = new ArrayList<>();

        for (Map.Entry<YearMonth, List<Long>> entry : cycleTimesByMonth.entrySet()) {
            List<Long> values = entry.getValue();

            double averageCycleTimeDays = values.isEmpty()
                    ? 0.0
                    : values.stream().mapToLong(Long::longValue).average().orElse(0.0);

            RequestsByMonth dto = new RequestsByMonth();
            dto.setMonth(entry.getKey());
            dto.setAverageCycleTimeDays(round(averageCycleTimeDays));
            result.add(dto);
        }

        return result;
    }

    public RequestStatsByPriority getRequestStatsByPriority(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<Request> requests = requestRepository.findByCreatedAtBetween(
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay()
        );

        int noneCount = 0;
        int lowCount = 0;
        int mediumCount = 0;
        int highCount = 0;

        for (Request request : requests) {
            if (request.getPriority() == null) {
                noneCount++;
                continue;
            }

            switch (request.getPriority()) {
                case LOW -> lowCount++;
                case MEDIUM -> mediumCount++;
                case HIGH, URGENT -> highCount++;
                case NONE -> noneCount++;
            }
        }

        RequestStatsByPriority dto = new RequestStatsByPriority();
        dto.setNonePriority(new RequestStatsByPriority.BasicStats(noneCount));
        dto.setLowPriority(new RequestStatsByPriority.BasicStats(lowCount));
        dto.setMediumPriority(new RequestStatsByPriority.BasicStats(mediumCount));
        dto.setHighPriority(new RequestStatsByPriority.BasicStats(highCount));

        return dto;
    }

    private boolean isApproved(Status status) {
        return status == Status.APPROVED
                || status == Status.ACCEPTED
                || status == Status.RESOLVED;
    }

    private boolean isPending(Status status) {
        return status == Status.PENDING
                || status == Status.OPEN
                || status == Status.WAITING;
    }

    private boolean isCancelled(Status status) {
        return status == Status.CANCELLED
                || status == Status.REJECTED;
    }

    private Map<LocalDate, Integer> initDateMap(LocalDate fromDate, LocalDate toDate) {
        Map<LocalDate, Integer> map = new LinkedHashMap<>();

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            map.put(date, 0);
        }

        return map;
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống.");
        }

        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu.");
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}