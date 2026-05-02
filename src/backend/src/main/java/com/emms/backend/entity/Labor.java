package com.emms.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.emms.backend.entity.abstracts.Time;
import com.emms.backend.entity.enums.TimeStatus;
import com.emms.backend.utils.Helper;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "labors", indexes = {
        @Index(name = "idx_labor_work_order", columnList = "work_order_id"),
        @Index(name = "idx_labor_assigned_to", columnList = "assigned_to"),
        @Index(name = "idx_labor_started_at", columnList = "started_at"),
        @Index(name = "idx_labor_status", columnList = "status"),
        @Index(name = "idx_labor_time_category", columnList = "time_category_id")
})
public class Labor extends Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "include_to_total_time", nullable = false)
    private boolean includeToTotalTime = true;

    @Column(name = "logged", nullable = false)
    private boolean logged = false;

    @Column(name = "hourly_rate", nullable = false)
    private Double hourlyRate = 0.0;

    @Column(name = "is_demo", nullable = false)
    private boolean demo = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "started_at", nullable = false)
    private Date startedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TimeStatus status = TimeStatus.STOPPED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_category_id")
    private TimeCategory timeCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private WorkOrder workOrder;

    public Labor() {
    }

    public Labor(User assignedTo,
                 Double hourlyRate,
                 Date startedAt,
                 WorkOrder workOrder,
                 boolean logged,
                 TimeStatus status) {
        this.assignedTo = assignedTo;
        this.hourlyRate = hourlyRate;
        this.startedAt = startedAt;
        this.workOrder = workOrder;
        this.logged = logged;
        this.status = status;
    }

    @PrePersist
    @PreUpdate
    protected void validateAndNormalize() {
        if (this.hourlyRate == null) {
            this.hourlyRate = 0.0;
        }

        if (this.hourlyRate < 0) {
            throw new IllegalArgumentException("hourlyRate không được âm");
        }

        if (this.startedAt == null) {
            throw new IllegalArgumentException("startedAt không được để trống");
        }

        if (this.status == null) {
            this.status = TimeStatus.STOPPED;
        }

        if (this.workOrder == null) {
            throw new IllegalArgumentException("Labor phải thuộc một WorkOrder");
        }

        if (this.getDuration() < 0) {
            throw new IllegalArgumentException("duration không được âm");
        }
    }

    public long getCost() {
        long duration = this.getDuration();

        if (duration <= 0 || this.hourlyRate == null || this.hourlyRate <= 0) {
            return 0L;
        }

        return (long) (this.hourlyRate * duration) / 3600;
    }

    public Date getEndedAt() {
        long duration = this.getDuration();

        if (this.startedAt == null) {
            return null;
        }

        if (duration <= 0) {
            return this.startedAt;
        }

        if (duration > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("duration quá lớn để tính endedAt");
        }

        return Helper.addSeconds(this.startedAt, (int) duration);
    }

    public boolean isRunning() {
        return this.status == TimeStatus.RUNNING;
    }

    public boolean isStopped() {
        return this.status == TimeStatus.STOPPED;
    }

    public boolean belongsToWorkOrder(WorkOrder workOrder) {
        if (this.workOrder == null || workOrder == null) {
            return false;
        }

        if (this.workOrder.getId() == null || workOrder.getId() == null) {
            return false;
        }

        return this.workOrder.getId().equals(workOrder.getId());
    }

    public static long getTotalWorkDuration(List<Labor> labors) {
        if (labors == null || labors.isEmpty()) {
            return 0L;
        }

        List<Labor> validLabors = labors.stream()
                .filter(labor -> labor != null)
                .filter(Labor::isIncludeToTotalTime)
                .filter(labor -> labor.getStartedAt() != null)
                .filter(labor -> labor.getDuration() > 0)
                .sorted(Comparator.comparing(Labor::getStartedAt))
                .collect(Collectors.toCollection(ArrayList::new));

        if (validLabors.isEmpty()) {
            return 0L;
        }

        long totalDuration = 0L;
        Date previousEnd = null;

        for (Labor labor : validLabors) {
            Date currentStart = labor.getStartedAt();
            Date currentEnd = labor.getEndedAt();

            if (currentStart == null || currentEnd == null || currentEnd.before(currentStart)) {
                continue;
            }

            if (previousEnd != null && currentStart.before(previousEnd)) {
                if (currentEnd.after(previousEnd)) {
                    totalDuration += Math.max(0L, (currentEnd.getTime() - previousEnd.getTime()) / 1000L);
                    previousEnd = currentEnd;
                }
            } else {
                totalDuration += labor.getDuration();
                previousEnd = currentEnd;
            }
        }

        return totalDuration;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public boolean isIncludeToTotalTime() {
        return includeToTotalTime;
    }

    public void setIncludeToTotalTime(boolean includeToTotalTime) {
        this.includeToTotalTime = includeToTotalTime;
    }

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public boolean isDemo() {
        return demo;
    }

    public void setDemo(boolean demo) {
        this.demo = demo;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public TimeStatus getStatus() {
        return status;
    }

    public void setStatus(TimeStatus status) {
        this.status = status;
    }

    public TimeCategory getTimeCategory() {
        return timeCategory;
    }

    public void setTimeCategory(TimeCategory timeCategory) {
        this.timeCategory = timeCategory;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }
}