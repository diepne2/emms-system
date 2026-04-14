package com.emms.backend.mapper;

import com.emms.backend.dto.document.*;
import com.emms.backend.entity.AssetDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssetDocumentMapper {

    AssetDocumentShowDTO toDto(AssetDocument entity);
}