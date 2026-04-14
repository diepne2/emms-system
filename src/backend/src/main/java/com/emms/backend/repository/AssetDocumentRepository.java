package com.emms.backend.repository;

import com.emms.backend.entity.AssetDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetDocumentRepository extends JpaRepository<AssetDocument, Long> {
    List<AssetDocument> findByAssetId(Long assetId);
}