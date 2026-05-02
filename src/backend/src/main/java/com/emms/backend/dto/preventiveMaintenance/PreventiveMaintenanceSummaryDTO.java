package com.emms.backend.dto.preventiveMaintenance;

import com.emms.backend.entity.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary DTO for preventive maintenance")
public class PreventiveMaintenanceSummaryDTO {

    private Long id;
    private String code;
    private String title;
    private String description;
    private Boolean active;

    private Long assetId;
    private String assetName;

    private Long assignedToId;
    private String assignedToName;

    private Priority priority;

    public PreventiveMaintenanceSummaryDTO() {
    }

    public PreventiveMaintenanceSummaryDTO(
            Long id,
            String code,
            String title,
            String description,
            Boolean active,
            Long assetId,
            String assetName,
            Long assignedToId,
            String assignedToName,
            Priority priority
    ) {
        this.id = id;
        this.code = trim(code);
        this.title = trim(title);
        this.description = trim(description);
        this.active = active;
        this.assetId = assetId;
        this.assetName = trim(assetName);
        this.assignedToId = assignedToId;
        this.assignedToName = trim(assignedToName);
        this.priority = priority;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = trim(code); }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = trim(title); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = trim(description); }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = trim(assetName); }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = trim(assignedToName); }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    private String trim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}