package com.emms.backend.repository;

import com.emms.backend.entity.Meter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MeterRepository extends JpaRepository<Meter, Long>, JpaSpecificationExecutor<Meter> {

    Collection<Meter> findByAsset_AssetId(Long assetId);

    Collection<Meter> findByLocation_LocationId(Long locationId);

    Collection<Meter> findByMeterCategory_MeterCategoryId(Long meterCategoryId);

    List<Meter> findByMeterIdIn(List<Long> ids);

    Optional<Meter> findByMeterId(Long meterId);

    @Query("""
           SELECT DISTINCT m FROM Meter m
           LEFT JOIN FETCH m.meterCategory
           LEFT JOIN FETCH m.image
           LEFT JOIN FETCH m.location
           LEFT JOIN FETCH m.asset
           """)
    List<Meter> findAllForExport();

    void deleteByDemoTrue();

    @Query("SELECT CASE WHEN COUNT(m) > ?1 THEN true ELSE false END FROM Meter m")
    boolean hasMoreThan(Long threshold);
}