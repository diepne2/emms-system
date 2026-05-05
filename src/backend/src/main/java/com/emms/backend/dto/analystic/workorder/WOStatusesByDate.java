package com.emms.backend.dto.analystic.workorder;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Thống kê số lượng work order theo trạng thái và theo ngày")
public class WOStatusesByDate extends WOStatuses {

    @Schema(description = "Ngày thống kê (yyyy-MM-dd)")
    private LocalDate date;

    public WOStatusesByDate() {
        super();
    }

    public WOStatusesByDate(LocalDate date) {
        super();
        this.date = date;
    }

    public WOStatusesByDate(Integer pendingCount,
                            Integer assignedCount,
                            Integer inProgressCount,
                            Integer onHoldCount,
                            Integer awaitingConfirmationCount,
                            Integer completedCount,
                            Integer rejectedCount,
                            LocalDate date) {
        super(
                pendingCount,
                assignedCount,
                inProgressCount,
                onHoldCount,
                awaitingConfirmationCount,
                completedCount,
                rejectedCount
        );
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "WOStatusesByDate{" +
                "date=" + date +
                ", pendingCount=" + getPendingCount() +
                ", assignedCount=" + getAssignedCount() +
                ", inProgressCount=" + getInProgressCount() +
                ", onHoldCount=" + getOnHoldCount() +
                ", awaitingConfirmationCount=" + getAwaitingConfirmationCount() +
                ", completedCount=" + getCompletedCount() +
                ", rejectedCount=" + getRejectedCount() +
                '}';
    }
}