package com.emms.backend.repository;

import com.emms.backend.entity.Asset;
import com.emms.backend.entity.enums.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

    List<Asset> findByNameContainingIgnoreCase(String name);

    List<Asset> findByStatus(AssetStatus status);

    Page<Asset> findByStatus(AssetStatus status, Pageable pageable);

    List<Asset> findByParentAssetName(String parentName);

    long countByParentAssetName(String parentName);

    boolean existsByParentAssetName(String parentName);

    List<Asset> findByLocationName(String locationName);

    List<Asset> findByNameIgnoreCaseAndStatus(String name, AssetStatus status);

    Optional<Asset> findByNameIgnoreCase(String name);

    Optional<Asset> findByBarcodeIgnoreCase(String barcode);
    
    Optional<Asset> findBySerialNumberIgnoreCase(String serialNumber);
}