package com.emms.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emms.backend.entity.WorkflowAction;

public interface WorkflowActionRepository extends JpaRepository<WorkflowAction, Long> {
    
}
