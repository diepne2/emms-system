package com.emms.backend.dto.task;


import com.emms.backend.entity.Task.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for patching an existing task")
public class TaskDTO {

    @Schema(description = "Task status")
    private TaskStatus status;

    @Schema(description = "Task notes")
    private String notes;

    @Schema(description = "Task value")
    private String value;

    @Schema(description = "Sort order")
    private Integer sortOrder;

    @Schema(description = "Required task")
    private Boolean requiredTask;

    // ===== Constructor =====
    public TaskDTO() {
    }

    // ===== Getter & Setter =====
    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = trim(notes);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = trim(value);
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getRequiredTask() {
        return requiredTask;
    }

    public void setRequiredTask(Boolean requiredTask) {
        this.requiredTask = requiredTask;
    }

    // ===== Utils =====
    private String trim(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}