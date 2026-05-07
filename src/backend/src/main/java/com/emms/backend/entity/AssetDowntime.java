package com.emms.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "asset_downtimes", indexes = {
        @Index(name = "idx_asset_downtime_asset", columnList = "asset_id"),
        @Index(name = "idx_asset_downtime_work_order", columnList = "work_order_id"),
        @Index(name = "idx_asset_downtime_start", columnList = "starts_on"),
        @Index(name = "idx_asset_downtime_end", columnList = "ends_on")
})
public class AssetDowntime {

    public enum DowntimeReason {
        BREAKDOWN,
        MAINTENANCE,
        POWER_FAILURE,
        CALIBRATION,
        OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    private DowntimeReason reason = DowntimeReason.BREAKDOWN;

    @Column(name = "starts_on", nullable = false)
    private LocalDateTime startsOn;

    @Column(name = "ends_on")
    private LocalDateTime endsOn;

    @Column(name = "duration_seconds", nullable = false)
    private Long durationSeconds = 0L;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AssetDowntime() {
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        normalizeAndRecalculate();
    }

    @PreUpdate
    public void preUpdate() {
        normalizeAndRecalculate();
    }

    private void normalizeAndRecalculate() {
        if (asset == null) {
            throw new IllegalArgumentException("asset không được null");
        }
        if (startsOn == null) {
            throw new IllegalArgumentException("startsOn không được null");
        }
        if (reason == null) {
            reason = DowntimeReason.BREAKDOWN;
        }
        note = trim(note);

        if (endsOn != null) {
            if (endsOn.isBefore(startsOn)) {
                throw new IllegalArgumentException("endsOn phải >= startsOn");
            }
            durationSeconds = ChronoUnit.SECONDS.between(startsOn, endsOn);
        } else if (durationSeconds == null || durationSeconds < 0) {
            durationSeconds = 0L;
        }
    }

    public boolean isOpen() {
        return endsOn == null;
    }

    public boolean isClosed() {
        return endsOn != null;
    }

    public void close(LocalDateTime endTime) {
        if (endTime == null) {
            throw new IllegalArgumentException("endTime không được null");
        }
        if (endTime.isBefore(startsOn)) {
            throw new IllegalArgumentException("endTime phải >= startsOn");
        }
        this.endsOn = endTime;
        this.durationSeconds = ChronoUnit.SECONDS.between(startsOn, endTime);
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("asset không được null");
        }
        this.asset = asset;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public DowntimeReason getReason() {
        return reason;
    }

    public void setReason(DowntimeReason reason) {
        this.reason = reason;
    }

    public LocalDateTime getStartsOn() {
        return startsOn;
    }

    public void setStartsOn(LocalDateTime startsOn) {
        this.startsOn = startsOn;
    }

    public LocalDateTime getEndsOn() {
        return endsOn;
    }

    public void setEndsOn(LocalDateTime endsOn) {
        this.endsOn = endsOn;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

}