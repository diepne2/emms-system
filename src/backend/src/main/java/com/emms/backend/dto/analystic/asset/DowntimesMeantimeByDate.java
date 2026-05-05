package com.emms.backend.dto.analystic.asset;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Thống kê thời gian trung bình downtime theo ngày")
public class DowntimesMeantimeByDate {

    @Schema(description = "Thời gian downtime trung bình (giờ)")
    private Double averageDowntimeHours;

    @Schema(description = "Ngày (yyyy-MM-dd)")
    private LocalDate date;

    public DowntimesMeantimeByDate() {
    }

    public DowntimesMeantimeByDate(Double averageDowntimeHours, LocalDate date) {
        this.averageDowntimeHours = averageDowntimeHours;
        this.date = date;
    }

    public Double getAverageDowntimeHours() {
        return averageDowntimeHours;
    }

    public void setAverageDowntimeHours(Double averageDowntimeHours) {
        this.averageDowntimeHours = averageDowntimeHours;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}