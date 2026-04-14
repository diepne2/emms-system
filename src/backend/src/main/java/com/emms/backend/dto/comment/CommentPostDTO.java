package com.emms.backend.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO tạo mới comment")
public class CommentPostDTO {

    @NotNull(message = "workOrderId không được để trống")
    @Schema(description = "ID của work order", example = "1")
    private Long workOrderId;

    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Schema(description = "Nội dung comment", example = "Cần kiểm tra lại thiết bị này")
    private String content;

    public CommentPostDTO() {
    }

    public CommentPostDTO(Long workOrderId, String content) {
        this.workOrderId = workOrderId;
        this.content = trim(content);
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = trim(content);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}