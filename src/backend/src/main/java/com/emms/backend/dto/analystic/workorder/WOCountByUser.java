package com.emms.backend.dto.analystic.workorder;

import com.emms.backend.dto.user.UserSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Số lượng work order theo người dùng")
public class WOCountByUser extends UserSummaryDTO {

    @Schema(description = "Tổng số work order")
    private Integer totalCount;

    public WOCountByUser() {
    }

    public WOCountByUser(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public String toString() {
        return "WOCountByUser{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", totalCount=" + totalCount +
                '}';
    }
}