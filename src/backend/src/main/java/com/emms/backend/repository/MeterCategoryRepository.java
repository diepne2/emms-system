package com.emms.backend.repository;

import com.emms.backend.entity.MeterCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeterCategoryRepository extends JpaRepository<MeterCategory, Long> {

    List<MeterCategory> findAllByOrderByNameAsc();

    Optional<MeterCategory> findByName(String name);

    Optional<MeterCategory> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndMeterCategoryIdNot(String name, Long meterCategoryId);
}