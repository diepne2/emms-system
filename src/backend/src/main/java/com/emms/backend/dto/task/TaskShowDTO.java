package com.emms.backend.dto.task;

import com.emms.backend.dto.audit.AuditShowDTO;
import com.emms.backend.dto.file.FileShowDTO;
import com.emms.backend.entity.Task.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "DTO for displaying task details in API responses")
public class TaskShowDTO extends AuditShowDTO {

    @Schema(description = "Task ID")
    private Long id;

    @Schema(description = "Base task information")
    private TaskBaseShowDTO taskBase;

    @Schema(description = "Task label")
    private String label;

    @Schema(description = "Task notes")
    private String notes;

    @Schema(description = "Task value")
    private String value;

    @Schema(description = "Task status")
    private TaskStatus status;

    @Schema(description = "Sort order")
    private Integer sortOrder;

    @Schema(description = "Required task")
    private boolean requiredTask;

    @Schema(description = "Completed by")
    private String completedBy;

    @Schema(description = "Completed at")
    private LocalDateTime completedAt;

    @Schema(description = "List of attached files")
    private List<FileShowDTO> files = new ArrayList<>();

    public TaskShowDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TaskBaseShowDTO getTaskBase() {
        return taskBase;
    }

    public void setTaskBase(TaskBaseShowDTO taskBase) {
        this.taskBase = taskBase;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = trim(label);
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

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isRequiredTask() {
        return requiredTask;
    }

    public void setRequiredTask(boolean requiredTask) {
        this.requiredTask = requiredTask;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(String completedBy) {
        this.completedBy = trim(completedBy);
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public List<FileShowDTO> getFiles() {
        return files;
    }

    public void setFiles(List<FileShowDTO> files) {
        this.files = files != null ? files : new ArrayList<>();
    }

    private String trim(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}