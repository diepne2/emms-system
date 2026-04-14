package com.emms.backend.dto.woflow;

import com.emms.backend.entity.enums.WFMainCondition;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Workflow configuration to create")
public class WorkflowPostDTO {

    @NotBlank(message = "Title must not be blank")
    private String title;

    @NotNull
    private WFMainCondition mainCondition;

    private List<WorkflowConditionPostDTO> secondaryConditions = new ArrayList<>();

    @NotNull
    private WorkflowActionPostDTO action;

    public WorkflowPostDTO() {}

    public WorkflowPostDTO(String title,
                           WFMainCondition mainCondition,
                           List<WorkflowConditionPostDTO> secondaryConditions,
                           WorkflowActionPostDTO action) {
        this.title = trim(title);
        this.mainCondition = mainCondition;
        this.secondaryConditions = secondaryConditions != null ? secondaryConditions : new ArrayList<>();
        this.action = action;
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

    public List<WorkflowConditionPostDTO> getSecondaryConditions() {
        return secondaryConditions;
    }

    public void setSecondaryConditions(List<WorkflowConditionPostDTO> secondaryConditions) {
        this.secondaryConditions = secondaryConditions == null ? new ArrayList<>() : secondaryConditions;
    }

    public WorkflowActionPostDTO getAction() {
        return action;
    }

    public void setAction(WorkflowActionPostDTO action) {
        this.action = action;
    }

    private String trim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}