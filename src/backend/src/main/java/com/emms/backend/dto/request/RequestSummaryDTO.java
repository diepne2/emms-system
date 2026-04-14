package com.emms.backend.dto.request;

import com.emms.backend.entity.Request;

import java.time.LocalDateTime;

public class RequestSummaryDTO {

    private Long requestId;

    private String locationName;
    private String requestPortalTitle;
    private String workOrderTitle;

    private Request.Status status;
    private Request.Priority priority;

    private boolean cancelled;
    private String cancellationReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RequestSummaryDTO() {
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = trim(locationName);
    }

    public String getRequestPortalTitle() {
        return requestPortalTitle;
    }

    public void setRequestPortalTitle(String requestPortalTitle) {
        this.requestPortalTitle = trim(requestPortalTitle);
    }

    public String getWorkOrderTitle() {
        return workOrderTitle;
    }

    public void setWorkOrderTitle(String workOrderTitle) {
        this.workOrderTitle = trim(workOrderTitle);
    }

    public Request.Status getStatus() {
        return status;
    }

    public void setStatus(Request.Status status) {
        this.status = status;
    }

    public Request.Priority getPriority() {
        return priority;
    }

    public void setPriority(Request.Priority priority) {
        this.priority = priority;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean getCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = trim(cancellationReason);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}