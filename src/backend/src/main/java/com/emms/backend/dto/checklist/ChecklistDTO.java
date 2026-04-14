package com.emms.backend.dto.checklist;

import com.emms.backend.dto.task.TaskBaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "DTO for updating an existing checklist")
public class ChecklistDTO {

    @Schema(description = "Checklist name")
    private String name;

    @Schema(description = "Checklist description")
    private String description;

    @Schema(description = "Applies to (WORK_ORDER, MAINTENANCE_PLAN, ASSET, GENERAL)")
    private String appliesTo;

    @Schema(description = "Active status")
    private Boolean active;

    @Schema(description = "Checklist tasks")
    private List<TaskBaseDTO> tasks = new ArrayList<>();

    public ChecklistDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = trim(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = trim(description);
    }

    public String getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(String appliesTo) {
        this.appliesTo = trim(appliesTo);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<TaskBaseDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskBaseDTO> tasks) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}