package com.emms.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emms.backend.entity.InventoryMonthlyClosing;

public interface InventoryMonthlyClosingRepository
        extends JpaRepository<InventoryMonthlyClosing, Long> {

    boolean existsByYearAndMonthAndStatus(Integer year, Integer month, String status);
}