package com.emms.backend.mapper;

import com.emms.backend.dto.asset.AssetPUTDTO;
import com.emms.backend.dto.asset.AssetShowDTO;
import com.emms.backend.dto.asset.AssetSummaryDTO;
import com.emms.backend.entity.Asset;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring"
)
public interface AssetMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAsset(@MappingTarget Asset entity, AssetPUTDTO dto);

    AssetPUTDTO toPatchDto(Asset model);

    AssetShowDTO toShowDto(Asset model);

    AssetSummaryDTO toSummaryDto(Asset model);
}