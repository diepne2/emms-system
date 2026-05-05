package com.emms.backend.dto.analystic.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thống kê work order theo người dùng")
public class UserWOStats {

    @Schema(description = "Số lượng work order đã tạo")
    private Integer createdCount;

    @Schema(description = "Số lượng work order đã hoàn thành")
    private Integer completedCount;

    @Schema(description = "Tỷ lệ hoàn thành (%)")
    private Double completionRate;

    public UserWOStats() {
    }

    public UserWOStats(Integer createdCount, Integer completedCount, Double completionRate) {
        this.createdCount = createdCount;
        this.completedCount = completedCount;
        this.completionRate = completionRate;
    }

    public Integer getCreatedCount() {
        return createdCount;
    }

    public void setCreatedCount(Integer createdCount) {
        this.createdCount = createdCount;
    }

    public Integer getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }
}