package com.emms.backend.mapper;

import com.emms.backend.dto.task.TaskOptionPatchDTO;
import com.emms.backend.dto.task.TaskOptionShowDTO;
import com.emms.backend.entity.TaskOption;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TaskOptionMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "taskOptionId", ignore = true)
    @Mapping(target = "taskBase", ignore = true)
    TaskOption updateTaskOption(@MappingTarget TaskOption entity, TaskOptionPatchDTO dto);

    @Mapping(source = "taskOptionId", target = "id")
    TaskOptionPatchDTO toPatchDto(TaskOption model);

    @Mapping(source = "taskOptionId", target = "id")
    TaskOptionShowDTO toShowDto(TaskOption model);
}