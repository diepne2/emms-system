package com.emms.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emms.backend.entity.InventoryCountItem;

public interface InventoryCountItemRepository
        extends JpaRepository<InventoryCountItem, Long> {

    List<InventoryCountItem> findByInventoryCountId(Long inventoryCountId);
}