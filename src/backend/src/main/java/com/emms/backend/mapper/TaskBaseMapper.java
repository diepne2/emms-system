package com.emms.backend.mapper;

import com.emms.backend.dto.task.TaskBaseDTO;
import com.emms.backend.dto.task.TaskBasePatchDTO;
import com.emms.backend.dto.task.TaskBaseShowDTO;
import com.emms.backend.entity.TaskBase;
import com.emms.backend.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TaskBaseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TaskBase fromDto(TaskBaseDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "asset", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget TaskBase entity, TaskBasePatchDTO dto);

    @Mapping(source = "createdBy", target = "createdBy")
    TaskBaseShowDTO toShowDto(TaskBase entity);

    default String map(User user) {
        return user == null ? null : user.getUsername();
    }
}