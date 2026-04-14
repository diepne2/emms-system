package com.emms.backend.repository;

import com.emms.backend.entity.Task;
import com.emms.backend.entity.Task.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface TaskRepository extends JpaRepository<Task, Long>,
        JpaSpecificationExecutor<Task> {

    // ===== BASIC =====
    List<Task> findByWorkOrder_IdOrderByCreatedAtAsc(Long workOrderId);

    List<Task> findByPreventiveMaintenance_Id(Long pmId);

    // ===== STATUS FILTER =====
    List<Task> findByWorkOrder_IdAndStatus(Long workOrderId, TaskStatus status);

    List<Task> findByPreventiveMaintenance_IdAndStatus(Long pmId, TaskStatus status);

    // ===== COUNT (Dashboard) =====
    long countByWorkOrder_Id(Long workOrderId);

    long countByWorkOrder_IdAndStatus(Long workOrderId, TaskStatus status);

    long countByPreventiveMaintenance_Id(Long pmId);

    long countByPreventiveMaintenance_IdAndStatus(Long pmId, TaskStatus status);

}