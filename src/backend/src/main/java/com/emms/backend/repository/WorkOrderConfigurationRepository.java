package com.emms.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emms.backend.entity.WorkOrderConfiguration;

public interface WorkOrderConfigurationRepository extends JpaRepository<WorkOrderConfiguration, Long> {
    Optional<WorkOrderConfiguration> findByConfigCode(String configCode);
    
}
