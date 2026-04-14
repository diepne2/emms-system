package com.emms.backend.dto.task;

import com.emms.backend.dto.asset.AssetSummaryDTO;
import com.emms.backend.dto.audit.AuditShowDTO;
import com.emms.backend.dto.user.UserSummaryDTO;
import com.emms.backend.entity.TaskBase;

public class TaskBaseShowDTO extends AuditShowDTO {

    private Long id;
    private String label;
    private TaskBase.TaskType taskType;
    private String description;
    private AssetSummaryDTO asset;
    private UserSummaryDTO createdByUser;
    private boolean active;

    public TaskBaseShowDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public AssetSummaryDTO getAsset() {
        return asset;
    }

    public void setAsset(AssetSummaryDTO asset) {
        this.asset = asset;
    }

    public UserSummaryDTO getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(UserSummaryDTO createdByUser) {
        this.createdByUser = createdByUser;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    private String trim(String v) {
        return v == null ? null : v.trim();
    }
}