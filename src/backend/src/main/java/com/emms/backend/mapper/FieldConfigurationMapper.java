package com.emms.backend.mapper;

import com.emms.backend.dto.fieldConfiguration.FieldConfigurationPatchDTO;
import com.emms.backend.entity.FieldConfiguration;
import org.springframework.stereotype.Component;

@Component
public class FieldConfigurationMapper {

    public FieldConfiguration updateFieldConfiguration(FieldConfiguration entity, FieldConfigurationPatchDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        if (dto.getFieldKey() != null) {
            entity.setFieldKey(dto.getFieldKey());
        }

        if (dto.getEnabled() != null) {
            entity.setEnabled(dto.getEnabled());
        }

        if (dto.getRequired() != null) {
            entity.setRequired(dto.getRequired());
        }

        if (dto.getDisplayOrder() != null) {
            entity.setDisplayOrder(dto.getDisplayOrder());
        }

        return entity;
    }

    public FieldConfigurationPatchDTO toPatchDto(FieldConfiguration model) {
        if (model == null) {
            return null;
        }

        FieldConfigurationPatchDTO dto = new FieldConfigurationPatchDTO();
        dto.setFieldKey(model.getFieldKey());
        dto.setEnabled(model.getEnabled());
        dto.setRequired(model.getRequired());
        dto.setDisplayOrder(model.getDisplayOrder());

        return dto;
    }
}