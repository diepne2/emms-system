package com.emms.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.emms.backend.dto.document.AssetDocumentShowDTO;
import com.emms.backend.entity.AssetDocument;

@Mapper(componentModel = "spring")
public interface AssetDocumentMapper {

    @Mapping(source = "asset.assetId", target = "assetId")
    AssetDocumentShowDTO toDto(AssetDocument entity);
}