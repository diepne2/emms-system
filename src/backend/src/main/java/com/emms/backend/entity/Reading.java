package com.emms.backend.entity;

import com.emms.backend.entity.abstracts.Audit;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "readings", indexes = {
        @Index(name = "idx_reading_meter", columnList = "meter_id"),
        @Index(name = "idx_reading_recorded_at", columnList = "recorded_at"),
        @Index(name = "idx_reading_triggered", columnList = "triggered_work_order_id")
})
public class Reading extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @Column(name = "value", nullable = false, precision = 19, scale = 2)
    private BigDecimal value;

    @Column(name = "delta_value", precision = 19, scale = 2)
    private BigDecimal deltaValue;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "triggered_work_order_id")
    private Long triggeredWorkOrderId;

    @Column(name = "triggered", nullable = false)
    private boolean triggered = false;

    public Reading() {
    }

    @PrePersist
    @PreUpdate
    public void normalize() {
        if (note != null) {
            note = note.trim();
            if (note.isEmpty()) {
                note = null;
            }
        }

        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }

        if (value == null) {
            throw new IllegalArgumentException("Reading value must not be null");
        }

        if (meter == null) {
            throw new IllegalArgumentException("Meter must not be null");
        }

        if (deltaValue == null) {
            deltaValue = BigDecimal.ZERO;
        }
    }

    public Long getId() {
        return id;
    }

    public Meter getMeter() {
        return meter;
    }

    public BigDecimal getValue() {
        return value;
    }

    public BigDecimal getDeltaValue() {
        return deltaValue;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public String getNote() {
        return note;
    }

    public Long getTriggeredWorkOrderId() {
        return triggeredWorkOrderId;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMeter(Meter meter) {
        this.meter = meter;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public void setDeltaValue(BigDecimal deltaValue) {
        this.deltaValue = deltaValue;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setTriggeredWorkOrderId(Long triggeredWorkOrderId) {
        this.triggeredWorkOrderId = triggeredWorkOrderId;
    }

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }
}