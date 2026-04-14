package com.emms.backend.mapper;

import com.emms.backend.dto.category.CategoryPatchDTO;
import com.emms.backend.entity.TimeCategory;
import org.springframework.stereotype.Component;

@Component
public class TimeCategoryMapper {

    public TimeCategory updateTimeCategory(TimeCategory entity, CategoryPatchDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        return entity;
    }

    public CategoryPatchDTO toPatchDto(TimeCategory model) {
        if (model == null) {
            return null;
        }

        CategoryPatchDTO dto = new CategoryPatchDTO();
        dto.setName(model.getName());
        return dto;
    }
}