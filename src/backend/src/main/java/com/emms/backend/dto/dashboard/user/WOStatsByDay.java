package com.emms.backend.dto.dashboard.user;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Thống kê work order theo ngày")
public class WOStatsByDay {

    @Schema(description = "Số lượng work order được tạo trong ngày")
    private Integer createdCount;

    @Schema(description = "Số lượng work order được hoàn thành trong ngày")
    private Integer completedCount;

    @Schema(description = "Ngày thống kê (yyyy-MM-dd)")
    private LocalDate date;

    // ===== Constructor =====

    public WOStatsByDay() {
    }

    public WOStatsByDay(Integer createdCount, Integer completedCount, LocalDate date) {
        this.createdCount = createdCount;
        this.completedCount = completedCount;
        this.date = date;
    }

    // ===== Getter & Setter =====

    public Integer getCreatedCount() {
        return createdCount;
    }

    public void setCreatedCount(Integer createdCount) {
        this.createdCount = createdCount;
    }

    public Integer getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    // ===== toString (debug dễ hơn) =====

    @Override
    public String toString() {
        return "WOStatsByDay{" +
                "createdCount=" + createdCount +
                ", completedCount=" + completedCount +
                ", date=" + date +
                '}';
    }
}