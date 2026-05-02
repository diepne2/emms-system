package com.emms.backend.dto.dashboard.asset;

import com.emms.backend.dto.asset.AssetSummaryDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thống kê số lần downtime theo từng thiết bị")
public class DowntimesByAsset extends AssetSummaryDTO {

    @Schema(description = "Số lần xảy ra downtime")
    private Integer downtimeCount;

    @Schema(description = "Tỷ lệ downtime (%)")
    private Double downtimePercentage;

    public DowntimesByAsset() {
    }

    public DowntimesByAsset(Integer downtimeCount, Double downtimePercentage) {
        this.downtimeCount = downtimeCount;
        this.downtimePercentage = downtimePercentage;
    }

    public Integer getDowntimeCount() {
        return downtimeCount;
    }

    public void setDowntimeCount(Integer downtimeCount) {
        this.downtimeCount = downtimeCount;
    }

    public Double getDowntimePercentage() {
        return downtimePercentage;
    }

    public void setDowntimePercentage(Double downtimePercentage) {
        this.downtimePercentage = downtimePercentage;
    }
}