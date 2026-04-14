package com.emms.backend.dto.dashboard.workorder;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Thống kê thời gian work order theo tuần")
public class WOTimeByWeek {

    @Schema(description = "Tổng thời gian work order (giờ)")
    private Double totalHours;

    @Schema(description = "Thời gian work order phản ứng (giờ)")
    private Double reactiveHours;

    @Schema(description = "Ngày bắt đầu tuần (yyyy-MM-dd)")
    private LocalDate weekStart;

    // ===== Constructor =====

    public WOTimeByWeek() {
    }

    public WOTimeByWeek(Double totalHours, Double reactiveHours, LocalDate weekStart) {
        this.totalHours = totalHours;
        this.reactiveHours = reactiveHours;
        this.weekStart = weekStart;
    }

    // ===== Getter & Setter =====

    public Double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(Double totalHours) {
        this.totalHours = totalHours;
    }

    public Double getReactiveHours() {
        return reactiveHours;
    }

    public void setReactiveHours(Double reactiveHours) {
        this.reactiveHours = reactiveHours;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    // ===== toString =====

    @Override
    public String toString() {
        return "WOTimeByWeek{" +
                "totalHours=" + totalHours +
                ", reactiveHours=" + reactiveHours +
                ", weekStart=" + weekStart +
                '}';
    }
}