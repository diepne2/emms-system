package com.emms.backend.mapper;

import com.emms.backend.dto.labor.LaborCreateDTO;
import com.emms.backend.dto.labor.LaborPatchDTO;
import com.emms.backend.dto.labor.LaborShowDTO;
import com.emms.backend.entity.Labor;
import com.emms.backend.entity.TimeCategory;
import com.emms.backend.entity.User;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.enums.TimeStatus;
import org.springframework.stereotype.Component;

@Component
public class LaborMapper {

    public Labor toEntity(LaborCreateDTO dto, WorkOrder workOrder, User assignedTo, TimeCategory timeCategory) {
        if (dto == null) {
            return null;
        }

        Labor labor = new Labor();
        labor.setWorkOrder(workOrder);
        labor.setAssignedTo(assignedTo);
        labor.setTimeCategory(timeCategory);
        labor.setStartedAt(dto.getStartedAt());
        labor.setHourlyRate(dto.getHourlyRate() == null ? 0.0 : dto.getHourlyRate());
        labor.setDuration(dto.getDuration() == null ? 0L : dto.getDuration());
        labor.setIncludeToTotalTime(dto.getIncludeToTotalTime() == null || dto.getIncludeToTotalTime());

        String status = dto.getStatus() == null ? "RUNNING" : dto.getStatus().trim().toUpperCase();
        labor.setStatus("STOPPED".equals(status) ? TimeStatus.STOPPED : TimeStatus.RUNNING);

        labor.setLogged(false);
        labor.setDemo(false);

        return labor;
    }

    public void updateLabor(Labor labor, LaborPatchDTO dto, User assignedTo, TimeCategory timeCategory) {
        if (labor == null || dto == null) {
            return;
        }

        if (dto.getAssignedToId() != null) {
            labor.setAssignedTo(assignedTo);
        }

        if (dto.getTimeCategoryId() != null) {
            labor.setTimeCategory(timeCategory);
        }

        if (dto.getIncludeToTotalTime() != null) {
            labor.setIncludeToTotalTime(dto.getIncludeToTotalTime());
        }

        if (dto.getHourlyRate() != null) {
            labor.setHourlyRate(dto.getHourlyRate());
        }

        if (dto.getDuration() != null) {
            labor.setDuration(dto.getDuration());
        }

        if (dto.getStartedAt() != null) {
            labor.setStartedAt(dto.getStartedAt());
        }
    }

    public LaborShowDTO toShowDTO(Labor labor) {
        if (labor == null) {
            return null;
        }

        LaborShowDTO dto = new LaborShowDTO();
        dto.setId(labor.getId());

        if (labor.getWorkOrder() != null) {
            dto.setWorkOrderId(labor.getWorkOrder().getId());
            dto.setWorkOrderTitle(labor.getWorkOrder().getTitle());

            dto.setWorkOrderCode("WO-" + labor.getWorkOrder().getId());
        }

        if (labor.getAssignedTo() != null) {
            dto.setAssignedToId(labor.getAssignedTo().getId());

            String firstName = labor.getAssignedTo().getFirstName() != null
                    ? labor.getAssignedTo().getFirstName()
                    : "";
            String lastName = labor.getAssignedTo().getLastName() != null
                    ? labor.getAssignedTo().getLastName()
                    : "";

            String fullName = (firstName + " " + lastName).trim();
            dto.setAssignedToName(fullName.isBlank() ? labor.getAssignedTo().getUsername() : fullName);
        }

        if (labor.getTimeCategory() != null) {
            dto.setTimeCategoryId(labor.getTimeCategory().getTimeCategoryId());
            dto.setTimeCategoryName(labor.getTimeCategory().getName());
        }

        dto.setIncludeToTotalTime(labor.isIncludeToTotalTime());
        dto.setHourlyRate(labor.getHourlyRate());
        dto.setDuration(labor.getDuration());
        dto.setCost(labor.getCost());
        dto.setStartedAt(labor.getStartedAt());
        dto.setEndedAt(labor.getEndedAt());
        dto.setStatus(labor.getStatus() == null ? null : labor.getStatus().name());

        return dto;
    }
}