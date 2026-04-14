package com.emms.backend.mapper;

import com.emms.backend.dto.woflow.*;
import com.emms.backend.entity.WorkflowCondition;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        uses = {
                AssetMapper.class,
                LocationMapper.class,
                UserMapper.class,
                WorkOrderCategoryMapper.class
        }
)
public interface WorkflowConditionMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workflow", ignore = true)
    WorkflowCondition updateWorkflowCondition(
            @MappingTarget WorkflowCondition entity,
            WorkflowConditionDTO dto
    );

    WorkflowConditionDTO toPatchDto(WorkflowCondition model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workflow", ignore = true)
    WorkflowCondition toModel(WorkflowConditionPostDTO dto);

    WorkflowConditionShowDTO toShowDto(WorkflowCondition model);
}