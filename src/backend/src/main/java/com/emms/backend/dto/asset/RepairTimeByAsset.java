package com.emms.backend.dto.asset;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thời gian sửa chữa trung bình (MTTR) theo từng tài sản")
public class RepairTimeByAsset extends AssetSummaryDTO {

    @Schema(description = "Thời gian sửa chữa trung bình (giờ)")
    private Double mttrHours;

    public RepairTimeByAsset() {
    }

    public RepairTimeByAsset(Double mttrHours) {
        this.mttrHours = mttrHours;
    }

    public Double getMttrHours() {
        return mttrHours;
    }

    public void setMttrHours(Double mttrHours) {
        this.mttrHours = mttrHours;
    }
}