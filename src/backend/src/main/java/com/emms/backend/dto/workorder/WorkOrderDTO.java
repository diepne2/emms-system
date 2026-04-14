package com.emms.backend.dto.workorder;

import com.emms.backend.dto.workorderBase.WorkOrderBaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO for patching an existing work order")
public class WorkOrderDTO extends WorkOrderBaseDTO {

    @Schema(description = "ID of assigned user")
    private Long assignedToId;

    @Schema(description = "Full name of user who completed the work order")
    private String completedBy;

    @Schema(description = "Date and time when the work order was completed")
    private LocalDateTime completedOn;

    @Schema(description = "Whether the work order is archived")
    private Boolean archived;

    public WorkOrderDTO() {
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(String completedBy) {
        this.completedBy = trim(completedBy);
    }

    public LocalDateTime getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(LocalDateTime completedOn) {
        this.completedOn = completedOn;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    @Override
    public void setEstimatedDuration(Double estimatedDuration) {
        super.setEstimatedDuration(estimatedDuration);
    }

    protected String trim(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}