package com.emms.backend.dto.dashboard.asset;


import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Thống kê downtime và chi phí theo ngày")
public class DowntimesByDate {

    @Schema(description = "Tổng thời gian downtime (giây)")
    private Long totalDowntime;

    @Schema(description = "Tổng chi phí work order")
    private BigDecimal totalWorkOrderCost;

    @Schema(description = "Ngày (yyyy-MM-dd)")
    private LocalDate date;

    public DowntimesByDate() {
    }

    public DowntimesByDate(Long totalDowntime, BigDecimal totalWorkOrderCost, LocalDate date) {
        this.totalDowntime = totalDowntime;
        this.totalWorkOrderCost = totalWorkOrderCost;
        this.date = date;
    }

    public Long getTotalDowntime() {
        return totalDowntime;
    }

    public void setTotalDowntime(Long totalDowntime) {
        this.totalDowntime = totalDowntime;
    }

    public BigDecimal getTotalWorkOrderCost() {
        return totalWorkOrderCost;
    }

    public void setTotalWorkOrderCost(BigDecimal totalWorkOrderCost) {
        this.totalWorkOrderCost = totalWorkOrderCost;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}