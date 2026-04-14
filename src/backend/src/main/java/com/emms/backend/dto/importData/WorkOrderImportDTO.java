package com.emms.backend.dto.importData;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "DTO for importing work orders from external data sources")
public class WorkOrderImportDTO {

    @Schema(description = "Unique identifier")
    private Long id;

    @Schema(description = "Title")
    @NotBlank
    private String title;

    @Schema(description = "Status")
    private String status;

    @Schema(description = "Priority")
    private String priority;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Due date as epoch milliseconds")
    private Long dueDate;

    @Schema(description = "Estimated duration")
    private Integer estimatedDuration;

    @Schema(description = "Required signature")
    private Boolean requiredSignature;

    @Schema(description = "Category name")
    private String category;

    @Schema(description = "Location name")
    private String locationName;

    @Schema(description = "Team name")
    private String teamName;

    @Schema(description = "Primary user email")
    private String primaryUserEmail;

    @Builder.Default
    @Schema(description = "List of assigned user emails")
    private List<String> assignedToEmails = new ArrayList<>();

    @Schema(description = "Asset name")
    private String assetName;

    @Schema(description = "Completed by email")
    private String completedByEmail;

    @Schema(description = "Completed on as epoch milliseconds")
    private Long completedOn;

    @Schema(description = "Whether the work order is archived")
    private Boolean archived;

    @Schema(description = "Feedback")
    private String feedback;

    @Builder.Default
    @Schema(description = "List of customer names")
    private List<String> customersNames = new ArrayList<>();

    public void setTitle(String title) {
        this.title = trim(title);
    }

    public void setStatus(String status) {
        this.status = trim(status);
    }

    public void setPriority(String priority) {
        this.priority = trim(priority);
    }

    public void setDescription(String description) {
        this.description = trim(description);
    }

    public void setCategory(String category) {
        this.category = trim(category);
    }

    public void setLocationName(String locationName) {
        this.locationName = trim(locationName);
    }

    public void setTeamName(String teamName) {
        this.teamName = trim(teamName);
    }

    public void setPrimaryUserEmail(String primaryUserEmail) {
        this.primaryUserEmail = normalizeEmail(primaryUserEmail);
    }

    public void setAssetName(String assetName) {
        this.assetName = trim(assetName);
    }

    public void setCompletedByEmail(String completedByEmail) {
        this.completedByEmail = normalizeEmail(completedByEmail);
    }

    public void setFeedback(String feedback) {
        this.feedback = trim(feedback);
    }

    public void setAssignedToEmails(List<String> assignedToEmails) {
        this.assignedToEmails = normalizeList(assignedToEmails, true);
    }

    public void setCustomersNames(List<String> customersNames) {
        this.customersNames = normalizeList(customersNames, false);
    }

    public void validate() {
        if (title == null) {
            throw new IllegalArgumentException("Title must not be blank");
        }

        if (estimatedDuration != null && estimatedDuration < 0) {
            throw new IllegalArgumentException("Estimated duration must be greater than or equal to 0");
        }

        if (dueDate != null && dueDate < 0) {
            throw new IllegalArgumentException("Due date must be valid");
        }

        if (completedOn != null && completedOn < 0) {
            throw new IllegalArgumentException("Completed on must be valid");
        }
    }

    private List<String> normalizeList(List<String> values, boolean lowercase) {
        List<String> result = new ArrayList<>();
        if (values == null) {
            return result;
        }

        for (String value : values) {
            String normalized = lowercase ? normalizeEmail(value) : trim(value);
            if (normalized != null) {
                result.add(normalized);
            }
        }
        return result;
    }

    private String normalizeEmail(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}