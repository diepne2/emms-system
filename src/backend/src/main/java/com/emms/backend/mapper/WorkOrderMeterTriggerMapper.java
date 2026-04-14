package com.emms.backend.mapper;

import com.emms.backend.dto.woMeterTrigger.WorkOrderMeterTriggerDTO;
import com.emms.backend.dto.woMeterTrigger.WorkOrderMeterTriggerShowDTO;
import com.emms.backend.entity.WorkOrderMeterTrigger;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface WorkOrderMeterTriggerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "meter", ignore = true)
    WorkOrderMeterTrigger fromDto(WorkOrderMeterTriggerDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "meter", ignore = true)
    void update(@MappingTarget WorkOrderMeterTrigger entity, WorkOrderMeterTriggerDTO dto);

    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "meterId", source = "meter.id")
    @Mapping(target = "meterName", source = "meter.name")
    WorkOrderMeterTriggerShowDTO toShowDto(WorkOrderMeterTrigger entity);
}