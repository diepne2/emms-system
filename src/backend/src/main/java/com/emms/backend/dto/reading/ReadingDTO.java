package com.emms.backend.dto.reading;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "DTO tạo Reading")
public class ReadingDTO {

    private Long meterId;

    @Schema(description = "Giá trị đọc")
    private BigDecimal value;

    @Schema(description = "Thời điểm ghi nhận")
    private LocalDateTime recordedAt;

    @Schema(description = "Ghi chú")
    private String note;

    public ReadingDTO() {
    }

    public Long getMeterId() {
        return meterId;
    }

    public void setMeterId(Long meterId) {
        this.meterId = meterId;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = trim(note);
    }

    private String trim(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}