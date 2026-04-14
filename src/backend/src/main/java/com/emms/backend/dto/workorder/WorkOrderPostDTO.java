package com.emms.backend.dto.workorder;

import com.emms.backend.entity.WorkOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "DTO for creating a new work order")
public class WorkOrderPostDTO {

    private String title;
    private String description;
    private WorkOrder.WorkOrderPriority priority;
    private LocalDate dueDate;
    private Double estimatedDuration;
    private Boolean requiresSignature;
    private String category;
    private String locationName;
    private String teamName;
    private String primaryUser;
    private Long assignedToId;
    private Long assetId;
    private String contractors;

    public WorkOrderPostDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = trim(title);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = trim(description);
    }

    public WorkOrder.WorkOrderPriority getPriority() {
        return priority;
    }

    public void setPriority(WorkOrder.WorkOrderPriority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Double getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Double estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public Boolean getRequiresSignature() {
        return requiresSignature;
    }

    public void setRequiresSignature(Boolean requiresSignature) {
        this.requiresSignature = requiresSignature;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = trim(category);
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = trim(locationName);
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = trim(teamName);
    }

    public String getPrimaryUser() {
        return primaryUser;
    }

    public void setPrimaryUser(String primaryUser) {
        this.primaryUser = trim(primaryUser);
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getContractors() {
        return contractors;
    }

    public void setContractors(String contractors) {
        this.contractors = trim(contractors);
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}