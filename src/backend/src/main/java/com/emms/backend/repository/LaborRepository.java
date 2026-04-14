package com.emms.backend.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emms.backend.entity.Labor;

public interface LaborRepository extends JpaRepository<Labor, Long>{
    Collection<Labor> findByWorkOrder_Id(Long workOrderId);
    
}
