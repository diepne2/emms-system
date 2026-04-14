package com.emms.backend.repository;

import com.emms.backend.entity.AssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetCategoryRepository extends JpaRepository<AssetCategory, Long> {

    List<AssetCategory> findByParent_Id(Long parentId);

    Optional<AssetCategory> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}