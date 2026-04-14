package com.emms.backend.mapper;

import com.emms.backend.dto.woflow.*;

import com.emms.backend.entity.Workflow;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        uses = {
                WorkflowConditionMapper.class,
                WorkflowActionMapper.class
        }
)
public interface WorkflowMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "secondaryConditions", ignore = true)
    @Mapping(target = "action", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Workflow updateWorkflow(@MappingTarget Workflow entity, WorkflowDTO dto);

    WorkflowDTO toPatchDto(Workflow model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Workflow toModel(WorkflowPostDTO dto);

    WorkflowShowDTO toShowDto(Workflow model);
}