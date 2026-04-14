package com.emms.backend.dto.woflow;

import com.emms.backend.dto.audit.AuditShowDTO;
import com.emms.backend.entity.enums.WFMainCondition;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Workflow configuration showing conditions, actions, and enablement status")
public class WorkflowShowDTO extends AuditShowDTO {

    @Schema(description = "Unique identifier")
    private Long id;

    @Schema(description = "Title of the workflow rule")
    private String title;

    @Schema(description = "Main condition that triggers the workflow")
    private WFMainCondition mainCondition;

    @Schema(description = "Secondary conditions for additional filtering")
    private List<WorkflowConditionShowDTO> secondaryConditions = new ArrayList<>();

    @Schema(description = "Action to execute when conditions are met")
    private WorkflowActionShowDTO action;

    @Schema(description = "Whether the workflow is currently active")
    private boolean enabled;

    public WorkflowShowDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = trim(title);
    }

    public WFMainCondition getMainCondition() {
        return mainCondition;
    }

    public void setMainCondition(WFMainCondition mainCondition) {
        this.mainCondition = mainCondition;
    }

    public List<WorkflowConditionShowDTO> getSecondaryConditions() {
        return secondaryConditions;
    }

    public void setSecondaryConditions(List<WorkflowConditionShowDTO> secondaryConditions) {
        this.secondaryConditions = secondaryConditions == null ? new ArrayList<>() : secondaryConditions;
    }

    public WorkflowActionShowDTO getAction() {
        return action;
    }

    public void setAction(WorkflowActionShowDTO action) {
        this.action = action;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}