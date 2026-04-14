package com.emms.backend.service.dashboard;

import com.emms.backend.dto.dashboard.request.CountByCategory;
import com.emms.backend.dto.dashboard.request.RequestStats;
import com.emms.backend.dto.dashboard.request.RequestStatsByPriority;
import com.emms.backend.dto.dashboard.request.RequestsByMonth;
import com.emms.backend.dto.dashboard.request.RequestsResolvedByDate;
import com.emms.backend.entity.Request;
import com.emms.backend.entity.RequestPortal;
import com.emms.backend.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            String status = getStatusName(request);

            if ("APPROVED".equals(status) || "ACCEPTED".equals(status) || "RESOLVED".equals(status)) {
                approvedCount++;
            } else if ("PENDING".equals(status) || "OPEN".equals(status) || "WAITING".equals(status)) {
                pendingCount++;
            } else if ("CANCELLED".equals(status) || "REJECTED".equals(status)) {
                cancelledCount++;
            }

            if (request.getCreatedAt() != null
                    && request.getUpdatedAt() != null
                    && ("APPROVED".equals(status) || "ACCEPTED".equals(status) || "RESOLVED".equals(status))) {

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

    /**
     * Giữ tên method + DTO cũ để không ảnh hưởng API hiện tại,
     * nhưng dữ liệu thực tế được group theo RequestPortal.
     */
    public List<CountByCategory> getCountByCategory(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<Request> requests = requestRepository.findByCreatedAtBetween(
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay()
        );

        Map<Long, CountByCategory> countMap = new LinkedHashMap<>();

        for (Request request : requests) {
            RequestPortal portal = request.getRequestPortal();
            if (portal == null || portal.getRequestPortalId() == null) {
                continue;
            }

            Long portalId = portal.getRequestPortalId();
            CountByCategory dto = countMap.get(portalId);

            if (dto == null) {
                dto = new CountByCategory();
                dto.setId(portalId);
                dto.setName(portal.getTitle());
                dto.setRequestCount(0);
                countMap.put(portalId, dto);
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

        Map<LocalDate, Integer> receivedMap = new LinkedHashMap<>();
        Map<LocalDate, Integer> resolvedMap = new LinkedHashMap<>();

        for (LocalDate date : enumerateDates(fromDate, toDate)) {
            receivedMap.put(date, 0);
            resolvedMap.put(date, 0);
        }

        for (Request request : requests) {
            if (request.getCreatedAt() != null) {
                LocalDate createdDate = request.getCreatedAt().toLocalDate();
                if (!createdDate.isBefore(fromDate) && !createdDate.isAfter(toDate)) {
                    receivedMap.put(createdDate, receivedMap.getOrDefault(createdDate, 0) + 1);
                }
            }

            String status = getStatusName(request);
            if (request.getUpdatedAt() != null
                    && ("APPROVED".equals(status) || "ACCEPTED".equals(status) || "RESOLVED".equals(status))) {

                LocalDate resolvedDate = request.getUpdatedAt().toLocalDate();
                if (!resolvedDate.isBefore(fromDate) && !resolvedDate.isAfter(toDate)) {
                    resolvedMap.put(resolvedDate, resolvedMap.getOrDefault(resolvedDate, 0) + 1);
                }
            }
        }

        List<RequestsResolvedByDate> result = new ArrayList<>();
        for (LocalDate date : enumerateDates(fromDate, toDate)) {
            RequestsResolvedByDate dto = new RequestsResolvedByDate();
            dto.setDate(date);
            dto.setReceivedCount(receivedMap.getOrDefault(date, 0));
            dto.setResolvedCount(resolvedMap.getOrDefault(date, 0));
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
        for (YearMonth month : enumerateMonths(fromDate, toDate)) {
            cycleTimesByMonth.put(month, new ArrayList<>());
        }

        for (Request request : requests) {
            if (request.getCreatedAt() == null) {
                continue;
            }

            YearMonth month = YearMonth.from(request.getCreatedAt());
            if (!cycleTimesByMonth.containsKey(month)) {
                continue;
            }

            if (request.getUpdatedAt() != null) {
                long days = Duration.between(request.getCreatedAt(), request.getUpdatedAt()).toDays();
                if (days >= 0) {
                    cycleTimesByMonth.get(month).add(days);
                }
            }
        }

        List<RequestsByMonth> result = new ArrayList<>();
        for (Map.Entry<YearMonth, List<Long>> entry : cycleTimesByMonth.entrySet()) {
            List<Long> values = entry.getValue();
            double avgDays = 0.0;

            if (!values.isEmpty()) {
                long sum = values.stream().mapToLong(Long::longValue).sum();
                avgDays = (double) sum / values.size();
            }

            RequestsByMonth dto = new RequestsByMonth();
            dto.setMonth(entry.getKey());
            dto.setAverageCycleTimeDays(round(avgDays));
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
            String priority = getPriorityName(request);

            if (priority == null || priority.isBlank() || "NONE".equals(priority)) {
                noneCount++;
            } else if ("LOW".equals(priority)) {
                lowCount++;
            } else if ("MEDIUM".equals(priority)) {
                mediumCount++;
            } else if ("HIGH".equals(priority) || "URGENT".equals(priority)) {
                highCount++;
            }
        }

        RequestStatsByPriority dto = new RequestStatsByPriority();
        dto.setNonePriority(new RequestStatsByPriority.BasicStats(noneCount));
        dto.setLowPriority(new RequestStatsByPriority.BasicStats(lowCount));
        dto.setMediumPriority(new RequestStatsByPriority.BasicStats(mediumCount));
        dto.setHighPriority(new RequestStatsByPriority.BasicStats(highCount));

        return dto;
    }

    private String getStatusName(Request request) {
        if (request == null || request.getStatus() == null) {
            return "";
        }
        return request.getStatus().name().toUpperCase();
    }

    private String getPriorityName(Request request) {
        if (request == null || request.getPriority() == null) {
            return "NONE";
        }
        return request.getPriority().name().toUpperCase();
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

    private List<YearMonth> enumerateMonths(LocalDate fromDate, LocalDate toDate) {
        List<YearMonth> months = new ArrayList<>();
        YearMonth current = YearMonth.from(fromDate);
        YearMonth end = YearMonth.from(toDate);

        while (!current.isAfter(end)) {
            months.add(current);
            current = current.plusMonths(1);
        }

        return months;
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