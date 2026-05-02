package com.emms.backend.dto.dashboard.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thống kê và chỉ số KPI của request")
public class RequestStats {

    @Schema(description = "Số lượng request đã được duyệt")
    private Integer approvedCount;

    @Schema(description = "Số lượng request đang chờ xử lý")
    private Integer pendingCount;

    @Schema(description = "Số lượng request đã bị hủy")
    private Integer cancelledCount;

    @Schema(description = "Thời gian xử lý trung bình (giờ)")
    private Double averageCycleTimeHours;



    public RequestStats() {
    }

    public RequestStats(Integer approvedCount,
                        Integer pendingCount,
                        Integer cancelledCount,
                        Double averageCycleTimeHours) {
        this.approvedCount = approvedCount;
        this.pendingCount = pendingCount;
        this.cancelledCount = cancelledCount;
        this.averageCycleTimeHours = averageCycleTimeHours;
    }


    public Integer getApprovedCount() {
        return approvedCount;
    }

    public void setApprovedCount(Integer approvedCount) {
        this.approvedCount = approvedCount;
    }

    public Integer getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Integer pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Integer getCancelledCount() {
        return cancelledCount;
    }

    public void setCancelledCount(Integer cancelledCount) {
        this.cancelledCount = cancelledCount;
    }

    public Double getAverageCycleTimeHours() {
        return averageCycleTimeHours;
    }

    public void setAverageCycleTimeHours(Double averageCycleTimeHours) {
        this.averageCycleTimeHours = averageCycleTimeHours;
    }



    @Override
    public String toString() {
        return "RequestStats{" +
                "approvedCount=" + approvedCount +
                ", pendingCount=" + pendingCount +
                ", cancelledCount=" + cancelledCount +
                ", averageCycleTimeHours=" + averageCycleTimeHours +
                '}';
    }
}