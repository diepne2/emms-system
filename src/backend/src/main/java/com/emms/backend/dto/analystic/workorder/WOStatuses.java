package com.emms.backend.dto.analystic.workorder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thống kê số lượng work order theo trạng thái")
public class WOStatuses {

    @Schema(description = "Số work order chờ duyệt")
    private Integer pendingCount; // CHO_DUYET

    @Schema(description = "Số work order đã phân công")
    private Integer assignedCount; // DA_PHAN_CONG

    @Schema(description = "Số work order đang thực hiện")
    private Integer inProgressCount; // DANG_THUC_HIEN

    @Schema(description = "Số work order tạm dừng")
    private Integer onHoldCount; // TAM_DUNG

    @Schema(description = "Số work order chờ xác nhận")
    private Integer awaitingConfirmationCount; // CHO_XAC_NHAN

    @Schema(description = "Số work order đã hoàn thành")
    private Integer completedCount; // HOAN_THANH

    @Schema(description = "Số work order bị từ chối")
    private Integer rejectedCount; // REJECTED


    public WOStatuses() {
    }

    public WOStatuses(Integer pendingCount,
                      Integer assignedCount,
                      Integer inProgressCount,
                      Integer onHoldCount,
                      Integer awaitingConfirmationCount,
                      Integer completedCount,
                      Integer rejectedCount) {
        this.pendingCount = pendingCount;
        this.assignedCount = assignedCount;
        this.inProgressCount = inProgressCount;
        this.onHoldCount = onHoldCount;
        this.awaitingConfirmationCount = awaitingConfirmationCount;
        this.completedCount = completedCount;
        this.rejectedCount = rejectedCount;
    }

    public Integer getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Integer pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Integer getAssignedCount() {
        return assignedCount;
    }

    public void setAssignedCount(Integer assignedCount) {
        this.assignedCount = assignedCount;
    }

    public Integer getInProgressCount() {
        return inProgressCount;
    }

    public void setInProgressCount(Integer inProgressCount) {
        this.inProgressCount = inProgressCount;
    }

    public Integer getOnHoldCount() {
        return onHoldCount;
    }

    public void setOnHoldCount(Integer onHoldCount) {
        this.onHoldCount = onHoldCount;
    }

    public Integer getAwaitingConfirmationCount() {
        return awaitingConfirmationCount;
    }

    public void setAwaitingConfirmationCount(Integer awaitingConfirmationCount) {
        this.awaitingConfirmationCount = awaitingConfirmationCount;
    }

    public Integer getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    public Integer getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(Integer rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    // ===== toString =====

    @Override
    public String toString() {
        return "WOStatuses{" +
                "pendingCount=" + pendingCount +
                ", assignedCount=" + assignedCount +
                ", inProgressCount=" + inProgressCount +
                ", onHoldCount=" + onHoldCount +
                ", awaitingConfirmationCount=" + awaitingConfirmationCount +
                ", completedCount=" + completedCount +
                ", rejectedCount=" + rejectedCount +
                '}';
    }
}