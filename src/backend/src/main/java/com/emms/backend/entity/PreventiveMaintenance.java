package com.emms.backend.entity;

import com.emms.backend.entity.abstracts.WorkOrderBase;
import com.emms.backend.entity.enums.PermissionEntity;
import com.emms.backend.entity.enums.Priority;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "preventive_maintenances",
        indexes = {
                @Index(name = "idx_pm_code", columnList = "code"),
                @Index(name = "idx_pm_title", columnList = "title"),
                @Index(name = "idx_pm_asset", columnList = "asset_id"),
                @Index(name = "idx_pm_active", columnList = "active")
        }
)
public class PreventiveMaintenance extends WorkOrderBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "code", unique = true, length = 100)
    private String code;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "demo", nullable = false)
    private boolean demo = false;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    public PreventiveMaintenance() {
    }

    @Override
    protected void normalize() {
        super.normalize();
        this.code = trim(this.code);
        validatePm();
    }

    private void validatePm() {
        if (this.code != null && this.code.length() > 100) {
            throw new IllegalArgumentException("Mã kế hoạch bảo trì không được vượt quá 100 ký tự");
        }
    }

    public boolean canBeEditedBy(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        return user.getRole().hasPermission(PermissionEntity.MAINTENANCE_PLAN_UPDATE);
    }

    public boolean canBeViewedBy(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        return user.getRole().hasPermission(PermissionEntity.MAINTENANCE_PLAN_VIEW);
    }

    public boolean canGenerateWorkOrder(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        return user.getRole().hasPermission(PermissionEntity.MAINTENANCE_PLAN_GENERATE_WO);
    }

    public boolean hasSchedule() {
        return this.schedule != null;
    }

    public boolean isActivePlan() {
        return this.active;
    }

    public String getName() {
        return this.title;
    }

    public void setName(String name) {
        this.title = trim(name);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = trim(code);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDemo() {
        return demo;
    }

    public void setDemo(boolean demo) {
        this.demo = demo;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public void setTitle(String title) {
        this.title = trim(title);
    }

    public void setDescription(String description) {
        this.description = trim(description);
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public void setRequestedBy(User requestedBy) {
        this.requestedBy = requestedBy;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public void setDueDate(java.time.LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public void setEstimatedHours(Double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public void setActualHours(Double actualHours) {
        this.actualHours = actualHours;
    }

    public void setRequiredSignature(boolean requiredSignature) {
        this.requiredSignature = requiredSignature;
    }
}