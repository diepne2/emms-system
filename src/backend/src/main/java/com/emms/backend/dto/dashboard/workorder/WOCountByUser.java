package com.emms.backend.dto.dashboard.workorder;

import com.emms.backend.dto.user.UserSummaryDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Số lượng work order theo người dùng")
public class WOCountByUser extends UserSummaryDTO {

    @Schema(description = "Tổng số work order")
    private Integer totalCount;

    // ===== Constructor =====

    public WOCountByUser() {
    }

    public WOCountByUser(Integer totalCount) {
        this.totalCount = totalCount;
    }

    // ===== Getter & Setter =====

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    // ===== toString =====

    @Override
    public String toString() {
        return "WOCountByUser{" +
                "totalCount=" + totalCount +
                '}';
    }

    public void setUsername(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setUsername'");
    }
}