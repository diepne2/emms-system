package com.emms.backend.dto.labor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(description = "DTO dùng để cập nhật một phần thông tin công việc (labor)")
public class LaborPatchDTO {

    @Schema(description = "ID của người được phân công")
    private Long assignedToId;

    @Schema(description = "Xác định có tính thời gian này vào tổng thời gian hay không")
    private Boolean includeToTotalTime;

    @Schema(description = "Đơn giá theo giờ (không được âm)")
    private Long hourlyRate;

    @Schema(description = "Thời lượng làm việc (tính bằng giây)")
    private Long duration;

    @Schema(description = "Thời điểm bắt đầu làm việc")
    private Date startedAt;

    @Schema(description = "ID của loại thời gian (Time Category)")
    private Long timeCategoryId;

    // ===== Getter & Setter =====

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public Boolean getIncludeToTotalTime() {
        return includeToTotalTime;
    }

    public void setIncludeToTotalTime(Boolean includeToTotalTime) {
        this.includeToTotalTime = includeToTotalTime;
    }

    public Long getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Long hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Long getTimeCategoryId() {
        return timeCategoryId;
    }

    public void setTimeCategoryId(Long timeCategoryId) {
        this.timeCategoryId = timeCategoryId;
    }
}