package com.emms.backend.dto.dashboard;

public class DashboardAlertDTO {

    private Long overdueWorkOrders;
    private Long assetsDown;
    private Long upcomingPM;

    public DashboardAlertDTO() {}

    public DashboardAlertDTO(Long overdueWorkOrders, Long assetsDown, Long upcomingPM) {
        this.overdueWorkOrders = overdueWorkOrders;
        this.assetsDown = assetsDown;
        this.upcomingPM = upcomingPM;
    }

    public Long getOverdueWorkOrders() { return overdueWorkOrders; }
    public void setOverdueWorkOrders(Long overdueWorkOrders) { this.overdueWorkOrders = overdueWorkOrders; }

    public Long getAssetsDown() { return assetsDown; }
    public void setAssetsDown(Long assetsDown) { this.assetsDown = assetsDown; }

    public Long getUpcomingPM() { return upcomingPM; }
    public void setUpcomingPM(Long upcomingPM) { this.upcomingPM = upcomingPM; }
}