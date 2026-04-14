package com.emms.backend.mapper;

import com.emms.backend.dto.category.CategoryPatchDTO;
import com.emms.backend.entity.WorkOrderCategory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface WorkOrderCategoryMapper {

    WorkOrderCategory updateWorkOrderCategory(
            @MappingTarget WorkOrderCategory entity,
            CategoryPatchDTO dto
    );
}