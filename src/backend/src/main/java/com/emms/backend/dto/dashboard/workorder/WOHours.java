package com.emms.backend.dto.dashboard.workorder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "So sánh thời gian ước tính và thực tế của work order")
public class WOHours {

    @Schema(description = "Thời gian ước tính (giờ)")
    private Double estimatedHours;

    @Schema(description = "Thời gian thực tế (giờ)")
    private Double actualHours;


    public WOHours() {
    }

    public WOHours(Double estimatedHours, Double actualHours) {
        this.estimatedHours = estimatedHours;
        this.actualHours = actualHours;
    }


    public Double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Double getActualHours() {
        return actualHours;
    }

    public void setActualHours(Double actualHours) {
        this.actualHours = actualHours;
    }


    @Override
    public String toString() {
        return "WOHours{" +
                "estimatedHours=" + estimatedHours +
                ", actualHours=" + actualHours +
                '}';
    }
}