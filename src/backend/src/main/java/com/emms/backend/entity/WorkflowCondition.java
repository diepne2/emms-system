package com.emms.backend.entity;

import com.emms.backend.entity.enums.Priority;
import com.emms.backend.entity.enums.Status;
import com.emms.backend.entity.enums.WorkOrderCondition;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Entity
@Table(name = "workflow_conditions", indexes = {
        @Index(name = "idx_workflow_condition_workflow", columnList = "workflow_id"),
        @Index(name = "idx_workflow_condition_type", columnList = "condition_type"),
        @Index(name = "idx_workflow_condition_status", columnList = "work_order_status")
})
public class WorkflowCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // parent workflow
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 50)
    private WorkOrderCondition workOrderCondition;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_order_status", length = 30)
    private Status workOrderStatus;

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

    @Column(name = "created_time_start")
    private Integer createdTimeStart;

    @Column(name = "created_time_end")
    private Integer createdTimeEnd;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "value", length = 500)
    private String value;

    @Column(name = "number_value")
    private Integer numberValue;

    public WorkflowCondition() {
    }

    @PrePersist
    @PreUpdate
    public void normalize() {
        this.value = trim(this.value);

        if (createdTimeStart != null && (createdTimeStart < 0 || createdTimeStart > 23)) {
            throw new IllegalArgumentException("createdTimeStart phải từ 0 đến 23");
        }

        if (createdTimeEnd != null && (createdTimeEnd < 0 || createdTimeEnd > 23)) {
            throw new IllegalArgumentException("createdTimeEnd phải từ 0 đến 23");
        }

        if (createdTimeStart != null && createdTimeEnd != null && createdTimeStart > createdTimeEnd) {
            throw new IllegalArgumentException("createdTimeStart không được lớn hơn createdTimeEnd");
        }
    }

    public boolean isMetForWorkOrder(WorkOrder workOrder) {
        if (workOrder == null || workOrderCondition == null) {
            return false;
        }

        switch (workOrderCondition) {
            case PRIORITY_IS:
                return priority != null
                        && workOrder.getPriority() != null
                        && workOrder.getPriority().name().equals(priority.name());

            case STATUS_IS:
                return workOrderStatus != null
                        && workOrder.getStatus() != null
                        && workOrder.getStatus().name().equals(workOrderStatus.name());

            case ASSET_IS:
                return asset != null
                        && workOrder.getAssetName() != null
                        && asset.getName() != null
                        && workOrder.getAssetName().equalsIgnoreCase(asset.getName());

            case LOCATION_IS:
                return location != null
                        && workOrder.getLocationName() != null
                        && location.getName() != null
                        && workOrder.getLocationName().equalsIgnoreCase(location.getName());

 
            case USER_IS:
                return user != null
                        && workOrder.getPrimaryUser() != null
                        && workOrder.getPrimaryUser().equalsIgnoreCase(user.getUsername());

            case CATEGORY_IS:
                return category != null
                        && workOrder.getCategory() != null
                        && category.getName() != null
                        && workOrder.getCategory().equalsIgnoreCase(category.getName());

            case CREATED_AT_BETWEEN:
                if (workOrder.getDateCreated() == null || createdTimeStart == null || createdTimeEnd == null) {
                    return false;
                }
                int hour = workOrder.getDateCreated().getHour();
                return hour >= createdTimeStart && hour <= createdTimeEnd;

            case DUE_DATE_BETWEEN:
                return workOrder.getDueDate() != null
                        && startDate != null
                        && endDate != null
                        && !workOrder.getDueDate().isBefore(startDate)
                        && !workOrder.getDueDate().isAfter(endDate);

            case DUE_DATE_AFTER:
                return workOrder.getDueDate() != null
                        && endDate != null
                        && workOrder.getDueDate().isAfter(endDate);

            default:
                return false;
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
}