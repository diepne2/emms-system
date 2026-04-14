package com.emms.backend.dto.woMeterTrigger;

import com.emms.backend.dto.workorderBase.WorkOrderBaseShowDTO;
import com.emms.backend.entity.enums.WorkOrderMeterTriggerCondition;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "DTO hiển thị Work Order Meter Trigger")
public class WorkOrderMeterTriggerShowDTO extends WorkOrderBaseShowDTO {

    private Long id;
    private boolean recurrent;
    private boolean active;
    private String name;
    private WorkOrderMeterTriggerCondition triggerCondition;
    private BigDecimal triggerValue;
    private Integer cooldownMinutes;
    private LocalDateTime lastTriggeredAt;

    private Long meterId;
    private String meterName;

    public WorkOrderMeterTriggerShowDTO() {
    }

    public Long getId() {
        return id;
    }

    public boolean isRecurrent() {
        return recurrent;
    }

    public boolean isActive() {
        return active;
    }

    public String getName() {
        return name;
    }

    public WorkOrderMeterTriggerCondition getTriggerCondition() {
        return triggerCondition;
    }

    public BigDecimal getTriggerValue() {
        return triggerValue;
    }

    public Integer getCooldownMinutes() {
        return cooldownMinutes;
    }

    public LocalDateTime getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public Long getMeterId() {
        return meterId;
    }

    public String getMeterName() {
        return meterName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRecurrent(boolean recurrent) {
        this.recurrent = recurrent;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTriggerCondition(WorkOrderMeterTriggerCondition triggerCondition) {
        this.triggerCondition = triggerCondition;
    }

    public void setTriggerValue(BigDecimal triggerValue) {
        this.triggerValue = triggerValue;
    }

    public void setCooldownMinutes(Integer cooldownMinutes) {
        this.cooldownMinutes = cooldownMinutes;
    }

    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
    }

    public void setMeterId(Long meterId) {
        this.meterId = meterId;
    }

    public void setMeterName(String meterName) {
        this.meterName = meterName;
    }
}