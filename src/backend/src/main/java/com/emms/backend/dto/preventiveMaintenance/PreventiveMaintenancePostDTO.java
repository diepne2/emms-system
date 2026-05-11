package com.emms.backend.dto.preventiveMaintenance;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class PreventiveMaintenancePostDTO {

    @NotBlank
    private String title;

    private String description;

    private Long assetId;

    private Long assignedToId;

    private Double estimatedHours;

    @NotNull
    private LocalDateTime startsOn;

    @NotNull
    private RecurrenceRule recurrenceRule;

    private LocalDateTime endsOn;


    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public LocalDateTime getStartsOn() { return startsOn; }
    public void setStartsOn(LocalDateTime startsOn) { this.startsOn = startsOn; }

    public RecurrenceRule getRecurrenceRule() { return recurrenceRule; }
    public void setRecurrenceRule(RecurrenceRule recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }

    public LocalDateTime getEndsOn() {
        return endsOn;
    }
    
    public void setEndsOn(LocalDateTime endsOn) {
        this.endsOn = endsOn;
    }
}