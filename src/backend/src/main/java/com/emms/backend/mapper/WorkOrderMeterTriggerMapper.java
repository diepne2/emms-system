package com.emms.backend.mapper;

import com.emms.backend.dto.woMeterTrigger.WorkOrderMeterTriggerDTO;
import com.emms.backend.dto.woMeterTrigger.WorkOrderMeterTriggerShowDTO;
import com.emms.backend.entity.WorkOrderMeterTrigger;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface WorkOrderMeterTriggerMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "meter", ignore = true)
    @Mapping(target = "lastTriggeredAt", ignore = true)
    WorkOrderMeterTrigger update(
            @MappingTarget WorkOrderMeterTrigger entity,
            WorkOrderMeterTriggerDTO dto
    );

    @Mapping(source = "meter.id", target = "meterId")
    @Mapping(source = "meter.name", target = "meterName")
    WorkOrderMeterTriggerShowDTO toShowDto(WorkOrderMeterTrigger entity);
}