package com.emms.backend.mapper;

import com.emms.backend.dto.category.CategoryPatchDTO;
import com.emms.backend.entity.AssetCategory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface AssetCategoryMapper {
    AssetCategory updateAssetCategory(@MappingTarget AssetCategory entity, AssetCategory assetCategory);

    @Mappings({})
    CategoryPatchDTO toPatchDto(AssetCategory model);
}