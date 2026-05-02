package com.emms.backend.dto.dashboard.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thống kê request theo mức độ ưu tiên")
public class RequestStatsByPriority {

    @Schema(description = "Thống kê priority NONE")
    private BasicStats nonePriority;

    @Schema(description = "Thống kê priority LOW")
    private BasicStats lowPriority;

    @Schema(description = "Thống kê priority MEDIUM")
    private BasicStats mediumPriority;

    @Schema(description = "Thống kê priority HIGH")
    private BasicStats highPriority;

    public RequestStatsByPriority() {
    }

    public RequestStatsByPriority(BasicStats nonePriority,
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
        return "RequestStatsByPriority{" +
                "nonePriority=" + nonePriority +
                ", lowPriority=" + lowPriority +
                ", mediumPriority=" + mediumPriority +
                ", highPriority=" + highPriority +
                '}';
    }



    @Schema(description = "Thống kê cơ bản theo priority")
    public static class BasicStats {

        @Schema(description = "Số lượng request")
        private Integer requestCount;

    

        public BasicStats() {
        }

        public BasicStats(Integer requestCount) {
            this.requestCount = requestCount;
        }


        public Integer getRequestCount() {
            return requestCount;
        }

        public void setRequestCount(Integer requestCount) {
            this.requestCount = requestCount;
        }

  

        @Override
        public String toString() {
            return "BasicStats{" +
                    "requestCount=" + requestCount +
                    '}';
        }
    }
}