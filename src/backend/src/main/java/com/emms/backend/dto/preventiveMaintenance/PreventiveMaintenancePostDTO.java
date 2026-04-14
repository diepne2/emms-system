package com.emms.backend.dto.preventiveMaintenance;

import com.emms.backend.entity.enums.Priority;
import com.emms.backend.entity.enums.RecurrenceBasedOn;
import com.emms.backend.entity.enums.RecurrenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "DTO dùng để tạo kế hoạch bảo trì định kỳ")
public class PreventiveMaintenancePostDTO {

    @Schema(description = "Tiêu đề kế hoạch")
    @NotBlank(message = "title không được để trống")
    private String title;

    @Schema(description = "Mô tả kế hoạch")
    private String description;

    @Schema(description = "ID thiết bị")
    private Long assetId;

    @Schema(description = "ID người được phân công")
    private Long assignedToId;

    @Schema(description = "Số giờ ước tính")
    private Double estimatedHours;

    @Schema(description = "Ngày bắt đầu")
    private LocalDateTime startsOn;

    @Schema(description = "Tần suất lặp")
    @NotNull(message = "frequency không được để trống")
    private Integer frequency;

    @Schema(description = "Độ trễ hạn hoàn thành")
    private Integer dueDateDelay;

    @Schema(description = "Ngày kết thúc")
    private LocalDateTime endsOn;

    @Schema(description = "Loại lặp")
    @NotNull(message = "recurrenceType không được để trống")
    private RecurrenceType recurrenceType;

    @Schema(description = "Cơ sở lặp")
    @NotNull(message = "recurrenceBasedOn không được để trống")
    private RecurrenceBasedOn recurrenceBasedOn;

    @Schema(description = "Danh sách ngày trong tuần")
    private List<Integer> daysOfWeek = new ArrayList<>();

    @Schema(description = "Mức độ ưu tiên")
    private Priority priority;

    public PreventiveMaintenancePostDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = trim(title);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = trim(description);
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public Double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public LocalDateTime getStartsOn() {
        return startsOn;
    }

    public void setStartsOn(LocalDateTime startsOn) {
        this.startsOn = startsOn;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Integer getDueDateDelay() {
        return dueDateDelay;
    }

    public void setDueDateDelay(Integer dueDateDelay) {
        this.dueDateDelay = dueDateDelay;
    }

    public LocalDateTime getEndsOn() {
        return endsOn;
    }

    public void setEndsOn(LocalDateTime endsOn) {
        this.endsOn = endsOn;
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
        this.daysOfWeek = daysOfWeek != null ? daysOfWeek : new ArrayList<>();
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}