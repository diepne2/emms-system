package com.emms.backend.dto.woflow;


import com.emms.backend.dto.IdDTO;
import com.emms.backend.entity.enums.Priority;
import com.emms.backend.entity.enums.Status;
import com.emms.backend.entity.enums.WorkOrderCondition;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "DTO for creating a new workflow condition")
public class WorkflowConditionPostDTO {

    @NotNull(message = "workOrderCondition is required")
    private WorkOrderCondition workOrderCondition;

    private Priority priority;
    private Status workOrderStatus;

    @Schema(implementation = IdDTO.class)
    private IdDTO asset;

    @Schema(implementation = IdDTO.class)
    private IdDTO location;

    @Schema(implementation = IdDTO.class)
    private IdDTO user;

    @Schema(implementation = IdDTO.class)
    private IdDTO category;

    private Integer createdTimeStart;
    private Integer createdTimeEnd;

    private LocalDate startDate;
    private LocalDate endDate;

    @Size(max = 500)
    private String value;

    private Integer numberValue;

    // ===== Constructor =====
    public WorkflowConditionPostDTO() {
    }

    // ===== Getter / Setter =====

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

    public IdDTO getAsset() {
        return asset;
    }

    public void setAsset(IdDTO asset) {
        this.asset = asset;
    }

    public IdDTO getLocation() {
        return location;
    }

    public void setLocation(IdDTO location) {
        this.location = location;
    }

    public IdDTO getUser() {
        return user;
    }

    public void setUser(IdDTO user) {
        this.user = user;
    }

    public IdDTO getCategory() {
        return category;
    }

    public void setCategory(IdDTO category) {
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

    // ===== Utils =====
    private String trim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}