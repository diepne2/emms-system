package com.emms.backend.dto.requestPortal;

import com.emms.backend.dto.audit.AuditShowDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO public để hiển thị thông tin request portal (không cần đăng nhập)")
public class RequestPortalPublicDTO extends AuditShowDTO {

    @Schema(description = "Tiêu đề của request portal")
    private String title;

    @Schema(description = "Thông điệp chào mừng hiển thị trên portal")
    private String welcomeMessage;

    @Schema(description = "Mã định danh duy nhất (UUID) của portal")
    private String uuid;

    public RequestPortalPublicDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = trim(title);
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = trim(welcomeMessage);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = trim(uuid);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}