package com.emms.backend.mapper;

import com.emms.backend.dto.task.*;
import com.emms.backend.entity.TaskBase;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = {AssetMapper.class, UserMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
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

    @Mapping(source = "createdBy", target = "createdByUser")
    TaskBaseShowDTO toShowDto(TaskBase entity);
}