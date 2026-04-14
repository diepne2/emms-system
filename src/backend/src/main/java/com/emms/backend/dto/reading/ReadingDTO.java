package com.emms.backend.dto.reading;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO tạo/cập nhật chỉ số meter")
public class ReadingDTO {

    @Schema(description = "Giá trị đo", example = "1250.5")
    private Double value;

    @Schema(description = "ID meter", example = "1")
    private Long meterId;

    @Schema(description = "Thời điểm ghi nhận", example = "2026-04-12T10:30:00")
    private LocalDateTime recordedAt;

    public ReadingDTO() {
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Long getMeterId() {
        return meterId;
    }

    public void setMeterId(Long meterId) {
        this.meterId = meterId;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}