package com.emms.backend.service;

public class AutoCreateWorkOrderRequest {

    private String title;
    private String description;
    private Long assetId;
    private Long locationId;
    private Object priority;
    private Long assignedToId;
    private String sourceType;
    private Long sourceId;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getAssetId() {
        return assetId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public Object getPriority() {
        return priority;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public void setPriority(Object priority) {
        this.priority = priority;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }
}