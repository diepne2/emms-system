package com.emms.backend.mapper;

import com.emms.backend.dto.request.RequestShowDTO;
import com.emms.backend.dto.request.RequestSummaryDTO;
import com.emms.backend.entity.Request;
import com.emms.backend.entity.WorkOrder;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

    public RequestSummaryDTO toSummaryDto(Request entity) {
        if (entity == null) {
            return null;
        }

        RequestSummaryDTO dto = new RequestSummaryDTO();

        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        dto.setPriority(entity.getPriority() != null ? entity.getPriority().name() : null);
        dto.setDueDate(entity.getDueDate());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getLocation() != null) {
            dto.setLocationId(entity.getLocation().getId());
            dto.setLocationName(entity.getLocation().getName());
        }

        if (entity.getAsset() != null) {
            dto.setAssetId(entity.getAsset().getId());
            dto.setAssetName(entity.getAsset().getName());
            dto.setAssetCode(entity.getAsset().getBarcode());
        }

        if (entity.getWorkOrder() != null) {
            dto.setWorkOrderId(entity.getWorkOrder().getId());
        }

        return dto;
    }

    public RequestShowDTO toShowDto(Request entity) {
        if (entity == null) {
            return null;
        }

        RequestShowDTO dto = new RequestShowDTO();

        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setDueDate(entity.getDueDate());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        dto.setPriority(entity.getPriority() != null ? entity.getPriority().name() : null);
        dto.setCancelled(entity.isCancelled());
        dto.setCancellationReason(entity.getCancellationReason());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getLocation() != null) {
            dto.setLocationId(entity.getLocation().getId());
            dto.setLocationName(entity.getLocation().getName());
        }

  
        if (entity.getAsset() != null) {
            dto.setAssetId(entity.getAsset().getId());
            dto.setAssetName(entity.getAsset().getName());
            dto.setAssetCode(entity.getAsset().getBarcode());
        }

  
        WorkOrder workOrder = entity.getWorkOrder();
        if (workOrder != null) {
            dto.setWorkOrderId(workOrder.getId());
            dto.setWorkOrderCode(String.valueOf(workOrder.getId()));
        }

        return dto;
    }
}