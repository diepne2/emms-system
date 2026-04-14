package com.emms.backend.dto.woMeterTrigger;

import com.emms.backend.dto.workorderBase.WorkOrderBaseDTO;
import com.emms.backend.entity.enums.WorkOrderMeterTriggerCondition;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO tạo / cập nhật Work Order Meter Trigger")
public class WorkOrderMeterTriggerDTO extends WorkOrderBaseDTO {

    @Schema(description = "Có lặp lại hay không", example = "true")
    private Boolean recurrent;

    @Schema(description = "Trigger có active hay không", example = "true")
    private Boolean active;

    @Schema(description = "Tên trigger", example = "Trigger theo số giờ")
    private String name;

    @Schema(description = "Điều kiện trigger", example = "GREATER_THAN")
    private WorkOrderMeterTriggerCondition triggerCondition;

    @Schema(description = "Ngưỡng trigger", example = "100")
    private BigDecimal triggerValue;

    @Schema(description = "Cooldown (phút)", example = "60")
    private Integer cooldownMinutes;

    @Schema(description = "ID meter", example = "1")
    private Long meterId;

    public WorkOrderMeterTriggerDTO() {
    }

    public Boolean getRecurrent() {
        return recurrent;
    }

    public void setRecurrent(Boolean recurrent) {
        this.recurrent = recurrent;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = trim(name);
    }

    public WorkOrderMeterTriggerCondition getTriggerCondition() {
        return triggerCondition;
    }

    public void setTriggerCondition(WorkOrderMeterTriggerCondition triggerCondition) {
        this.triggerCondition = triggerCondition;
    }

    public BigDecimal getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(BigDecimal triggerValue) {
        this.triggerValue = triggerValue;
    }

    public Integer getCooldownMinutes() {
        return cooldownMinutes;
    }

    public void setCooldownMinutes(Integer cooldownMinutes) {
        this.cooldownMinutes = cooldownMinutes;
    }

    public Long getMeterId() {
        return meterId;
    }

    public void setMeterId(Long meterId) {
        this.meterId = meterId;
    }

    protected String trim(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}