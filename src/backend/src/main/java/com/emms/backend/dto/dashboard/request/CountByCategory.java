package com.emms.backend.dto.dashboard.request;

import com.emms.backend.dto.category.CategorySummaryDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thống kê số lượng request theo category")
public class CountByCategory extends CategorySummaryDTO {

    @Schema(description = "Số lượng request")
    private Integer requestCount;


    public CountByCategory() {
    }

    public CountByCategory(Integer requestCount) {
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
        return "CountByCategory{" +
                "requestCount=" + requestCount +
                '}';
    }
}