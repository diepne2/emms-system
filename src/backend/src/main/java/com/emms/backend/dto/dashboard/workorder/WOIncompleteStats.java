package com.emms.backend.dto.dashboard.workorder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thống kê work order chưa hoàn thành")
public class WOIncompleteStats {

    @Schema(description = "Tổng số work order chưa hoàn thành")
    private Integer totalIncompleteCount;

    @Schema(description = "Tuổi trung bình của work order (tính bằng ngày)")
    private Double averageAgeDays;


    public WOIncompleteStats() {
    }

    public WOIncompleteStats(Integer totalIncompleteCount, Double averageAgeDays) {
        this.totalIncompleteCount = totalIncompleteCount;
        this.averageAgeDays = averageAgeDays;
    }

    public Integer getTotalIncompleteCount() {
        return totalIncompleteCount;
    }

    public void setTotalIncompleteCount(Integer totalIncompleteCount) {
        this.totalIncompleteCount = totalIncompleteCount;
    }

    public Double getAverageAgeDays() {
        return averageAgeDays;
    }

    public void setAverageAgeDays(Double averageAgeDays) {
        this.averageAgeDays = averageAgeDays;
    }


    @Override
    public String toString() {
        return "WOIncompleteStats{" +
                "totalIncompleteCount=" + totalIncompleteCount +
                ", averageAgeDays=" + averageAgeDays +
                '}';
    }
}