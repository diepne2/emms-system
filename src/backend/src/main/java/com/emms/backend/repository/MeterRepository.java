package com.emms.backend.repository;

import com.emms.backend.entity.Meter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MeterRepository extends JpaRepository<Meter, Long>, JpaSpecificationExecutor<Meter> {

    List<Meter> findAllByOrderByNameAsc();

    Optional<Meter> findByNameIgnoreCase(String name);

    Collection<Meter> findByMeterCategory_MeterCategoryId(Long meterCategoryId);

    Collection<Meter> findByAsset_Id(Long assetId);

    Collection<Meter> findByLocation_Id(Long locationId);

    Collection<Meter> findByDemo(boolean demo);
}