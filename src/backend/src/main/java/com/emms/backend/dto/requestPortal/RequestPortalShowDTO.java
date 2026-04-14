package com.emms.backend.dto.requestPortal;

import com.emms.backend.dto.audit.AuditShowDTO;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "DTO dùng để hiển thị chi tiết request portal trong API response")
public class RequestPortalShowDTO extends AuditShowDTO {

    @Schema(description = "Tiêu đề của request portal", example = "Cổng gửi yêu cầu bảo trì")
    private String title;

    @Schema(description = "Thông điệp chào mừng hiển thị trên portal", example = "Vui lòng điền đầy đủ thông tin bên dưới")
    private String welcomeMessage;

    @Schema(description = "Mã định danh duy nhất (UUID) của portal", example = "f3a2f7d2-7e6a-4a9d-9f8c-21a7f47ab123")
    private String uuid;

    public RequestPortalShowDTO() {
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