package com.emms.backend.mapper;

import com.emms.backend.dto.wo_history.WorkOrderHistoryShowDTO;
import com.emms.backend.entity.User;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.WorkOrderHistory;
import org.springframework.stereotype.Component;

@Component
public class WorkOrderHistoryMapper {

    public WorkOrderHistoryShowDTO toShowDto(WorkOrderHistory entity) {
        if (entity == null) {
            return null;
        }

        WorkOrderHistoryShowDTO dto = new WorkOrderHistoryShowDTO();
        dto.setId(entity.getId());
        dto.setVersionNo(entity.getVersionNo());
        dto.setVersionName(entity.getVersionName());
        dto.setNote(entity.getNote());
        dto.setSnapshotJson(entity.getSnapshotJson());
        dto.setCreatedAt(entity.getCreatedAt());

        WorkOrder workOrder = entity.getWorkOrder();
        if (workOrder != null) {
            dto.setWorkOrderId(workOrder.getId());
        }

        User savedBy = entity.getSavedBy();
        if (savedBy != null) {
            dto.setSavedById(savedBy.getUserId());
            dto.setSavedByName(
                    savedBy.getFullName() != null && !savedBy.getFullName().isBlank()
                            ? savedBy.getFullName().trim()
                            : savedBy.getUsername()
            );
        }

        return dto;
    }
}