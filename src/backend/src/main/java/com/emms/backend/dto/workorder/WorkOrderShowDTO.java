package com.emms.backend.dto.workorder;

import com.emms.backend.dto.workorderBase.WorkOrderBaseShowDTO;
import com.emms.backend.entity.WorkOrder.WorkOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Work order DTO for displaying work order details in API responses")
public class WorkOrderShowDTO extends WorkOrderBaseShowDTO {

    @Schema(description = "ID công việc")
    private Long id;

    @Schema(description = "Trạng thái công việc")
    private WorkOrderStatus status;

    @Schema(description = "Người hoàn thành")
    private String completedBy;

    @Schema(description = "Thời điểm hoàn thành")
    private LocalDateTime completedOn;

    @Schema(description = "Đã lưu trữ hay chưa")
    private Boolean archived;

    @Schema(description = "Phản hồi")
    private String feedback;

    @Schema(description = "Ngày tạo nghiệp vụ")
    private LocalDateTime dateCreated;

    @Schema(description = "Ngày tạo bản ghi")
    private LocalDateTime createdAt;

    @Schema(description = "Ngày cập nhật gần nhất")
    private LocalDateTime updatedAt;

    public WorkOrderShowDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrderStatus status) {
        this.status = status;
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

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = trim(feedback);
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}