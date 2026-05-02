package com.emms.backend.dto.dashboard.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Thống kê số lượng request nhận và xử lý theo ngày")
public class RequestsResolvedByDate {

    @Schema(description = "Số lượng request nhận trong ngày")
    private Integer receivedCount;

    @Schema(description = "Số lượng request đã xử lý trong ngày")
    private Integer resolvedCount;

    @Schema(description = "Ngày thống kê (yyyy-MM-dd)")
    private LocalDate date;


    public RequestsResolvedByDate() {
    }

    public RequestsResolvedByDate(Integer receivedCount, Integer resolvedCount, LocalDate date) {
        this.receivedCount = receivedCount;
        this.resolvedCount = resolvedCount;
        this.date = date;
    }


    public Integer getReceivedCount() {
        return receivedCount;
    }

    public void setReceivedCount(Integer receivedCount) {
        this.receivedCount = receivedCount;
    }

    public Integer getResolvedCount() {
        return resolvedCount;
    }

    public void setResolvedCount(Integer resolvedCount) {
        this.resolvedCount = resolvedCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }


    @Override
    public String toString() {
        return "RequestsResolvedByDate{" +
                "receivedCount=" + receivedCount +
                ", resolvedCount=" + resolvedCount +
                ", date=" + date +
                '}';
    }
}