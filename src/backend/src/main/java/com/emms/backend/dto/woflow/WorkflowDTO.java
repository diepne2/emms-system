package com.emms.backend.dto.woflow;

import com.emms.backend.dto.IdDTO;
import com.emms.backend.entity.enums.WFMainCondition;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "DTO for patching an existing workflow")
public class WorkflowDTO {

    @Schema(description = "Workflow title")
    private String title;

    @Schema(description = "Main condition")
    private WFMainCondition mainCondition;

    @ArraySchema(schema = @Schema(implementation = IdDTO.class))
    private List<IdDTO> secondaryConditions = new ArrayList<>();

    @Schema(implementation = IdDTO.class)
    private IdDTO action;

    public WorkflowDTO() {
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

    public List<IdDTO> getSecondaryConditions() {
        return secondaryConditions;
    }

    public void setSecondaryConditions(List<IdDTO> secondaryConditions) {
        this.secondaryConditions = secondaryConditions == null ? new ArrayList<>() : secondaryConditions;
    }

    public IdDTO getAction() {
        return action;
    }

    public void setAction(IdDTO action) {
        this.action = action;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}