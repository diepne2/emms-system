package com.emms.backend.dto.workorderBase;

import com.emms.backend.dto.asset.AssetSummaryDTO;
import com.emms.backend.dto.audit.AuditShowDTO;
import com.emms.backend.dto.category.CategorySummaryDTO;
import com.emms.backend.dto.file.FileSummaryDTO;
import com.emms.backend.dto.location.LocationSummaryDTO;
import com.emms.backend.dto.user.UserSummaryDTO;
import com.emms.backend.entity.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Hiển thị thông tin cơ bản của đơn hàng công việc")
public class WorkOrderBaseShowDTO extends AuditShowDTO {
    @Schema(description = "Ngày đáo hạn của đơn hàng công việc")
    private LocalDate dueDate;

    @Schema(description = "Mức độ ưu tiên của đơn hàng công việc")
    private Priority priority;

    @Schema(description = "Thời lượng ước tính trong giờ")
    private Double estimatedDuration;

    @Schema(description = "Ngày bắt đầu ước tính cho đơn hàng công việc")
    private LocalDateTime estimatedStartDate;

    @Schema(description = "Mô tả chi tiết của đơn hàng công việc")
    private String description;

    @Schema(description = "Tiêu đề của đơn hàng công việc")
    private String title;

    @Schema(description = "Cho biết liệu một chữ ký có được yêu cầu không")
    private Boolean requiredSignature;

    @Schema(description = "Danh mục liên quan đến đơn hàng công việc")
    private CategorySummaryDTO category;

    private LocationSummaryDTO location;

    @Schema(description = "Người dùng chính chịu trách nhiệm cho đơn hàng công việc")
    private UserSummaryDTO primaryUser;

    @Schema(description = "Kỹ thuật viên/người dùng được chỉ định")
    private UserSummaryDTO assignedTo;

    private AssetSummaryDTO asset;


    private List<FileSummaryDTO> files;

 
    private FileSummaryDTO image;

    public WorkOrderBaseShowDTO() {
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
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

    public UserSummaryDTO getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UserSummaryDTO assignedTo) {
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

    protected String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}