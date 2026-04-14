package com.emms.backend.dto.workorderBase;

import com.emms.backend.dto.asset.AssetSummaryDTO;
import com.emms.backend.dto.audit.AuditShowDTO;
import com.emms.backend.dto.category.CategorySummaryDTO;
import com.emms.backend.dto.file.FileSummaryDTO;
import com.emms.backend.dto.location.LocationSummaryDTO;
import com.emms.backend.dto.user.UserSummaryDTO;
import com.emms.backend.entity.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Base work order display data transfer object")
public class WorkOrderBaseShowDTO extends AuditShowDTO {

    @Schema(description = "Due date of the work order")
    private LocalDateTime dueDate;

    @Schema(description = "Priority level of the work order")
    private Priority priority;

    @Schema(description = "Estimated duration in hours")
    private Double estimatedDuration;

    @Schema(description = "Estimated start date for the work order")
    private LocalDateTime estimatedStartDate;

    @Schema(description = "Detailed description of the work order")
    private String description;

    @Schema(description = "Title of the work order")
    private String title;

    @Schema(description = "Indicates if a signature is required")
    private Boolean requiredSignature;

    @Schema(description = "Category associated with the work order")
    private CategorySummaryDTO category;

    @Schema(description = "Location where the work will be performed")
    private LocationSummaryDTO location;


    @Schema(description = "Primary user responsible for the work order")
    private UserSummaryDTO primaryUser;

    @Schema(description = "List of users assigned to the work order")
    private List<UserSummaryDTO> assignedTo;

    @Schema(description = "Asset related to the work order")
    private AssetSummaryDTO asset;

    @Schema(description = "List of files attached to the work order")
    private List<FileSummaryDTO> files;

    @Schema(description = "Image associated with the work order")
    private FileSummaryDTO image;

    // ===== Constructor =====
    public WorkOrderBaseShowDTO() {
    }

    // ===== Getter / Setter =====

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Double getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Double estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public LocalDateTime getEstimatedStartDate() {
        return estimatedStartDate;
    }

    public void setEstimatedStartDate(LocalDateTime estimatedStartDate) {
        this.estimatedStartDate = estimatedStartDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = trim(description);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = trim(title);
    }

    public Boolean getRequiredSignature() {
        return requiredSignature;
    }

    public void setRequiredSignature(Boolean requiredSignature) {
        this.requiredSignature = requiredSignature;
    }

    public CategorySummaryDTO getCategory() {
        return category;
    }

    public void setCategory(CategorySummaryDTO category) {
        this.category = category;
    }

    public LocationSummaryDTO getLocation() {
        return location;
    }

    public void setLocation(LocationSummaryDTO location) {
        this.location = location;
    }

    public UserSummaryDTO getPrimaryUser() {
        return primaryUser;
    }

    public void setPrimaryUser(UserSummaryDTO primaryUser) {
        this.primaryUser = primaryUser;
    }

    public List<UserSummaryDTO> getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(List<UserSummaryDTO> assignedTo) {
        this.assignedTo = assignedTo;
    }

    public AssetSummaryDTO getAsset() {
        return asset;
    }

    public void setAsset(AssetSummaryDTO asset) {
        this.asset = asset;
    }

    public List<FileSummaryDTO> getFiles() {
        return files;
    }

    public void setFiles(List<FileSummaryDTO> files) {
        this.files = files;
    }

    public FileSummaryDTO getImage() {
        return image;
    }

    public void setImage(FileSummaryDTO image) {
        this.image = image;
    }

    // ===== helper =====
    private String trim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}