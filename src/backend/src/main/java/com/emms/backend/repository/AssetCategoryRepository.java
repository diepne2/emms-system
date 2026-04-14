package com.emms.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;


import com.emms.backend.entity.AssetCategory;

public interface AssetCategoryRepository extends JpaRepository<AssetCategory, Long>{
    Optional<AssetCategory> findByName(String name);
    Optional<AssetCategory> findByNameIgnoreCase(String name);

    Collection<AssetCategory> findByParent_CategoryId(Long parentId);
    Collection<AssetCategory> findByParent(AssetCategory parent);

    boolean existsByNameIgnoreCase(String name);
    
}
