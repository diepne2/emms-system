package com.emms.backend.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tiêu chí tìm comment")
public class CommentCriteria {

    @Schema(description = "ID work order", example = "1")
    private Long workOrderId;

    public CommentCriteria() {
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }
}