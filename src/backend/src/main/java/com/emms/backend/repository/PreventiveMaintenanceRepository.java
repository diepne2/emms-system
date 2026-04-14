package com.emms.backend.repository;

import com.emms.backend.entity.PreventiveMaintenance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PreventiveMaintenanceRepository
        extends JpaRepository<PreventiveMaintenance, Long>,
        JpaSpecificationExecutor<PreventiveMaintenance> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<PreventiveMaintenance> findByCodeIgnoreCase(String code);

    Collection<PreventiveMaintenance> findByActive(boolean active);

    Collection<PreventiveMaintenance> findByDemo(boolean demo);

    List<PreventiveMaintenance> findByCreatedAtBefore(LocalDateTime before);

    List<PreventiveMaintenance> findByDueDateBefore(LocalDateTime before);

    void deleteByDemoTrue();

    @EntityGraph(attributePaths = {"schedule", "asset", "requestedBy", "assignedTo"})
    @Query("SELECT p FROM PreventiveMaintenance p")
    List<PreventiveMaintenance> findAllForExport();

    @Query("SELECT COUNT(p) > :threshold FROM PreventiveMaintenance p")
    boolean hasMoreThan(@Param("threshold") long threshold);
}