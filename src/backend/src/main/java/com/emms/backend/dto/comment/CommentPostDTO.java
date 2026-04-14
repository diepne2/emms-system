package com.emms.backend.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO tạo comment")
public class CommentPostDTO {

    @NotNull
    @Schema(description = "ID work order", example = "1")
    private Long workOrderId;

    @NotBlank
    @Schema(description = "Nội dung comment")
    private String content;

    public CommentPostDTO() {
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