package com.emms.backend.dto.labor;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public class LaborCreateDTO {

    @NotNull(message = "workOrderId is required")
    private Long workOrderId;

    private Long assignedToId;

    private Long timeCategoryId;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Ho_Chi_Minh"
    )
    @NotNull(message = "startedAt is required")
    private Date startedAt;

    @Min(value = 0, message = "hourlyRate must not be negative")
    private Double hourlyRate = 0.0;

    @Min(value = 0, message = "duration must not be negative")
    private Long duration = 0L;

    private Boolean includeToTotalTime = true;

    private String status = "RUNNING";

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public Long getTimeCategoryId() {
        return timeCategoryId;
    }

    public void setTimeCategoryId(Long timeCategoryId) {
        this.timeCategoryId = timeCategoryId;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
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

    public Boolean getIncludeToTotalTime() {
        return includeToTotalTime;
    }

    public void setIncludeToTotalTime(Boolean includeToTotalTime) {
        this.includeToTotalTime = includeToTotalTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}