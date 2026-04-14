package com.emms.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emms.backend.entity.WorkflowCondition;

import java.util.Collection;

public interface WorkflowConditionRepository extends JpaRepository<WorkflowCondition, Long> {

    Collection<WorkflowCondition> findByWorkflow_Id(Long workflowId);
}