package com.emms.backend.mapper;

import com.emms.backend.dto.labor.LaborPatchDTO;
import com.emms.backend.entity.Labor;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface LaborMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "laborId", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "timeCategory", ignore = true)
    @Mapping(target = "workOrder", ignore = true)
    @Mapping(target = "logged", ignore = true)
    @Mapping(target = "demo", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateLabor(@MappingTarget Labor entity, LaborPatchDTO dto);

    @Mapping(source = "assignedTo.userId", target = "assignedToId")
    @Mapping(source = "timeCategory.timeCategoryId", target = "timeCategoryId")
    LaborPatchDTO toPatchDto(Labor model);
}