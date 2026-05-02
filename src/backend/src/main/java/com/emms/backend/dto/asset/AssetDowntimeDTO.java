package com.emms.backend.dto.asset;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class AssetDowntimeDTO {

    @NotNull(message = "assetId không được để trống")
    private Long assetId;

    private Long workOrderId;

    private String reason;

    @NotNull(message = "startsOn không được để trống")
    private LocalDateTime startsOn;

    private LocalDateTime endsOn;

    private String note;

    public AssetDowntimeDTO() {
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason == null ? null : reason.trim();
    }

    public LocalDateTime getStartsOn() {
        return startsOn;
    }

    public void setStartsOn(LocalDateTime startsOn) {
        this.startsOn = startsOn;
    }

    public LocalDateTime getEndsOn() {
        return endsOn;
    }

    public void setEndsOn(LocalDateTime endsOn) {
        this.endsOn = endsOn;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note == null ? null : note.trim();
    }
}