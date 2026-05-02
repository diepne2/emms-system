package com.emms.backend.dto.dashboard.asset;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thống kê và chỉ số hiệu suất của tài sản")
public class AssetStats {

    @Schema(description = "Tổng thời gian ngừng hoạt động (tính bằng giây)")
    private Long totalDowntime;

    @Schema(description = "Tỷ lệ sẵn sàng của tài sản (%)")
    private Double availability;

    @Schema(description = "Số lần xảy ra downtime")
    private Integer downtimeEvents;


    public Long getTotalDowntime() {
        return totalDowntime;
    }

    public void setTotalDowntime(Long totalDowntime) {
        this.totalDowntime = totalDowntime;
    }

    public Double getAvailability() {
        return availability;
    }

    public void setAvailability(Double availability) {
        this.availability = availability;
    }

    public Integer getDowntimeEvents() {
        return downtimeEvents;
    }

    public void setDowntimeEvents(Integer downtimeEvents) {
        this.downtimeEvents = downtimeEvents;
    }
}