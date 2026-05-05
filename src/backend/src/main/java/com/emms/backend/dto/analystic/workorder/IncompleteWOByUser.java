package com.emms.backend.dto.analystic.workorder;

import com.emms.backend.dto.user.UserSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thống kê work order chưa hoàn thành theo người dùng")
public class IncompleteWOByUser extends UserSummaryDTO {

    @Schema(description = "Số lượng work order chưa hoàn thành")
    private Integer incompleteCount;

    @Schema(description = "Tuổi trung bình của work order (tính bằng ngày)")
    private Double averageAgeDays;

    public IncompleteWOByUser() {
    }

    public IncompleteWOByUser(Integer incompleteCount, Double averageAgeDays) {
        this.incompleteCount = incompleteCount;
        this.averageAgeDays = averageAgeDays;
    }

    public Integer getIncompleteCount() {
        return incompleteCount;
    }

    public void setIncompleteCount(Integer incompleteCount) {
        this.incompleteCount = incompleteCount;
    }

    public Double getAverageAgeDays() {
        return averageAgeDays;
    }

    public void setAverageAgeDays(Double averageAgeDays) {
        this.averageAgeDays = averageAgeDays;
    }

    @Override
    public String toString() {
        return "IncompleteWOByUser{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", incompleteCount=" + incompleteCount +
                ", averageAgeDays=" + averageAgeDays +
                '}';
    }
}