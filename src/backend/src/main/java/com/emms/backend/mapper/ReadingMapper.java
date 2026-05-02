package com.emms.backend.mapper;

import com.emms.backend.dto.reading.ReadingDTO;
import com.emms.backend.dto.reading.ReadingShowDTO;
import com.emms.backend.entity.Reading;
import org.springframework.stereotype.Component;

@Component
public class ReadingMapper {

    public Reading toEntity(ReadingDTO dto) {
        if (dto == null) {
            return null;
        }

        Reading entity = new Reading();
        entity.setValue(dto.getValue());
        entity.setRecordedAt(dto.getRecordedAt());
        entity.setNote(dto.getNote());
        return entity;
    }

    public ReadingShowDTO toShowDto(Reading entity) {
        if (entity == null) {
            return null;
        }

        ReadingShowDTO dto = new ReadingShowDTO();
        dto.setId(entity.getId());
        dto.setValue(entity.getValue());
        dto.setDeltaValue(entity.getDeltaValue());
        dto.setRecordedAt(entity.getRecordedAt());
        dto.setNote(entity.getNote());
        dto.setTriggered(entity.isTriggered());
        dto.setTriggeredWorkOrderId(entity.getTriggeredWorkOrderId());

        if (entity.getMeter() != null) {
            dto.setMeterId(entity.getMeter().getId());
            dto.setMeterName(entity.getMeter().getName());
        }

        return dto;
    }
}