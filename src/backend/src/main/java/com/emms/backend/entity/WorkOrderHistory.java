package com.emms.backend.entity;

import com.emms.backend.entity.abstracts.Audit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "work_order_histories", indexes = {
        @Index(name = "idx_woh_work_order", columnList = "work_order_id"),
        @Index(name = "idx_woh_saved_by", columnList = "saved_by"),
        @Index(name = "idx_woh_version_no", columnList = "version_no")
})
@Schema(description = "Lịch sử phiên bản Work Order")
public class WorkOrderHistory extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Schema(description = "ID lịch sử", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank
    @Column(name = "version_name", nullable = false, length = 255)
    @Schema(description = "Tên phiên bản lưu")
    private String versionName;

    @NotNull
    @Column(name = "version_no", nullable = false)
    @Schema(description = "Số phiên bản", example = "1")
    private Integer versionNo = 1;

    @Column(name = "note", length = 1000)
    @Schema(description = "Ghi chú khi lưu lịch sử")
    private String note;

    @Lob
    @Column(name = "snapshot_json")
    @Schema(description = "Snapshot dữ liệu Work Order tại thời điểm lưu, dạng JSON")
    private String snapshotJson;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "saved_by", nullable = false)
    @Schema(description = "Người lưu phiên bản")
    private User savedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    @Schema(description = "Work Order gốc")
    private WorkOrder workOrder;

    public WorkOrderHistory() {
    }

    public WorkOrderHistory(Long id,
                            String versionName,
                            Integer versionNo,
                            String note,
                            String snapshotJson,
                            User savedBy,
                            WorkOrder workOrder) {
        this.id = id;
        this.versionName = trim(versionName);
        this.versionNo = versionNo;
        this.note = trim(note);
        this.snapshotJson = snapshotJson;
        this.savedBy = savedBy;
        this.workOrder = workOrder;
    }

    public WorkOrderHistory(String versionName,
                            Integer versionNo,
                            String note,
                            String snapshotJson,
                            User savedBy,
                            WorkOrder workOrder) {
        this.versionName = trim(versionName);
        this.versionNo = versionNo;
        this.note = trim(note);
        this.snapshotJson = snapshotJson;
        this.savedBy = savedBy;
        this.workOrder = workOrder;
    }

    @PrePersist
    @PreUpdate
    public void prePersistAndUpdate() {
        this.versionName = trim(this.versionName);
        this.note = trim(this.note);

        if (this.versionNo == null || this.versionNo < 1) {
            this.versionNo = 1;
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    public Long getId() {
        return id;
    }

    public String getVersionName() {
        return versionName;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public String getNote() {
        return note;
    }

    public String getSnapshotJson() {
        return snapshotJson;
    }

    public User getSavedBy() {
        return savedBy;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setVersionName(String versionName) {
        this.versionName = trim(versionName);
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public void setNote(String note) {
        this.note = trim(note);
    }

    public void setSnapshotJson(String snapshotJson) {
        this.snapshotJson = snapshotJson;
    }

    public void setSavedBy(User savedBy) {
        this.savedBy = savedBy;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }
}