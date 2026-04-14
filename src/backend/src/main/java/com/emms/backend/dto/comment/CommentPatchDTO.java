package com.emms.backend.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO cập nhật comment")
public class CommentPatchDTO {

    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Schema(description = "Nội dung comment", example = "Đã cập nhật nội dung comment")
    private String content;

    public CommentPatchDTO() {
    }

    public CommentPatchDTO(String content) {
        this.content = trim(content);
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