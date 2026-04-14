package com.emms.backend.dto.dashboard.asset;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Tổng quan các chỉ số hiệu suất của tài sản (Asset)")
public class AssetOverview {

    @Schema(description = "Thời gian trung bình giữa hai lần hỏng (MTBF) - tính bằng giây")
    private Double mtbf;

    @Schema(description = "Thời gian trung bình để sửa chữa (MTTR) - tính bằng giây")
    private Double mttr;

    @Schema(description = "Tổng thời gian ngừng hoạt động (downtime) - tính bằng giây")
    private Long totalDowntime;

    @Schema(description = "Tổng thời gian hoạt động (uptime) - tính bằng giây")
    private Long totalUptime;

    @Schema(description = "Tổng chi phí liên quan đến tài sản")
    private BigDecimal totalCost;

    // ===== Getter & Setter =====

    public Double getMtbf() {
        return mtbf;
    }

    public void setMtbf(Double mtbf) {
        this.mtbf = mtbf;
    }

    public Double getMttr() {
        return mttr;
    }

    public void setMttr1(Double mttr) {
        this.mttr = mttr;
    }

    public Long getTotalDowntime() {
        return totalDowntime;
    }

    public void setTotalDowntime(Long totalDowntime) {
        this.totalDowntime = totalDowntime;
    }

    public Long getTotalUptime() {
        return totalUptime;
    }

    public void setTotalUptime(Long totalUptime) {
        this.totalUptime = totalUptime;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public void setMttr(Double mtbf2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setMttr'");
    }
}