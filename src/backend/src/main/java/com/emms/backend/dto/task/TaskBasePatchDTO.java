package com.emms.backend.dto.task;

import com.emms.backend.entity.TaskBase;

public class TaskBasePatchDTO {

    private String label;
    private TaskBase.TaskType taskType;
    private String description;
    private Long assetId;
    private Boolean active;

    public TaskBasePatchDTO() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = trim(label);
    }

    public TaskBase.TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskBase.TaskType taskType) {
        this.taskType = taskType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = trim(description);
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    private String trim(String v) {
        return v == null ? null : v.trim();
    }
}