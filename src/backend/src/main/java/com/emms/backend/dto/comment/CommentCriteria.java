package com.emms.backend.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Điều kiện tìm kiếm comment theo work order")
public class CommentCriteria {

    @NotNull(message = "workOrderId không được để trống")
    @Schema(description = "ID của work order", example = "1")
    private Long workOrderId;

    public CommentCriteria() {
    }

    public CommentCriteria(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }
}