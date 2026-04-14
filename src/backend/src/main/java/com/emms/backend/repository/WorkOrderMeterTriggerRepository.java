package com.emms.backend.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emms.backend.entity.WorkOrderMeterTrigger;

public interface WorkOrderMeterTriggerRepository extends JpaRepository<WorkOrderMeterTrigger, Long>{
    Collection<WorkOrderMeterTrigger> findByMeter_MeterId(Long meterId);
    
}
