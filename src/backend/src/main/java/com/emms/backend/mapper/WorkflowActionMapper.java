package com.emms.backend.mapper;

import com.emms.backend.dto.woflow.*;

import com.emms.backend.entity.WorkflowAction;
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
                WorkOrderCategoryMapper.class,
                ChecklistMapper.class,
                VendorMapper.class
        }
)
public interface WorkflowActionMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    WorkflowAction updateWorkflowAction(@MappingTarget WorkflowAction entity, WorkflowActionDTO dto);

    WorkflowActionDTO toPatchDto(WorkflowAction model);

    @Mapping(target = "id", ignore = true)
    WorkflowAction toModel(WorkflowActionPostDTO dto);

    @Mapping(source = "user", target = "assignedTo")
    WorkflowActionShowDTO toShowDto(WorkflowAction model);
}