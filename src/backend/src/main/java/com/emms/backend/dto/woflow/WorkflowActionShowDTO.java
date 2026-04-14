package com.emms.backend.dto.woflow;

import com.emms.backend.dto.asset.AssetSummaryDTO;
import com.emms.backend.dto.audit.AuditShowDTO;
import com.emms.backend.dto.category.CategorySummaryDTO;
import com.emms.backend.dto.checklist.ChecklistSummaryDTO;
import com.emms.backend.dto.location.LocationSummaryDTO;
import com.emms.backend.dto.user.UserSummaryDTO;
import com.emms.backend.entity.enums.AssetStatus;
import com.emms.backend.entity.enums.Priority;
import com.emms.backend.entity.enums.WorkOrderAction;

public class WorkflowActionShowDTO extends AuditShowDTO {

    private Long id;

    private WorkOrderAction workOrderAction;
    private Priority priority;

    private AssetSummaryDTO asset;
    private LocationSummaryDTO location;
    private UserSummaryDTO assignedTo;

    private CategorySummaryDTO category;  
    private ChecklistSummaryDTO checklist;

    private String value;
    private AssetStatus assetStatus;
    private Integer numberValue;

    private boolean enabled;

    public WorkflowActionShowDTO() {}

    // ===== Getter / Setter =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkOrderAction getWorkOrderAction() {
        return workOrderAction;
    }

    public void setWorkOrderAction(WorkOrderAction workOrderAction) {
        this.workOrderAction = workOrderAction;
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

    public UserSummaryDTO getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UserSummaryDTO assignedTo) {
        this.assignedTo = assignedTo;
    }

    public CategorySummaryDTO getCategory() {
        return category;
    }

    public void setCategory(CategorySummaryDTO category) {
        this.category = category;
    }

    public ChecklistSummaryDTO getChecklist() {
        return checklist;
    }

    public void setChecklist(ChecklistSummaryDTO checklist) {
        this.checklist = checklist;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = trim(value);
    }

    public AssetStatus getAssetStatus() {
        return assetStatus;
    }

    public void setAssetStatus(AssetStatus assetStatus) {
        this.assetStatus = assetStatus;
    }

    public Integer getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(Integer numberValue) {
        this.numberValue = numberValue;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    // ===== Utils =====
    private String trim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}