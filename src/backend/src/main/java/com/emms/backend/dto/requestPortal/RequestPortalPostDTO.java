package com.emms.backend.dto.requestPortal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Schema(description = "DTO dùng để tạo mới request portal")
public class RequestPortalPostDTO {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    @Schema(description = "Tiêu đề của request portal", example = "Cổng gửi yêu cầu bảo trì")
    private String title;

    @Size(max = 2000, message = "Lời chào không được vượt quá 2000 ký tự")
    @Schema(description = "Thông điệp chào mừng hiển thị trên portal", example = "Vui lòng điền đầy đủ thông tin bên dưới")
    private String welcomeMessage;


    public RequestPortalPostDTO() {
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

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}