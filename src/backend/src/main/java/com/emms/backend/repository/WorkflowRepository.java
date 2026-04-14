package com.emms.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emms.backend.entity.Workflow;

public interface WorkflowRepository extends JpaRepository<Workflow, Long>{
    
}
