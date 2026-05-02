package com.emms.backend.dto.workorder;

import com.emms.backend.dto.workorderBase.WorkOrderBaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO cho đơn hàng công việc")
public class WorkOrderDTO extends WorkOrderBaseDTO {

    private Long assignedToId;

    private Long assetId;

    @Schema(description = "Tên người hoàn thành công việc")
    private String completedBy;

    @Schema(description = "Ngày và giờ khi đơn hàng công việc được hoàn thành")
    private LocalDateTime completedOn;

    @Schema(description = "Xem liệu đơn hàng công việc có được lưu trữ không")
    private Boolean archived;

    public WorkOrderDTO() {
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(String completedBy) {
        this.completedBy = trim(completedBy);
    }

    public LocalDateTime getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(LocalDateTime completedOn) {
        this.completedOn = completedOn;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    @Override
    public void setEstimatedDuration(Double estimatedDuration) {
        super.setEstimatedDuration(estimatedDuration);
    }

    protected String trim(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}