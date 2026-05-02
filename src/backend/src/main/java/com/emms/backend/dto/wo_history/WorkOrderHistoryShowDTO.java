package com.emms.backend.dto.wo_history;

import java.time.LocalDateTime;

public class WorkOrderHistoryShowDTO {

    private Long id;
    private Long workOrderId;
    private Integer versionNo;
    private String versionName;
    private String note;
    private String snapshotJson;
    private Long savedById;
    private String savedByName;
    private LocalDateTime createdAt;

    public WorkOrderHistoryShowDTO() {
    }

    public Long getId() {
        return id;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getNote() {
        return note;
    }

    public String getSnapshotJson() {
        return snapshotJson;
    }

    public Long getSavedById() {
        return savedById;
    }

    public String getSavedByName() {
        return savedByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setSnapshotJson(String snapshotJson) {
        this.snapshotJson = snapshotJson;
    }

    public void setSavedById(Long savedById) {
        this.savedById = savedById;
    }

    public void setSavedByName(String savedByName) {
        this.savedByName = savedByName;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}