package com.emms.backend.dto.request;

import com.emms.backend.entity.Request;

import java.time.LocalDateTime;

public class RequestShowDTO {

    private Long requestId;

    private Long locationId;
    private String locationName;

    private Long requestPortalId;
    private String requestPortalTitle;

    private Long workOrderId;
    private String workOrderTitle;

    private Request.Status status;
    private Request.Priority priority;

    private boolean cancelled;
    private String cancellationReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RequestShowDTO() {
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = trim(locationName);
    }

    public Long getRequestPortalId() {
        return requestPortalId;
    }

    public void setRequestPortalId(Long requestPortalId) {
        this.requestPortalId = requestPortalId;
    }

    public String getRequestPortalTitle() {
        return requestPortalTitle;
    }

    public void setRequestPortalTitle(String requestPortalTitle) {
        this.requestPortalTitle = trim(requestPortalTitle);
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
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