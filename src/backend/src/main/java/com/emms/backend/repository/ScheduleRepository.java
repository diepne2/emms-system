package com.emms.backend.repository;

import com.emms.backend.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    Optional<Schedule> findByPreventiveMaintenance_Id(Long preventiveMaintenanceId);

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
    List<Schedule> findActiveSchedules(LocalDate today);
}