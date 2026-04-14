package com.emms.backend.mapper;

import com.emms.backend.dto.asset.AssetDowntimeDTO;
import com.emms.backend.entity.AssetDowntime;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AssetDowntimeMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "workOrder", ignore = true)
    @Mapping(target = "durationSeconds", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateAssetDowntimeFromDto(AssetDowntimeDTO dto, @MappingTarget AssetDowntime entity);

    AssetDowntimeDTO toDto(AssetDowntime entity);
}