package com.emms.backend.dto.comment;

import com.emms.backend.dto.user.UserSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO hiển thị chi tiết comment")
public class CommentShowDTO {

    @Schema(description = "ID của comment", example = "10")
    private Long id;

    @Schema(description = "Thông tin người tạo comment")
    private UserSummaryDTO user;

    @Schema(description = "Nội dung comment", example = "Cần kiểm tra lại thiết bị này")
    private String content;

    @Schema(description = "Comment hệ thống hay không", example = "false")
    private boolean system;

    @Schema(description = "Thời gian tạo comment")
    private LocalDateTime createdAt;

    public CommentShowDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserSummaryDTO getUser() {
        return user;
    }

    public void setUser(UserSummaryDTO user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = trim(content);
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}