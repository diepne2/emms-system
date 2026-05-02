package com.emms.backend.dto.reading;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReadingShowDTO {

    private Long id;
    private Long meterId;
    private String meterName;
    private BigDecimal value;
    private BigDecimal deltaValue;
    private LocalDateTime recordedAt;
    private String note;
    private boolean triggered;
    private Long triggeredWorkOrderId;

    public ReadingShowDTO() {
    }

    public Long getId() {
        return id;
    }

    public Long getMeterId() {
        return meterId;
    }

    public String getMeterName() {
        return meterName;
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

    public boolean isTriggered() {
        return triggered;
    }

    public Long getTriggeredWorkOrderId() {
        return triggeredWorkOrderId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMeterId(Long meterId) {
        this.meterId = meterId;
    }

    public void setMeterName(String meterName) {
        this.meterName = meterName;
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

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }

    public void setTriggeredWorkOrderId(Long triggeredWorkOrderId) {
        this.triggeredWorkOrderId = triggeredWorkOrderId;
    }
}