package com.emms.backend.repository;

import com.emms.backend.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    Optional<Schedule> findByPreventiveMaintenance_PreventiveMaintenanceId(Long preventiveMaintenanceId);

    List<Schedule> findByDisabledFalse();

    List<Schedule> findByDemoTrue();

    @Transactional
    void deleteByDemoTrue();

    @Modifying
    @Transactional
    @Query("""
        update Schedule s
        set s.disabled = true
        where s.demo = true
    """)
    int disableAllDemoSchedules();

    @Query("""
        select s
        from Schedule s
        where s.disabled = false
          and s.startsOn <= :today
          and (s.endsOn is null or s.endsOn >= :today)
    """)
    Collection<Schedule> findActiveSchedules(LocalDate today);

    @Query(value = """
        SELECT s.*
        FROM schedules s
        LEFT JOIN (
            SELECT parent_preventive_maintenance_id,
                   COUNT(*) AS total,
                   MAX(first_time_to_react) AS last_react
            FROM (
                SELECT wo.parent_preventive_maintenance_id,
                       wo.first_time_to_react,
                       ROW_NUMBER() OVER (
                           PARTITION BY wo.parent_preventive_maintenance_id
                           ORDER BY wo.created_at DESC
                       ) AS rn
                FROM work_order wo
            ) ranked_wo
            WHERE rn <= 10
            GROUP BY parent_preventive_maintenance_id
        ) wo_stats
            ON s.preventive_maintenance_id = wo_stats.parent_preventive_maintenance_id
        WHERE s.disabled = false
          AND s.starts_on <= CURRENT_DATE
          AND (s.ends_on IS NULL OR s.ends_on >= CURRENT_DATE)
          AND (
                wo_stats.total IS NULL
                OR wo_stats.total < 10
                OR wo_stats.last_react IS NOT NULL
          )
        """, nativeQuery = true)
    Collection<Schedule> findByActive();
}