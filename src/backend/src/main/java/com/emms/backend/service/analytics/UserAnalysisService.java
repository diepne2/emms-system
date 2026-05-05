package com.emms.backend.service.analytics;

import com.emms.backend.dto.analystic.user.UserWOStats;
import com.emms.backend.dto.analystic.user.WOStatsByDay;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.WorkOrder.WorkOrderStatus;
import com.emms.backend.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class UserAnalysisService {

    private final WorkOrderRepository workOrderRepository;

    public UserAnalysisService(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    public UserWOStats getUserStats(Long userId, LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<WorkOrder> workOrders = workOrderRepository.findByAssignedTo_UserIdAndCreatedAtBetween(
                userId,
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay()
        );

        int createdCount = workOrders.size();
        int completedCount = 0;

        for (WorkOrder wo : workOrders) {
            if (wo.getStatus() == WorkOrderStatus.DONE) {
                completedCount++;
            }
        }

        double completionRate = createdCount == 0
                ? 0.0
                : ((double) completedCount / createdCount) * 100.0;

        return new UserWOStats(createdCount, completedCount, round(completionRate));
    }

    public List<WOStatsByDay> getWOStatsByDay(Long userId, LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<WorkOrder> workOrders = workOrderRepository.findByAssignedTo_UserIdAndCreatedAtBetween(
                userId,
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay()
        );

        Map<LocalDate, Integer> createdMap = new LinkedHashMap<>();
        Map<LocalDate, Integer> completedMap = new LinkedHashMap<>();

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            createdMap.put(date, 0);
            completedMap.put(date, 0);
        }

        for (WorkOrder wo : workOrders) {
            if (wo.getCreatedAt() != null) {
                LocalDate createdDate = wo.getCreatedAt().toLocalDate();
                if (createdMap.containsKey(createdDate)) {
                    createdMap.put(createdDate, createdMap.get(createdDate) + 1);
                }
            }

            if (wo.getUpdatedAt() != null && wo.getStatus() == WorkOrderStatus.DONE) {
                LocalDate completedDate = wo.getUpdatedAt().toLocalDate();
                if (completedMap.containsKey(completedDate)) {
                    completedMap.put(completedDate, completedMap.get(completedDate) + 1);
                }
            }
        }

        List<WOStatsByDay> result = new ArrayList<>();

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            result.add(new WOStatsByDay(
                    createdMap.get(date),
                    completedMap.get(date),
                    date
            ));
        }

        return result;
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