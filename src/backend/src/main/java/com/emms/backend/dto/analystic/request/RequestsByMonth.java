package com.emms.backend.dto.analystic.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.YearMonth;

@Schema(description = "Thống kê request theo tháng")
public class RequestsByMonth {

    @Schema(description = "Thời gian xử lý trung bình (ngày)")
    private Double averageCycleTimeDays;

    @Schema(description = "Tháng thống kê (yyyy-MM)")
    private YearMonth month;

    // ===== Constructor =====

    public RequestsByMonth() {
    }

    public RequestsByMonth(Double averageCycleTimeDays, YearMonth month) {
        this.averageCycleTimeDays = averageCycleTimeDays;
        this.month = month;
    }

    // ===== Getter & Setter =====

    public Double getAverageCycleTimeDays() {
        return averageCycleTimeDays;
    }

    public void setAverageCycleTimeDays(Double averageCycleTimeDays) {
        this.averageCycleTimeDays = averageCycleTimeDays;
    }

    public YearMonth getMonth() {
        return month;
    }

    public void setMonth(YearMonth month) {
        this.month = month;
    }

    // ===== toString =====

    @Override
    public String toString() {
        return "RequestsByMonth{" +
                "averageCycleTimeDays=" + averageCycleTimeDays +
                ", month=" + month +
                '}';
    }
}