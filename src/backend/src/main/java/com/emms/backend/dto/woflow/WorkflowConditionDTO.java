package com.emms.backend.dto.woflow;



import com.emms.backend.dto.IdDTO;
import com.emms.backend.entity.*;
import com.emms.backend.entity.enums.*;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public class WorkflowConditionDTO {

    private WorkOrderCondition workOrderCondition;
    private Priority priority;
    private Status workOrderStatus;

    @Schema(implementation = IdDTO.class)
    private Asset asset;

    @Schema(implementation = IdDTO.class)
    private Location location;

    @Schema(implementation = IdDTO.class)
    private User user;

    @Schema(implementation = IdDTO.class)
    private WorkOrderCategory category;

    private Integer createdTimeStart;
    private Integer createdTimeEnd;

    private LocalDate startDate;
    private LocalDate endDate;

    private String value;
    private Integer numberValue;

    public WorkflowConditionDTO() {
    }

    public WorkOrderCondition getWorkOrderCondition() {
        return workOrderCondition;
    }

    public void setWorkOrderCondition(WorkOrderCondition workOrderCondition) {
        this.workOrderCondition = workOrderCondition;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getWorkOrderStatus() {
        return workOrderStatus;
    }

    public void setWorkOrderStatus(Status workOrderStatus) {
        this.workOrderStatus = workOrderStatus;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public WorkOrderCategory getCategory() {
        return category;
    }

    public void setCategory(WorkOrderCategory category) {
        this.category = category;
    }

    public Integer getCreatedTimeStart() {
        return createdTimeStart;
    }

    public void setCreatedTimeStart(Integer createdTimeStart) {
        this.createdTimeStart = createdTimeStart;
    }

    public Integer getCreatedTimeEnd() {
        return createdTimeEnd;
    }

    public void setCreatedTimeEnd(Integer createdTimeEnd) {
        this.createdTimeEnd = createdTimeEnd;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = trim(value);
    }

    public Integer getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(Integer numberValue) {
        this.numberValue = numberValue;
    }

    private String trim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}