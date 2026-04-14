package com.emms.backend.repository;

import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.WorkOrder.WorkOrderPriority;
import com.emms.backend.entity.WorkOrder.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    List<WorkOrder> findAllByOrderByDateCreatedDesc();

    Collection<WorkOrder> findByStatus(WorkOrderStatus status);

    Collection<WorkOrder> findByPriority(WorkOrderPriority priority);

    Collection<WorkOrder> findByAssignedTo_UsernameIgnoreCase(String username);

    Collection<WorkOrder> findByDateCreatedBetween(LocalDateTime start, LocalDateTime end);

    Collection<WorkOrder> findByCompletedOnBetween(LocalDateTime start, LocalDateTime end);

    List<WorkOrder> findByAssignedTo_UserIdAndCreatedAtBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
        select coalesce(sum(w.totalCost), 0)
        from WorkOrder w
        where (:assetId is null or w.asset.id = :assetId)
          and w.createdAt >= :fromDate
          and w.createdAt < :toDate
    """)
    BigDecimal sumActualCostByAssetAndDateRange(
            @Param("assetId") Long assetId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
        select function('date', w.createdAt), coalesce(sum(w.totalCost), 0)
        from WorkOrder w
        where (:assetId is null or w.asset.id = :assetId)
          and w.createdAt >= :fromDate
          and w.createdAt < :toDate
        group by function('date', w.createdAt)
        order by function('date', w.createdAt)
    """)
    List<Object[]> findActualCostGroupByDateAndAsset(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("assetId") Long assetId
    );

    default Map<LocalDate, BigDecimal> sumActualCostGroupByDateAndAsset(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long assetId
    ) {
        List<Object[]> rows = findActualCostGroupByDateAndAsset(fromDate, toDate, assetId);
        Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();

        for (Object[] row : rows) {
            LocalDate date;
            if (row[0] instanceof java.sql.Date sqlDate) {
                date = sqlDate.toLocalDate();
            } else if (row[0] instanceof LocalDate localDate) {
                date = localDate;
            } else {
                date = LocalDate.parse(row[0].toString());
            }

            BigDecimal cost = (BigDecimal) row[1];
            result.put(date, cost == null ? BigDecimal.ZERO : cost);
        }

        return result;
    }

    @Query("""
        select avg(
            function('timestampdiff', hour, w.createdAt,
                (
                    select min(w2.createdAt)
                    from WorkOrder w2
                    where w2.asset.id = w.asset.id
                      and w2.createdAt > w.createdAt
                )
            )
        )
        from WorkOrder w
        where (:assetId is null or w.asset.id = :assetId)
          and w.createdAt >= :fromDate
          and w.createdAt < :toDate
    """)
    Double calculateAverageMaintenanceIntervalHours(
            @Param("assetId") Long assetId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
        select count(w)
        from WorkOrder w
        where w.createdAt >= :fromDate
          and w.createdAt < :toDate
    """)
    Long countAllInRange(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
        select count(w)
        from WorkOrder w
        where w.status = :status
          and w.createdAt >= :fromDate
          and w.createdAt < :toDate
    """)
    Long countByStatusInRange(
            @Param("status") WorkOrderStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    /*
     * Entity hiện tại chưa có sourceType / maintenancePlan.
     * Tạm coi compliant = tất cả WO trong range.
     */
    @Query("""
        select count(w)
        from WorkOrder w
        where w.createdAt >= :fromDate
          and w.createdAt < :toDate
    """)
    Long countCompliantInRange(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
        select avg(function('timestampdiff', hour, w.createdAt, w.completedOn))
        from WorkOrder w
        where w.completedOn is not null
          and w.createdAt >= :fromDate
          and w.createdAt < :toDate
    """)
    Double getAverageCycleTimeHours(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    /*
     * Entity hiện tại chưa có startedAt.
     * Tạm dùng completedOn để không làm vỡ app.
     */
    @Query("""
        select avg(function('timestampdiff', hour, w.createdAt, w.completedOn))
        from WorkOrder w
        where w.completedOn is not null
          and w.createdAt >= :fromDate
          and w.createdAt < :toDate
    """)
    Double getAverageResponseTimeHours(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
        select coalesce(sum(w.estimatedDuration), 0)
        from WorkOrder w
        where w.createdAt >= :fromDate
          and w.createdAt < :toDate
    """)
    Double sumEstimatedHoursInRange(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    /*
     * Entity hiện tại chưa có actualDuration.
     * Tạm trả 0.0 để app boot.
     */
    @Query("""
        select 0.0
        from WorkOrder w
        where w.createdAt >= :fromDate
          and w.createdAt < :toDate
    """)
    Double sumActualHoursInRange(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    /*
     * Entity hiện tại chưa có sourceType / maintenancePlan.
     * Tạm coi compliant = tất cả WO trong range.
     */
    @Query("""
        select coalesce(sum(w.estimatedDuration), 0)
        from WorkOrder w
        where w.createdAt >= :fromDate
          and w.createdAt < :toDate
    """)
    Double sumEstimatedHoursForCompliantInRange(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
        select u.userId,
               u.username,
               concat(coalesce(u.firstName, ''), ' ', coalesce(u.lastName, '')),
               count(w)
        from WorkOrder w
        join w.assignedTo u
        where w.createdAt >= :fromDate
          and w.createdAt < :toDate
        group by u.userId, u.username, u.firstName, u.lastName
        order by count(w) desc
    """)
    List<Object[]> countByUserInRange(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
        select count(w)
        from WorkOrder w
        where w.status in :statuses
          and w.createdAt >= :fromDate
          and w.createdAt < :toDate
    """)
    Long countByStatusesInRange(
            @Param("statuses") Collection<WorkOrderStatus> statuses,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
        select avg(function('timestampdiff', day, w.createdAt, current_timestamp))
        from WorkOrder w
        where w.status in :statuses
          and w.createdAt >= :fromDate
          and w.createdAt < :toDate
    """)
    Double averageAgeByStatusesInRange(
            @Param("statuses") Collection<WorkOrderStatus> statuses,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
        select u.userId,
               u.username,
               concat(coalesce(u.firstName, ''), ' ', coalesce(u.lastName, '')),
               count(w),
               avg(function('timestampdiff', day, w.createdAt, current_timestamp))
        from WorkOrder w
        join w.assignedTo u
        where w.status in :statuses
          and w.createdAt >= :fromDate
          and w.createdAt < :toDate
        group by u.userId, u.username, u.firstName, u.lastName
        order by count(w) desc
    """)
    List<Object[]> incompleteByUserInRange(
            @Param("statuses") Collection<WorkOrderStatus> statuses,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
        select a.id, a.name, count(w),
               avg(function('timestampdiff', day, w.createdAt, current_timestamp))
        from WorkOrder w
        join w.asset a
        where w.status in :statuses
          and w.createdAt >= :fromDate
          and w.createdAt < :toDate
        group by a.id, a.name
        order by count(w) desc
    """)
    List<Object[]> incompleteByAssetInRange(
            @Param("statuses") Collection<WorkOrderStatus> statuses,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
        select count(w)
        from WorkOrder w
        where w.priority = :priority
          and w.createdAt >= :fromDate
          and w.createdAt < :toDate
    """)
    Long countByPriorityInRange(
            @Param("priority") WorkOrderPriority priority,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
        select coalesce(sum(w.estimatedDuration), 0)
        from WorkOrder w
        where w.priority = :priority
          and w.createdAt >= :fromDate
          and w.createdAt < :toDate
    """)
    Double sumEstimatedHoursByPriorityInRange(
            @Param("priority") WorkOrderPriority priority,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}