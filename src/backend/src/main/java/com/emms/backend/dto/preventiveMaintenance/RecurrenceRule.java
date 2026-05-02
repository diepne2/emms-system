package com.emms.backend.dto.preventiveMaintenance;

import com.emms.backend.entity.enums.Priority;
import com.emms.backend.entity.enums.RecurrenceBasedOn;
import com.emms.backend.entity.enums.RecurrenceType;

import java.util.List;

public class RecurrenceRule {

    private RecurrenceType type; // DAILY / WEEKLY / MONTHLY / YEARLY

    private RecurrenceBasedOn basedOn; // SCHEDULED_DATE / COMPLETED_DATE

    private Integer frequency;

    private Integer dueDateDelay;

    private List<Integer> daysOfWeek;

    private Priority priority;

    public RecurrenceType getType() { return type; }
    public void setType(RecurrenceType type) { this.type = type; }

    public RecurrenceBasedOn getBasedOn() { return basedOn; }
    public void setBasedOn(RecurrenceBasedOn basedOn) { this.basedOn = basedOn; }

    public Integer getFrequency() { return frequency; }
    public void setFrequency(Integer frequency) { this.frequency = frequency; }

    public Integer getDueDateDelay() { return dueDateDelay; }
    public void setDueDateDelay(Integer dueDateDelay) { this.dueDateDelay = dueDateDelay; }

    public List<Integer> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(List<Integer> daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
}