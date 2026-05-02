package com.emms.backend.entity.abstracts;

import com.emms.backend.entity.Asset;
import com.emms.backend.entity.User;
import com.emms.backend.entity.enums.Priority;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class WorkOrderBase {

    @Column(name = "title", nullable = false, length = 255)
    protected String title;

    @Column(name = "description", length = 2000)
    protected String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    protected Priority priority = Priority.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    protected Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by")
    protected User requestedBy;

  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    protected User assignedTo;

    @Column(name = "due_date")
    protected LocalDateTime dueDate;

    @Column(name = "estimated_hours")
    protected Double estimatedHours;

    @Column(name = "actual_hours")
    protected Double actualHours;

    @Column(name = "required_signature")
    protected boolean requiredSignature = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;

    @PrePersist
    protected void onCreateBase() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        normalize();
    }

    @PreUpdate
    protected void onUpdateBase() {
        this.updatedAt = LocalDateTime.now();
        normalize();
    }

    protected void normalize() {
        title = trim(title);
        description = trim(description);

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title không được để trống");
        }

        if (estimatedHours != null && estimatedHours < 0) {
            throw new IllegalArgumentException("estimatedHours không được âm");
        }

        if (actualHours != null && actualHours < 0) {
            throw new IllegalArgumentException("actualHours không được âm");
        }
    }

    protected String trim(String value) {
        return value == null ? null : value.trim();
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDateTime.now());
    }

    public boolean hasAssignment() {
        return assignedTo != null;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public Asset getAsset() { return asset; }
    public User getRequestedBy() { return requestedBy; }
    public User getAssignedTo() { return assignedTo; }
    public LocalDateTime getDueDate() { return dueDate; }
    public Double getEstimatedHours() { return estimatedHours; }
    public Double getActualHours() { return actualHours; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}