package com.emms.backend.repository;

import com.emms.backend.entity.Meter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeterRepository extends JpaRepository<Meter, Long> {

    boolean existsByAsset_Id(Long assetId);

    List<Meter> findByAsset_Id(Long assetId);
}