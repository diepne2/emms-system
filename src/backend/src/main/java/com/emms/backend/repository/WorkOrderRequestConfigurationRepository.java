package com.emms.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emms.backend.entity.WorkOrderRequestConfiguration;

public interface WorkOrderRequestConfigurationRepository extends JpaRepository<WorkOrderRequestConfiguration, Long>{
    
}
