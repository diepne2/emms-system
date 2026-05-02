package com.emms.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
public class Request {

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED,
        CANCELLED,
        RESOLVED,
        OPEN,
        WAITING,
        ACCEPTED
    }

    public enum Priority {
        NONE,
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 30)
    private Priority priority;

    @Column(name = "cancelled", nullable = false)
    private boolean cancelled = false;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Request() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        this.title = normalize(this.title);
        this.description = normalize(this.description);
        this.cancellationReason = normalize(this.cancellationReason);

        if (this.priority == null) {
            this.priority = Priority.NONE;
        }
        if (this.status == null) {
            this.status = Status.PENDING;
        }
        if (this.title == null || this.title.isBlank()) {
            this.title = "Request";
        }

        syncCancellationState();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();

        this.title = normalize(this.title);
        this.description = normalize(this.description);
        this.cancellationReason = normalize(this.cancellationReason);

        if (this.title == null || this.title.isBlank()) {
            this.title = "Request";
        }

        syncCancellationState();
    }

    private void syncCancellationState() {
        if (!this.cancelled && this.status != Status.CANCELLED) {
            this.cancellationReason = null;
            return;
        }

        if (this.status == Status.CANCELLED) {
            this.cancelled = true;
        }

        if (this.cancelled && (this.cancellationReason == null || this.cancellationReason.isBlank())) {
            this.cancellationReason = "Request was cancelled";
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = normalize(title);
    }

    public Asset getAsset() {
        return asset;
    }
    
    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = normalize(description);
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        if (status == Status.CANCELLED) {
            this.cancelled = true;
        }
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean getCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        if (!cancelled && this.status == Status.CANCELLED) {
            this.status = Status.PENDING;
        }
        if (cancelled && (this.cancellationReason == null || this.cancellationReason.isBlank())) {
            this.cancellationReason = "Request was cancelled";
        }
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = normalize(cancellationReason);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}