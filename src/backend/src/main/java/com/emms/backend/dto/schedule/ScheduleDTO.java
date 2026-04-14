package com.emms.backend.dto.schedule;

import com.emms.backend.entity.enums.RecurrenceBasedOn;
import com.emms.backend.entity.enums.RecurrenceType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDTO {

    private Boolean disabled;
    private LocalDate startsOn;
    private Integer frequency;
    private LocalDate endsOn;
    private Integer dueDateDelay;
    private RecurrenceType recurrenceType;
    private RecurrenceBasedOn recurrenceBasedOn;
    private List<Integer> daysOfWeek = new ArrayList<>();

    public ScheduleDTO() {
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
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
        this.daysOfWeek = daysOfWeek == null ? new ArrayList<>() : daysOfWeek;
    }
}