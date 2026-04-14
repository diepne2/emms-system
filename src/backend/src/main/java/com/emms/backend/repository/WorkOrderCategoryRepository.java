package com.emms.backend.repository;


import com.emms.backend.entity.WorkOrderCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkOrderCategoryRepository extends JpaRepository<WorkOrderCategory, Long> {

    Optional<WorkOrderCategory> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<WorkOrderCategory> findAllByOrderByNameAsc();
}