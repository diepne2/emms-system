package com.emms.backend.mapper;

import com.emms.backend.dto.task.TaskDTO;
import com.emms.backend.entity.Task;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    TaskDTO toDto(Task entity);

    @Mapping(target = "taskId", ignore = true)
    @Mapping(target = "taskBase", ignore = true)
    @Mapping(target = "workOrder", ignore = true)
    @Mapping(target = "preventiveMaintenance", ignore = true)
    @Mapping(target = "files", ignore = true)
    Task fromDto(TaskDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "taskId", ignore = true)
    @Mapping(target = "taskBase", ignore = true)
    @Mapping(target = "workOrder", ignore = true)
    @Mapping(target = "preventiveMaintenance", ignore = true)
    @Mapping(target = "files", ignore = true)
    void updateTask(@MappingTarget Task entity, TaskDTO dto);
}