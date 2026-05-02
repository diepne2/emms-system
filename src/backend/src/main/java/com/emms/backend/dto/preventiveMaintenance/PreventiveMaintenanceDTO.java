package com.emms.backend.dto.preventiveMaintenance;

import com.emms.backend.dto.workorderBase.WorkOrderBaseDTO;
import com.emms.backend.entity.enums.Priority;

import java.time.LocalDateTime;

public class PreventiveMaintenanceDTO extends WorkOrderBaseDTO {

    private String code;
    private Boolean active;
    private Double estimatedHours;
    private Long assetId;
    private Long assignedToId;
    private Priority priority;
    private LocalDateTime startsOn;
    private LocalDateTime endsOn;

    private RecurrenceRule recurrenceRule;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public RecurrenceRule getRecurrenceRule() {
        return recurrenceRule;
    }

    public void setRecurrenceRule(RecurrenceRule recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }


    public LocalDateTime getStartsOn() {
        return startsOn;
    }

    public void setStartsOn(LocalDateTime startsOn) {
        this.startsOn = startsOn;
    }

    public LocalDateTime getEndsOn() {
        return endsOn;
    }

    public void setEndsOn(LocalDateTime endsOn) {
        this.endsOn = endsOn;
    }
}