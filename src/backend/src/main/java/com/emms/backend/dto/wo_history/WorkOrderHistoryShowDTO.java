package com.emms.backend.dto.wo_history;

import com.emms.backend.dto.audit.AuditShowDTO;
import com.emms.backend.dto.user.UserSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO hiển thị lịch sử phiên bản Work Order")
public class WorkOrderHistoryShowDTO extends AuditShowDTO {

    @Schema(description = "ID lịch sử", example = "1")
    private Long id;

    @Schema(description = "Tên phiên bản", example = "WO-0001 - Bản lưu trước khi duyệt")
    private String versionName;

    @Schema(description = "Số phiên bản", example = "1")
    private Integer versionNo;

    @Schema(description = "Ghi chú")
    private String note;

    @Schema(description = "Snapshot JSON")
    private String snapshotJson;

    @Schema(description = "Người lưu phiên bản")
    private UserSummaryDTO savedBy;

    @Schema(description = "ID của Work Order", example = "10")
    private Long workOrderId;

    public WorkOrderHistoryShowDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName == null ? null : versionName.trim();
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note == null ? null : note.trim();
    }

    public String getSnapshotJson() {
        return snapshotJson;
    }

    public void setSnapshotJson(String snapshotJson) {
        this.snapshotJson = snapshotJson;
    }

    public UserSummaryDTO getSavedBy() {
        return savedBy;
    }

    public void setSavedBy(UserSummaryDTO savedBy) {
        this.savedBy = savedBy;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }
}