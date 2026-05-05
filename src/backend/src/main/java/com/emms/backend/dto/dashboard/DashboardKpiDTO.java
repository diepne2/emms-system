package com.emms.backend.dto.dashboard;

public class DashboardKpiDTO {

    private Long totalWorkOrders;
    private Long completedWorkOrders;
    private Long openWorkOrders;
    private Long inProgressWorkOrders;
    private Long overdueWorkOrders;
    private Long totalAssetsDown;
    private Double completionRate;

    public DashboardKpiDTO() {}

    public DashboardKpiDTO(Long totalWorkOrders, Long completedWorkOrders,
                           Long openWorkOrders, Long inProgressWorkOrders,
                           Long overdueWorkOrders, Long totalAssetsDown,
                           Double completionRate) {
        this.totalWorkOrders = totalWorkOrders;
        this.completedWorkOrders = completedWorkOrders;
        this.openWorkOrders = openWorkOrders;
        this.inProgressWorkOrders = inProgressWorkOrders;
        this.overdueWorkOrders = overdueWorkOrders;
        this.totalAssetsDown = totalAssetsDown;
        this.completionRate = completionRate;
    }

    public Long getTotalWorkOrders() { return totalWorkOrders; }
    public void setTotalWorkOrders(Long totalWorkOrders) { this.totalWorkOrders = totalWorkOrders; }

    public Long getCompletedWorkOrders() { return completedWorkOrders; }
    public void setCompletedWorkOrders(Long completedWorkOrders) { this.completedWorkOrders = completedWorkOrders; }

    public Long getOpenWorkOrders() { return openWorkOrders; }
    public void setOpenWorkOrders(Long openWorkOrders) { this.openWorkOrders = openWorkOrders; }

    public Long getInProgressWorkOrders() { return inProgressWorkOrders; }
    public void setInProgressWorkOrders(Long inProgressWorkOrders) { this.inProgressWorkOrders = inProgressWorkOrders; }

    public Long getOverdueWorkOrders() { return overdueWorkOrders; }
    public void setOverdueWorkOrders(Long overdueWorkOrders) { this.overdueWorkOrders = overdueWorkOrders; }

    public Long getTotalAssetsDown() { return totalAssetsDown; }
    public void setTotalAssetsDown(Long totalAssetsDown) { this.totalAssetsDown = totalAssetsDown; }

    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
}