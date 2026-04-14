package com.emms.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "readings", indexes = {
        @Index(name = "idx_reading_meter", columnList = "meter_id"),
        @Index(name = "idx_reading_recorded_at", columnList = "recorded_at")
})
public class Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reading_id")
    private Long readingId;

    @Column(name = "value", nullable = false)
    private Double value;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meter_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Meter meter;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Reading() {
    }

    public Reading(Double value, Meter meter, LocalDateTime recordedAt) {
        this.value = value;
        this.meter = meter;
        this.recordedAt = recordedAt;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.recordedAt == null) {
            this.recordedAt = now;
        }
        this.createdAt = now;
        this.updatedAt = now;
        validateData();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        validateData();
    }

    private void validateData() {
        if (this.value == null) {
            throw new IllegalArgumentException("Giá trị đo không được null");
        }
        if (this.value < 0) {
            throw new IllegalArgumentException("Giá trị đo không được âm");
        }
        if (this.meter == null) {
            throw new IllegalArgumentException("Reading phải thuộc một Meter");
        }
        if (this.recordedAt == null) {
            throw new IllegalArgumentException("recordedAt không được null");
        }
    }

    public Long getReadingId() {
        return readingId;
    }

    public void setReadingId(Long readingId) {
        this.readingId = readingId;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Meter getMeter() {
        return meter;
    }

    public void setMeter(Meter meter) {
        this.meter = meter;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}