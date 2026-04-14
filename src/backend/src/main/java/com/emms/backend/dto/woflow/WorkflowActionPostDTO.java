package com.emms.backend.dto.woflow;
import com.emms.backend.entity.Asset;
import com.emms.backend.entity.Checklist;
import com.emms.backend.entity.Location;
import com.emms.backend.entity.User;
import com.emms.backend.entity.WorkOrderCategory;
import com.emms.backend.entity.enums.AssetStatus;
import com.emms.backend.entity.enums.Priority;
import com.emms.backend.entity.enums.WorkOrderAction;

public class WorkflowActionPostDTO {

    private WorkOrderAction workOrderAction;
    private Priority priority;
    private Asset asset;
    private Location location;
    private User user;
    private WorkOrderCategory category;
    private Checklist checklist;
    private String value;
    private AssetStatus assetStatus;
    private Integer numberValue;
    private Boolean enabled;

    public WorkflowActionPostDTO() {
    }

    public WorkflowActionPostDTO(WorkOrderAction workOrderAction,
                                 Priority priority,
                                 Asset asset,
                                 Location location,
                                 User user,
                                 WorkOrderCategory category,
                                 Checklist checklist,
                                 String value,
                                 AssetStatus assetStatus,
                                 Integer numberValue,
                                 Boolean enabled) {
        this.workOrderAction = workOrderAction;
        this.priority = priority;
        this.asset = asset;
        this.location = location;
        this.user = user;
        this.category = category;
        this.checklist = checklist;
        this.value = trim(value);
        this.assetStatus = assetStatus;
        this.numberValue = numberValue;
        this.enabled = enabled;
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

    public Checklist getChecklist() {
        return checklist;
    }

    public void setChecklist(Checklist checklist) {
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
