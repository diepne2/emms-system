package com.emms.backend.service.dashboard;

import com.emms.backend.dto.dashboard.user.UserWOStats;
import com.emms.backend.dto.dashboard.user.WOStatsByDay;
import com.emms.backend.entity.WorkOrder;
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

    // =========================
    // 1. STATS THEO USER
    // =========================
    public UserWOStats getUserStats(Long userId, LocalDate fromDate, LocalDate toDate) {

        List<WorkOrder> workOrders = workOrderRepository.findByAssignedTo_UserIdAndCreatedAtBetween(
                userId,
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay()
        );

        int createdCount = workOrders.size();
        int completedCount = 0;

        for (WorkOrder wo : workOrders) {
            if (wo.getStatus() != null &&
                    "HOAN_THANH".equalsIgnoreCase(wo.getStatus().name())) {
                completedCount++;
            }
        }

        double completionRate = createdCount == 0
                ? 0.0
                : ((double) completedCount / createdCount) * 100;

        return new UserWOStats(createdCount, completedCount, round(completionRate));
    }

    // =========================
    // 2. STATS THEO NGÀY
    // =========================
    public List<WOStatsByDay> getWOStatsByDay(Long userId, LocalDate fromDate, LocalDate toDate) {

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

            // created
            if (wo.getCreatedAt() != null) {
                LocalDate createdDate = wo.getCreatedAt().toLocalDate();
                if (createdMap.containsKey(createdDate)) {
                    createdMap.put(createdDate, createdMap.get(createdDate) + 1);
                }
            }

            // completed
            if (wo.getUpdatedAt() != null &&
                    wo.getStatus() != null &&
                    "HOAN_THANH".equalsIgnoreCase(wo.getStatus().name())) {

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

    // =========================
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}