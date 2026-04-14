package com.emms.backend.dto.dashboard.workorder;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thống kê và chỉ số KPI của work order")
public class WOStats {

    @Schema(description = "Tổng số work order")
    private Integer totalCount;

    @Schema(description = "Số work order đã hoàn thành")
    private Integer completedCount;

    @Schema(description = "Số work order compliant (đúng kế hoạch)")
    private Integer compliantCount;

    @Schema(description = "Thời gian hoàn thành trung bình (giờ)")
    private Double averageCycleTimeHours;

    @Schema(description = "Thời gian trung bình để phản hồi (MTTA - giờ)")
    private Double mttaHours;

    @Schema(description = "Tỷ lệ hoàn thành (%)")
    private Double completionRate;

    @Schema(description = "Tỷ lệ compliant (%)")
    private Double complianceRate;

    // ===== Constructor =====

    public WOStats() {
    }

    public WOStats(Integer totalCount,
                   Integer completedCount,
                   Integer compliantCount,
                   Double averageCycleTimeHours,
                   Double mttaHours,
                   Double completionRate,
                   Double complianceRate) {
        this.totalCount = totalCount;
        this.completedCount = completedCount;
        this.compliantCount = compliantCount;
        this.averageCycleTimeHours = averageCycleTimeHours;
        this.mttaHours = mttaHours;
        this.completionRate = completionRate;
        this.complianceRate = complianceRate;
    }

    // ===== Getter & Setter =====

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    public Integer getCompliantCount() {
        return compliantCount;
    }

    public void setCompliantCount(Integer compliantCount) {
        this.compliantCount = compliantCount;
    }

    public Double getAverageCycleTimeHours() {
        return averageCycleTimeHours;
    }

    public void setAverageCycleTimeHours(Double averageCycleTimeHours) {
        this.averageCycleTimeHours = averageCycleTimeHours;
    }

    public Double getMttaHours() {
        return mttaHours;
    }

    public void setMttaHours(Double mttaHours) {
        this.mttaHours = mttaHours;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public Double getComplianceRate() {
        return complianceRate;
    }

    public void setComplianceRate(Double complianceRate) {
        this.complianceRate = complianceRate;
    }

    // ===== toString =====

    @Override
    public String toString() {
        return "WOStats{" +
                "totalCount=" + totalCount +
                ", completedCount=" + completedCount +
                ", compliantCount=" + compliantCount +
                ", averageCycleTimeHours=" + averageCycleTimeHours +
                ", mttaHours=" + mttaHours +
                ", completionRate=" + completionRate +
                ", complianceRate=" + complianceRate +
                '}';
    }
}