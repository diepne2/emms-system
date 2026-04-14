package com.emms.backend.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO hiển thị comment")
public class CommentShowDTO {

    @Schema(description = "ID comment", example = "10")
    private Long id;

    @Schema(description = "ID work order", example = "1")
    private Long workOrderId;

    @Schema(description = "Tiêu đề work order", example = "Bảo trì máy nén khí")
    private String workOrderTitle;

    @Schema(description = "ID người tạo comment", example = "2")
    private Long userId;

    @Schema(description = "Tên đầy đủ người tạo", example = "Nguyễn Văn A")
    private String userFullName;

    @Schema(description = "Username người tạo", example = "nguyenvana")
    private String username;

    @Schema(description = "Nội dung comment")
    private String content;

    @Schema(description = "Thời gian tạo")
    private LocalDateTime createdAt;

    public CommentShowDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getWorkOrderTitle() {
        return workOrderTitle;
    }

    public void setWorkOrderTitle(String workOrderTitle) {
        this.workOrderTitle = trim(workOrderTitle);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = trim(userFullName);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = trim(username);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = trim(content);
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