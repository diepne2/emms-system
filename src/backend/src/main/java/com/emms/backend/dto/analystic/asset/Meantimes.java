package com.emms.backend.dto.analystic.asset;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Các chỉ số thời gian trung bình của tài sản")
public class Meantimes {

    @Schema(description = "Thời gian trung bình giữa hai lần hỏng (MTBF) - tính bằng giờ")
    private Double mtbfHours;

    @Schema(description = "Thời gian trung bình giữa hai lần bảo trì - tính bằng giờ")
    private Double maintenanceIntervalHours;

    public Meantimes() {
    }

    public Meantimes(Double mtbfHours, Double maintenanceIntervalHours) {
        this.mtbfHours = mtbfHours;
        this.maintenanceIntervalHours = maintenanceIntervalHours;
    }

    public Double getMtbfHours() {
        return mtbfHours;
    }

    public void setMtbfHours(Double mtbfHours) {
        this.mtbfHours = mtbfHours;
    }

    public Double getMaintenanceIntervalHours() {
        return maintenanceIntervalHours;
    }

    public void setMaintenanceIntervalHours(Double maintenanceIntervalHours) {
        this.maintenanceIntervalHours = maintenanceIntervalHours;
    }
}