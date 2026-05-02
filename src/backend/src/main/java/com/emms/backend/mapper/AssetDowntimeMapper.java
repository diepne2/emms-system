package com.emms.backend.mapper;

import com.emms.backend.dto.asset.AssetDowntimeShowDTO;
import com.emms.backend.entity.AssetDowntime;
import org.springframework.stereotype.Component;

@Component
public class AssetDowntimeMapper {

    public AssetDowntimeShowDTO toShowDto(AssetDowntime entity) {
        if (entity == null) {
            return null;
        }

        AssetDowntimeShowDTO dto = new AssetDowntimeShowDTO();
        dto.setId(entity.getId());

        if (entity.getAsset() != null) {
            dto.setAssetId(entity.getAsset().getId());
            dto.setAssetName(entity.getAsset().getName());
        }

        if (entity.getWorkOrder() != null) {
            dto.setWorkOrderId(entity.getWorkOrder().getId());
        }

        dto.setReason(entity.getReason() != null ? entity.getReason().name() : null);
        dto.setStartsOn(entity.getStartsOn());
        dto.setEndsOn(entity.getEndsOn());
        dto.setDurationSeconds(entity.getDurationSeconds());
        dto.setOpen(entity.isOpen());
        dto.setNote(entity.getNote());
        dto.setCreatedAt(entity.getCreatedAt());

        return dto;
    }
}