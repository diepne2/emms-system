package com.emms.backend.dto.dashboard.asset;

import com.emms.backend.dto.asset.AssetSummaryDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thời gian trung bình giữa hai lần hỏng (MTBF) theo từng tài sản")
public class MTBFByAsset extends AssetSummaryDTO {

    @Schema(description = "MTBF (tính bằng giờ)")
    private Double mtbfHours;

    public MTBFByAsset() {
    }

    public MTBFByAsset(Double mtbfHours) {
        this.mtbfHours = mtbfHours;
    }

    public Double getMtbfHours() {
        return mtbfHours;
    }

    public void setMtbfHours(Double mtbfHours) {
        this.mtbfHours = mtbfHours;
    }
}
