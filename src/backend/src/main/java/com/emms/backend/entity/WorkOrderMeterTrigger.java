package com.emms.backend.entity;

import com.emms.backend.entity.abstracts.WorkOrderBase;
import com.emms.backend.entity.enums.WorkOrderMeterTriggerCondition;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_order_meter_triggers", indexes = {
        @Index(name = "idx_womt_meter", columnList = "meter_id"),
        @Index(name = "idx_womt_active", columnList = "active"),
        @Index(name = "idx_womt_condition", columnList = "trigger_condition")
})
@Schema(description = "Quy tắc tự động tạo Work Order theo chỉ số meter")
public class WorkOrderMeterTrigger extends WorkOrderBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Schema(description = "ID", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "recurrent", nullable = false)
    @Schema(description = "Có lặp lại sau khi trigger hay không")
    private boolean recurrent = false;

    @Column(name = "active", nullable = false)
    @Schema(description = "Trigger đang hoạt động hay không")
    private boolean active = true;

    @NotBlank
    @Column(name = "name", nullable = false, length = 255)
    @Schema(description = "Tên trigger", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_condition", nullable = false, length = 50)
    @Schema(description = "Điều kiện trigger", requiredMode = Schema.RequiredMode.REQUIRED)
    private WorkOrderMeterTriggerCondition triggerCondition;

    @NotNull
    @Column(name = "trigger_value", nullable = false, precision = 19, scale = 2)
    @Schema(description = "Ngưỡng giá trị để trigger")
    private BigDecimal triggerValue;

    @NotNull
    @Column(name = "cooldown_minutes", nullable = false)
    @Schema(description = "Thời gian chờ giữa 2 lần trigger, đơn vị phút")
    private Integer cooldownMinutes = 0;

    @Column(name = "last_triggered_at")
    @Schema(description = "Lần cuối rule này được trigger")
    private LocalDateTime lastTriggeredAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meter_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    @Schema(description = "Meter dùng để theo dõi trigger")
    private Meter meter;

    public WorkOrderMeterTrigger() {
    }

    public WorkOrderMeterTrigger(Long id,
                                 boolean recurrent,
                                 boolean active,
                                 String name,
                                 WorkOrderMeterTriggerCondition triggerCondition,
                                 BigDecimal triggerValue,
                                 Integer cooldownMinutes,
                                 LocalDateTime lastTriggeredAt,
                                 Meter meter) {
        this.id = id;
        this.recurrent = recurrent;
        this.active = active;
        this.name = trim(name);
        this.triggerCondition = triggerCondition;
        this.triggerValue = triggerValue;
        this.cooldownMinutes = cooldownMinutes;
        this.lastTriggeredAt = lastTriggeredAt;
        this.meter = meter;
    }

    @PrePersist
    @PreUpdate
    public void beforeSave() {
        this.name = trim(this.name);

        if (this.cooldownMinutes == null || this.cooldownMinutes < 0) {
            this.cooldownMinutes = 0;
        }

        if (this.triggerValue == null) {
            this.triggerValue = BigDecimal.ZERO;
        }
    }

    protected String trim(String value) {
        if (value == null) {
            return null;
    }
    
    String trimmed = value.trim();
    
    return trimmed.isEmpty() ? null : trimmed;
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

    public Meter getMeter() {
        return meter;
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
        this.name = trim(name);
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

    public void setMeter(Meter meter) {
        this.meter = meter;
    }
}