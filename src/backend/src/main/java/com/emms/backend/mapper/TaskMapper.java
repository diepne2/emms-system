package com.emms.backend.mapper;

import com.emms.backend.dto.task.TaskDTO;
import com.emms.backend.dto.task.TaskShowDTO;
import com.emms.backend.entity.Task;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = {FileMapper.class, TaskBaseMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TaskMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workOrder", ignore = true)
    @Mapping(target = "preventiveMaintenance", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "taskBase", ignore = true)
    Task updateTask(@MappingTarget Task entity, TaskDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    TaskDTO toPatchDto(Task model);

    @Mapping(source = "taskId", target = "id")
    TaskShowDTO toShowDto(Task model);
}