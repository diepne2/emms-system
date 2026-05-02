package com.emms.backend.dto.labor;

import java.util.Date;

public class LaborShowDTO {

    private Long id;

    private Long workOrderId;
    private String workOrderCode;
    private String workOrderTitle;

    private Long assignedToId;
    private String assignedToName;

    private Long timeCategoryId;
    private String timeCategoryName;

    private Boolean includeToTotalTime;
    private Double hourlyRate;
    private Long duration;
    private Long cost;
    private Date startedAt;
    private Date endedAt;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getWorkOrderCode() {
        return workOrderCode;
    }

    public void setWorkOrderCode(String workOrderCode) {
        this.workOrderCode = workOrderCode;
    }

    public String getWorkOrderTitle() {
        return workOrderTitle;
    }

    public void setWorkOrderTitle(String workOrderTitle) {
        this.workOrderTitle = workOrderTitle;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public Long getTimeCategoryId() {
        return timeCategoryId;
    }

    public void setTimeCategoryId(Long timeCategoryId) {
        this.timeCategoryId = timeCategoryId;
    }

    public String getTimeCategoryName() {
        return timeCategoryName;
    }

    public void setTimeCategoryName(String timeCategoryName) {
        this.timeCategoryName = timeCategoryName;
    }

    public Boolean getIncludeToTotalTime() {
        return includeToTotalTime;
    }

    public void setIncludeToTotalTime(Boolean includeToTotalTime) {
        this.includeToTotalTime = includeToTotalTime;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getCost() {
        return cost;
    }

    public void setCost(Long cost) {
        this.cost = cost;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Date endedAt) {
        this.endedAt = endedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}