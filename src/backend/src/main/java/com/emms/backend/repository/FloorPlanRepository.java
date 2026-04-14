package com.emms.backend.repository;

import com.emms.backend.entity.FloorPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface FloorPlanRepository extends JpaRepository<FloorPlan, Long> {
    Collection<FloorPlan> findByLocation_Id(Long id);
}