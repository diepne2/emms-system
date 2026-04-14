package com.emms.backend.repository;

import com.emms.backend.entity.TimeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimeCategoryRepository extends JpaRepository<TimeCategory, Long> {

    List<TimeCategory> findAllByOrderByNameAsc();

    Optional<TimeCategory> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    void deleteByNameIgnoreCase(String name);
}