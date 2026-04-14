package com.emms.backend.dto.woflow;

import com.emms.backend.dto.asset.AssetSummaryDTO;
import com.emms.backend.dto.audit.AuditShowDTO;
import com.emms.backend.dto.category.CategorySummaryDTO;
import com.emms.backend.dto.location.LocationSummaryDTO;
import com.emms.backend.dto.user.UserSummaryDTO;
import com.emms.backend.entity.enums.Priority;
import com.emms.backend.entity.enums.Status;
import com.emms.backend.entity.enums.WorkOrderCondition;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Workflow condition configuration details")
public class WorkflowConditionShowDTO extends AuditShowDTO {

    @Schema(description = "Unique identifier")
    private Long id;

    @Schema(description = "Work order condition")
    private WorkOrderCondition workOrderCondition;

    @Schema(description = "Priority requirement")
    private Priority priority;

    @Schema(description = "Associated asset")
    private AssetSummaryDTO asset;

    @Schema(description = "Associated location")
    private LocationSummaryDTO location;

    @Schema(description = "Associated user")
    private UserSummaryDTO user;

    @Schema(description = "Work order category")
    private CategorySummaryDTO category;

    @Schema(description = "Work order status requirement")
    private Status workOrderStatus;

    @Schema(description = "Created time range start")
    private Integer createdTimeStart;

    @Schema(description = "Created time range end")
    private Integer createdTimeEnd;

    @Schema(description = "Start date for condition")
    private LocalDate startDate;

    @Schema(description = "End date for condition")
    private LocalDate endDate;

    @Schema(description = "String value for the condition")
    private String value;

    @Schema(description = "Numeric value for the condition")
    private Integer numberValue;

    public WorkflowConditionShowDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public AssetSummaryDTO getAsset() {
        return asset;
    }

    public void setAsset(AssetSummaryDTO asset) {
        this.asset = asset;
    }

    public LocationSummaryDTO getLocation() {
        return location;
    }

    public void setLocation(LocationSummaryDTO location) {
        this.location = location;
    }

    public UserSummaryDTO getUser() {
        return user;
    }

    public void setUser(UserSummaryDTO user) {
        this.user = user;
    }

    public CategorySummaryDTO getCategory() {
        return category;
    }

    public void setCategory(CategorySummaryDTO category) {
        this.category = category;
    }

    public Status getWorkOrderStatus() {
        return workOrderStatus;
    }

    public void setWorkOrderStatus(Status workOrderStatus) {
        this.workOrderStatus = workOrderStatus;
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
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
