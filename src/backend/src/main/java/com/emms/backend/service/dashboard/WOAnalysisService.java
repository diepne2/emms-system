package com.emms.backend.service.dashboard;

import com.emms.backend.dto.dashboard.workorder.IncompleteWOByAsset;
import com.emms.backend.dto.dashboard.workorder.IncompleteWOByUser;
import com.emms.backend.dto.dashboard.workorder.WOCountByUser;
import com.emms.backend.dto.dashboard.workorder.WOCountByWeek;
import com.emms.backend.dto.dashboard.workorder.WOHours;
import com.emms.backend.dto.dashboard.workorder.WOIncompleteStats;
import com.emms.backend.dto.dashboard.workorder.WOStats;
import com.emms.backend.dto.dashboard.workorder.WOStatsByPriority;
import com.emms.backend.dto.dashboard.workorder.WOStatuses;
import com.emms.backend.dto.dashboard.workorder.WOStatusesByDate;
import com.emms.backend.dto.dashboard.workorder.WOTimeByWeek;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.WorkOrder.WorkOrderPriority;
import com.emms.backend.entity.WorkOrder.WorkOrderStatus;
import com.emms.backend.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class WOAnalysisService {

    private static final Set<WorkOrderStatus> INCOMPLETE_STATUSES = Set.of(
            WorkOrderStatus.OPEN,
            WorkOrderStatus.ON_HOLD,
            WorkOrderStatus.IN_PROGRESS
    );

    private final WorkOrderRepository workOrderRepository;

    public WOAnalysisService(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    public WOStats getStats(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = atStartOfDayOrMin(fromDate);
        LocalDateTime to = atStartOfNextDayOrMax(toDate);

        Integer totalCount = safeInt(workOrderRepository.countAllInRange(from, to));
        Integer completedCount = safeInt(
                workOrderRepository.countByStatusInRange(WorkOrderStatus.DONE, from, to)
        );

        Double averageCycleTimeHours = safeDouble(workOrderRepository.getAverageCycleTimeHours(from, to));

        // Entity hiện tại không có startedAt / maintenancePlan / sourceType
        Double mttaHours = 0.0;
        Integer compliantCount = 0;

        Double completionRate = totalCount == 0 ? 0.0 : round2((completedCount * 100.0) / totalCount);
        Double complianceRate = 0.0;

        return new WOStats(
                totalCount,
                completedCount,
                compliantCount,
                averageCycleTimeHours,
                mttaHours,
                completionRate,
                complianceRate
        );
    }

    public WOStatuses getStatuses(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = atStartOfDayOrMin(fromDate);
        LocalDateTime to = atStartOfNextDayOrMax(toDate);

        return new WOStatuses(
                safeInt(workOrderRepository.countByStatusInRange(WorkOrderStatus.OPEN, from, to)),
                0, // assignedCount - entity hiện tại không có DA_PHAN_CONG
                safeInt(workOrderRepository.countByStatusInRange(WorkOrderStatus.IN_PROGRESS, from, to)),
                safeInt(workOrderRepository.countByStatusInRange(WorkOrderStatus.ON_HOLD, from, to)),
                0, // awaitingConfirmationCount - entity hiện tại không có CHO_XAC_NHAN
                safeInt(workOrderRepository.countByStatusInRange(WorkOrderStatus.DONE, from, to)),
                0  // rejected/tu_choi - entity hiện tại không có
        );
    }

    public List<WOStatusesByDate> getStatusesByDate(LocalDate fromDate, LocalDate toDate) {
        LocalDate start = fromDate != null ? fromDate : LocalDate.now().minusDays(6);
        LocalDate end = toDate != null ? toDate : LocalDate.now();

        if (start.isAfter(end)) {
            LocalDate temp = start;
            start = end;
            end = temp;
        }

        List<WOStatusesByDate> results = new ArrayList<>();
        LocalDate cursor = start;

        while (!cursor.isAfter(end)) {
            LocalDateTime dayStart = cursor.atStartOfDay();
            LocalDateTime nextDayStart = cursor.plusDays(1).atStartOfDay();

            results.add(new WOStatusesByDate(
                    safeInt(workOrderRepository.countByStatusInRange(WorkOrderStatus.OPEN, dayStart, nextDayStart)),
                    0,
                    safeInt(workOrderRepository.countByStatusInRange(WorkOrderStatus.IN_PROGRESS, dayStart, nextDayStart)),
                    safeInt(workOrderRepository.countByStatusInRange(WorkOrderStatus.ON_HOLD, dayStart, nextDayStart)),
                    0,
                    safeInt(workOrderRepository.countByStatusInRange(WorkOrderStatus.DONE, dayStart, nextDayStart)),
                    0,
                    cursor
            ));

            cursor = cursor.plusDays(1);
        }

        return results;
    }

    public List<WOCountByWeek> getCountByWeek(LocalDate fromDate, LocalDate toDate) {
        LocalDate start = (fromDate != null ? fromDate : LocalDate.now().minusWeeks(7))
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = (toDate != null ? toDate : LocalDate.now())
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        if (start.isAfter(end)) {
            LocalDate temp = start;
            start = end;
            end = temp;
        }

        List<WOCountByWeek> results = new ArrayList<>();
        LocalDate weekStart = start;

        while (!weekStart.isAfter(end)) {
            LocalDateTime from = weekStart.atStartOfDay();
            LocalDateTime to = weekStart.plusWeeks(1).atStartOfDay();

            Integer totalCount = safeInt(workOrderRepository.countAllInRange(from, to));

            // Entity hiện tại không có compliant/reactive marker
            Integer compliantCount = 0;
            Integer reactiveCount = totalCount;

            results.add(new WOCountByWeek(totalCount, compliantCount, reactiveCount, weekStart));
            weekStart = weekStart.plusWeeks(1);
        }

        return results;
    }

    public List<WOTimeByWeek> getTimeByWeek(LocalDate fromDate, LocalDate toDate) {
        LocalDate start = (fromDate != null ? fromDate : LocalDate.now().minusWeeks(7))
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = (toDate != null ? toDate : LocalDate.now())
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        if (start.isAfter(end)) {
            LocalDate temp = start;
            start = end;
            end = temp;
        }

        List<WOTimeByWeek> results = new ArrayList<>();
        LocalDate weekStart = start;

        while (!weekStart.isAfter(end)) {
            LocalDateTime from = weekStart.atStartOfDay();
            LocalDateTime to = weekStart.plusWeeks(1).atStartOfDay();

            Double totalHours = safeDouble(workOrderRepository.sumEstimatedHoursInRange(from, to));
            Double reactiveHours = totalHours;

            results.add(new WOTimeByWeek(round2(totalHours), round2(reactiveHours), weekStart));
            weekStart = weekStart.plusWeeks(1);
        }

        return results;
    }

    public List<WOCountByUser> getCountByUser(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = atStartOfDayOrMin(fromDate);
        LocalDateTime to = atStartOfNextDayOrMax(toDate);

        List<Object[]> rows = workOrderRepository.countByUserInRange(from, to);
        List<WOCountByUser> results = new ArrayList<>();

        for (Object[] row : rows) {
            Long userId = row[0] != null ? ((Number) row[0]).longValue() : null;
            String username = row[1] != null ? row[1].toString() : null;
            String fullName = row[2] != null ? row[2].toString() : null;
            Integer totalCount = row[3] != null ? ((Number) row[3]).intValue() : 0;

            WOCountByUser dto = new WOCountByUser();
            dto.setId(userId);
            dto.setUsername(username);
            dto.setFullName(fullName);
            dto.setTotalCount(totalCount);
            results.add(dto);
        }

        return results;
    }

    public WOHours getHours(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = atStartOfDayOrMin(fromDate);
        LocalDateTime to = atStartOfNextDayOrMax(toDate);

        Double estimatedHours = safeDouble(workOrderRepository.sumEstimatedHoursInRange(from, to));

        // Entity hiện tại không có actualDuration
        Double actualHours = 0.0;

        return new WOHours(round2(estimatedHours), round2(actualHours));
    }

    public WOIncompleteStats getIncompleteStats(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = atStartOfDayOrMin(fromDate);
        LocalDateTime to = atStartOfNextDayOrMax(toDate);

        Integer totalIncompleteCount = safeInt(
                workOrderRepository.countByStatusesInRange(INCOMPLETE_STATUSES, from, to)
        );
        Double averageAgeDays = safeDouble(
                workOrderRepository.averageAgeByStatusesInRange(INCOMPLETE_STATUSES, from, to)
        );

        return new WOIncompleteStats(totalIncompleteCount, round2(averageAgeDays));
    }

    public List<IncompleteWOByUser> getIncompleteByUser(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = atStartOfDayOrMin(fromDate);
        LocalDateTime to = atStartOfNextDayOrMax(toDate);

        List<Object[]> rows = workOrderRepository.incompleteByUserInRange(INCOMPLETE_STATUSES, from, to);
        List<IncompleteWOByUser> results = new ArrayList<>();

        for (Object[] row : rows) {
            Long userId = row[0] != null ? ((Number) row[0]).longValue() : null;
            String username = row[1] != null ? row[1].toString() : null;
            String fullName = row[2] != null ? row[2].toString() : null;
            Integer incompleteCount = row[3] != null ? ((Number) row[3]).intValue() : 0;
            Double averageAgeDays = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;

            IncompleteWOByUser dto = new IncompleteWOByUser();
            dto.setId(userId);
            dto.setUsername(username);
            dto.setFullName(fullName);
            dto.setIncompleteCount(incompleteCount);
            dto.setAverageAgeDays(round2(averageAgeDays));

            results.add(dto);
        }

        return results;
    }

    public List<IncompleteWOByAsset> getIncompleteByAsset(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = atStartOfDayOrMin(fromDate);
        LocalDateTime to = atStartOfNextDayOrMax(toDate);

        List<Object[]> rows = workOrderRepository.incompleteByAssetInRange(INCOMPLETE_STATUSES, from, to);
        List<IncompleteWOByAsset> results = new ArrayList<>();

        for (Object[] row : rows) {
            Long assetId = row[0] != null ? ((Number) row[0]).longValue() : null;
            String assetName = row[1] != null ? row[1].toString() : null;
            Integer incompleteCount = row[2] != null ? ((Number) row[2]).intValue() : 0;
            Double averageAgeDays = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;

            IncompleteWOByAsset dto = new IncompleteWOByAsset();
            dto.setId(assetId);
            dto.setName(assetName);
            dto.setIncompleteCount(incompleteCount);
            dto.setAverageAgeDays(round2(averageAgeDays));

            results.add(dto);
        }

        return results;
    }

    public WOStatsByPriority getStatsByPriority(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = atStartOfDayOrMin(fromDate);
        LocalDateTime to = atStartOfNextDayOrMax(toDate);

        return new WOStatsByPriority(
                buildPriorityStats(WorkOrderPriority.NONE, from, to),
                buildPriorityStats(WorkOrderPriority.LOW, from, to),
                buildPriorityStats(WorkOrderPriority.MEDIUM, from, to),
                buildPriorityStats(WorkOrderPriority.HIGH, from, to)
        );
    }

    private WOStatsByPriority.BasicStats buildPriorityStats(
            WorkOrderPriority priority,
            LocalDateTime from,
            LocalDateTime to
    ) {
        Integer count = safeInt(workOrderRepository.countByPriorityInRange(priority, from, to));
        Double estimatedHours = safeDouble(workOrderRepository.sumEstimatedHoursByPriorityInRange(priority, from, to));
        return new WOStatsByPriority.BasicStats(count, round2(estimatedHours));
    }

    private LocalDateTime atStartOfDayOrMin(LocalDate date) {
        return date != null ? date.atStartOfDay() : LocalDate.of(2000, 1, 1).atStartOfDay();
    }

    private LocalDateTime atStartOfNextDayOrMax(LocalDate date) {
        return date != null ? date.plusDays(1).atStartOfDay() : LocalDate.of(2999, 12, 31).atStartOfDay();
    }

    private Integer safeInt(Long value) {
        return value == null ? 0 : value.intValue();
    }

    private Double safeDouble(Double value) {
        return value == null ? 0.0 : round2(value);
    }

    private Double round2(Double value) {
        if (value == null) {
            return 0.0;
        }
        return Math.round(value * 100.0) / 100.0;
    }
}