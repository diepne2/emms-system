package com.emms.backend.entity;

import com.emms.backend.entity.enums.RecurrenceBasedOn;
import com.emms.backend.entity.enums.RecurrenceType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedules", indexes = {
        @Index(name = "idx_schedule_starts_on", columnList = "starts_on"),
        @Index(name = "idx_schedule_ends_on", columnList = "ends_on"),
        @Index(name = "idx_schedule_type", columnList = "recurrence_type"),
        @Index(name = "idx_schedule_based_on", columnList = "recurrence_based_on")
})
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "disabled", nullable = false)
    private boolean disabled = false;

    @Column(name = "starts_on", nullable = false)
    private LocalDate startsOn = LocalDate.now();

    @Column(name = "frequency", nullable = false)
    private Integer frequency = 1;

    @Column(name = "ends_on")
    private LocalDate endsOn;

    @Column(name = "due_date_delay")
    private Integer dueDateDelay;

    @Column(name = "demo", nullable = false)
    private boolean demo = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false, length = 30)
    private RecurrenceType recurrenceType = RecurrenceType.DAILY;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_based_on", nullable = false, length = 30)
    private RecurrenceBasedOn recurrenceBasedOn = RecurrenceBasedOn.SCHEDULED_DATE;

    @ElementCollection
    @CollectionTable(
            name = "schedule_days_of_week",
            joinColumns = @JoinColumn(name = "schedule_id")
    )
    @Column(name = "day_of_week", nullable = false)
    private List<Integer> daysOfWeek = new ArrayList<>();

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preventive_maintenance_id", nullable = false, unique = true)
    private PreventiveMaintenance preventiveMaintenance;



    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Schedule() {
    }

    public Schedule(PreventiveMaintenance preventiveMaintenance) {
        setPreventiveMaintenance(preventiveMaintenance);
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        normalize();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        normalize();
    }

    private void normalize() {
        if (startsOn == null) {
            startsOn = LocalDate.now();
        }

        if (frequency == null || frequency < 1) {
            throw new IllegalArgumentException("frequency phải >= 1");
        }

        if (dueDateDelay != null && dueDateDelay < 0) {
            throw new IllegalArgumentException("dueDateDelay không được âm");
        }

        if (recurrenceType == null) {
            recurrenceType = RecurrenceType.DAILY;
        }

        if (recurrenceBasedOn == null) {
            recurrenceBasedOn = RecurrenceBasedOn.SCHEDULED_DATE;
        }

        if (endsOn != null && endsOn.isBefore(startsOn)) {
            throw new IllegalArgumentException("endsOn phải >= startsOn");
        }

        if (daysOfWeek == null) {
            daysOfWeek = new ArrayList<>();
        }

        normalizeDaysOfWeek();

        if (preventiveMaintenance == null) {
            throw new IllegalArgumentException("preventiveMaintenance không được null");
        }
    }

    private void normalizeDaysOfWeek() {
        List<Integer> normalized = new ArrayList<>();

        for (Integer day : daysOfWeek) {
            if (day == null) {
                continue;
            }
            if (day < 1 || day > 7) {
                throw new IllegalArgumentException("daysOfWeek chỉ nhận giá trị từ 1 đến 7");
            }
            if (!normalized.contains(day)) {
                normalized.add(day);
            }
        }

        this.daysOfWeek = normalized;
    }

    public boolean isWeekly() {
        return recurrenceType == RecurrenceType.WEEKLY;
    }

    public boolean isMonthly() {
        return recurrenceType == RecurrenceType.MONTHLY;
    }

    public boolean isYearly() {
        return recurrenceType == RecurrenceType.YEARLY;
    }

    public boolean isDaily() {
        return recurrenceType == RecurrenceType.DAILY;
    }

    public boolean isActiveOn(LocalDate date) {
        if (date == null || disabled) {
            return false;
        }
        if (date.isBefore(startsOn)) {
            return false;
        }
        return endsOn == null || !date.isAfter(endsOn);
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public LocalDate getStartsOn() {
        return startsOn;
    }

    public void setStartsOn(LocalDate startsOn) {
        this.startsOn = startsOn;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public LocalDate getEndsOn() {
        return endsOn;
    }

    public void setEndsOn(LocalDate endsOn) {
        this.endsOn = endsOn;
    }

    public Integer getDueDateDelay() {
        return dueDateDelay;
    }

    public void setDueDateDelay(Integer dueDateDelay) {
        this.dueDateDelay = dueDateDelay;
    }

    public boolean isDemo() {
        return demo;
    }

    public void setDemo(boolean demo) {
        this.demo = demo;
    }

    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(RecurrenceType recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public RecurrenceBasedOn getRecurrenceBasedOn() {
        return recurrenceBasedOn;
    }

    public void setRecurrenceBasedOn(RecurrenceBasedOn recurrenceBasedOn) {
        this.recurrenceBasedOn = recurrenceBasedOn;
    }

    public List<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<Integer> daysOfWeek) {
        this.daysOfWeek = daysOfWeek == null ? new ArrayList<>() : new ArrayList<>(daysOfWeek);
    }

    public PreventiveMaintenance getPreventiveMaintenance() {
        return preventiveMaintenance;
    }

    public void setPreventiveMaintenance(PreventiveMaintenance preventiveMaintenance) {
        this.preventiveMaintenance = preventiveMaintenance;
        if (preventiveMaintenance != null && preventiveMaintenance.getSchedule() != this) {
            preventiveMaintenance.setSchedule(this);
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}