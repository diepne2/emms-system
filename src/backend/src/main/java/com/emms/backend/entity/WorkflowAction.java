package com.emms.backend.entity;

import com.emms.backend.entity.enums.AssetStatus;
import com.emms.backend.entity.enums.Priority;
import com.emms.backend.entity.enums.WorkOrderAction;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "workflow_actions", indexes = {
        @Index(name = "idx_workflow_action_workflow", columnList = "workflow_id"),
        @Index(name = "idx_workflow_action_type", columnList = "work_order_action"),
        @Index(name = "idx_workflow_action_enabled", columnList = "enabled")
})
public class WorkflowAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // owning side of 1-1
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_id", nullable = false, unique = true)
    private Workflow workflow;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_order_action", length = 50)
    private WorkOrderAction workOrderAction;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User user;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_category_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private WorkOrderCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Checklist checklist;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_status", length = 30)
    private AssetStatus assetStatus;

    @Column(name = "value", length = 500)
    private String value;

    @Column(name = "number_value")
    private Integer numberValue;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    public WorkflowAction() {
    }

    @PrePersist
    @PreUpdate
    public void normalize() {
        this.value = trim(this.value);

        if (this.numberValue != null && this.numberValue < 0) {
            throw new IllegalArgumentException("numberValue không được âm");
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }



    public boolean hasWorkOrderMutation() {
        return this.workOrderAction != null
                || this.priority != null
                || this.category != null
                || this.checklist != null
                || this.assetStatus != null;
    }

    public Long getId() {
        return id;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
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

    public AssetStatus getAssetStatus() {
        return assetStatus;
    }

    public void setAssetStatus(AssetStatus assetStatus) {
        this.assetStatus = assetStatus;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}