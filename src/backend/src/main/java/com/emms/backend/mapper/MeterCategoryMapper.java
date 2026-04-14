package com.emms.backend.mapper;

import org.springframework.stereotype.Component;

import com.emms.backend.dto.category.CategoryPatchDTO;
import com.emms.backend.entity.MeterCategory;

@Component
public class MeterCategoryMapper {

    public MeterCategory updateMeterCategory(MeterCategory entity, CategoryPatchDTO dto) {
        if (entity == null || dto == null) return entity;

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }

        return entity;
    }

    public CategoryPatchDTO toPatchDto(MeterCategory model) {
        if (model == null) return null;

        CategoryPatchDTO dto = new CategoryPatchDTO();
        dto.setName(model.getName());
        dto.setDescription(model.getDescription());
        return dto;
    }
}

