package com.emms.backend.repository;

import com.emms.backend.entity.InventoryCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryCountRepository extends JpaRepository<InventoryCount, Long> {

    boolean existsByYearAndMonthAndStatus(Integer year, Integer month, String status);
}