package com.emms.backend.dto.analystic.workorder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thống kê work order theo mức độ ưu tiên")
public class WOStatsByPriority {

    @Schema(description = "Thống kê priority NONE")
    private BasicStats nonePriority;

    @Schema(description = "Thống kê priority LOW")
    private BasicStats lowPriority;

    @Schema(description = "Thống kê priority MEDIUM")
    private BasicStats mediumPriority;

    @Schema(description = "Thống kê priority HIGH")
    private BasicStats highPriority;


    public WOStatsByPriority() {
    }

    public WOStatsByPriority(BasicStats nonePriority,
                             BasicStats lowPriority,
                             BasicStats mediumPriority,
                             BasicStats highPriority) {
        this.nonePriority = nonePriority;
        this.lowPriority = lowPriority;
        this.mediumPriority = mediumPriority;
        this.highPriority = highPriority;
    }


    public BasicStats getNonePriority() {
        return nonePriority;
    }

    public void setNonePriority(BasicStats nonePriority) {
        this.nonePriority = nonePriority;
    }

    public BasicStats getLowPriority() {
        return lowPriority;
    }

    public void setLowPriority(BasicStats lowPriority) {
        this.lowPriority = lowPriority;
    }

    public BasicStats getMediumPriority() {
        return mediumPriority;
    }

    public void setMediumPriority(BasicStats mediumPriority) {
        this.mediumPriority = mediumPriority;
    }

    public BasicStats getHighPriority() {
        return highPriority;
    }

    public void setHighPriority(BasicStats highPriority) {
        this.highPriority = highPriority;
    }


    @Override
    public String toString() {
        return "WOStatsByPriority{" +
                "nonePriority=" + nonePriority +
                ", lowPriority=" + lowPriority +
                ", mediumPriority=" + mediumPriority +
                ", highPriority=" + highPriority +
                '}';
    }


    @Schema(description = "Thống kê cơ bản cho một mức độ ưu tiên")
    public static class BasicStats {

        @Schema(description = "Số lượng work order")
        private Integer count;

        @Schema(description = "Tổng thời gian ước tính (giờ)")
        private Double estimatedHours;

        public BasicStats() {
        }

        public BasicStats(Integer count, Double estimatedHours) {
            this.count = count;
            this.estimatedHours = estimatedHours;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public Double getEstimatedHours() {
            return estimatedHours;
        }

        public void setEstimatedHours(Double estimatedHours) {
            this.estimatedHours = estimatedHours;
        }


        @Override
        public String toString() {
            return "BasicStats{" +
                    "count=" + count +
                    ", estimatedHours=" + estimatedHours +
                    '}';
        }
    }
}