package com.emms.backend.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO cập nhật comment")
public class CommentPatchDTO {

    @Schema(description = "Nội dung comment", example = "Đã cập nhật lại nội dung comment")
    private String content;

    public CommentPatchDTO() {
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