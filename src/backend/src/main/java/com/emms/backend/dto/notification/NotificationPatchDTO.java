package com.emms.backend.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO dùng để cập nhật trạng thái notification")
public class NotificationPatchDTO {

    @Schema(description = "Đã đọc hay chưa")
    private Boolean read;

    // ===== Constructor =====
    public NotificationPatchDTO() {
    }

    public NotificationPatchDTO(Boolean read) {
        this.read = read;
    }

    // ===== Getter =====
    public Boolean getRead() {
        return read;
    }

    // ===== Setter =====
    public void setRead(Boolean read) {
        this.read = read;
    }
}