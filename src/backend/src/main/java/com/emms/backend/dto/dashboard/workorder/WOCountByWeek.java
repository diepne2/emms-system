package com.emms.backend.dto.dashboard.workorder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Thống kê work order theo tuần")
public class WOCountByWeek {

    @Schema(description = "Tổng số work order trong tuần")
    private Integer totalCount;

    @Schema(description = "Số work order bảo trì định kỳ (compliant)")
    private Integer compliantCount;

    @Schema(description = "Số work order phản ứng (reactive)")
    private Integer reactiveCount;

    @Schema(description = "Ngày bắt đầu tuần (yyyy-MM-dd)")
    private LocalDate weekStart;

    // ===== Constructor =====

    public WOCountByWeek() {
    }

    public WOCountByWeek(Integer totalCount, Integer compliantCount, Integer reactiveCount, LocalDate weekStart) {
        this.totalCount = totalCount;
        this.compliantCount = compliantCount;
        this.reactiveCount = reactiveCount;
        this.weekStart = weekStart;
    }

    // ===== Getter & Setter =====

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getCompliantCount() {
        return compliantCount;
    }

    public void setCompliantCount(Integer compliantCount) {
        this.compliantCount = compliantCount;
    }

    public Integer getReactiveCount() {
        return reactiveCount;
    }

    public void setReactiveCount(Integer reactiveCount) {
        this.reactiveCount = reactiveCount;
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
        return "WOCountByWeek{" +
                "totalCount=" + totalCount +
                ", compliantCount=" + compliantCount +
                ", reactiveCount=" + reactiveCount +
                ", weekStart=" + weekStart +
                '}';
    }
}