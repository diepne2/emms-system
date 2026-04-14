package com.emms.backend.mapper;

import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenanceDTO;
import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenanceShowDTO;
import com.emms.backend.entity.PreventiveMaintenance;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PreventiveMaintenanceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "requestedBy", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "schedule", ignore = true)
    PreventiveMaintenance fromDto(PreventiveMaintenanceDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "requestedBy", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "schedule", ignore = true)
    void update(@MappingTarget PreventiveMaintenance entity, PreventiveMaintenanceDTO dto);

    @Mapping(target = "assignedTo", ignore = true)
    PreventiveMaintenanceShowDTO toShowDto(PreventiveMaintenance entity);
}