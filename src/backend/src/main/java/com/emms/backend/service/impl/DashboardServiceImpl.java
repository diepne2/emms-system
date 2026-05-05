package com.emms.backend.service.impl;

import com.emms.backend.dto.dashboard.DashboardAlertDTO;
import com.emms.backend.dto.dashboard.DashboardCountDTO;
import com.emms.backend.dto.dashboard.DashboardKpiDTO;
import com.emms.backend.entity.WorkOrder.WorkOrderStatus;
import com.emms.backend.repository.AssetRepository;
import com.emms.backend.repository.PreventiveMaintenanceRepository;
import com.emms.backend.repository.WorkOrderRepository;
import com.emms.backend.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final LocalDate DEFAULT_FROM_DATE = LocalDate.of(2000, 1, 1);

    private final WorkOrderRepository workOrderRepository;
    private final AssetRepository assetRepository;
    private final PreventiveMaintenanceRepository preventiveMaintenanceRepository;

    public DashboardServiceImpl(
            WorkOrderRepository workOrderRepository,
            AssetRepository assetRepository,
            PreventiveMaintenanceRepository preventiveMaintenanceRepository
    ) {
        this.workOrderRepository = workOrderRepository;
        this.assetRepository = assetRepository;
        this.preventiveMaintenanceRepository = preventiveMaintenanceRepository;
    }

    @Override
    public DashboardKpiDTO getKpi(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = toStart(fromDate);
        LocalDateTime to = toEnd(toDate);

        long totalWorkOrders = workOrderRepository.countByDateRange(from, to);

        long completedWorkOrders = workOrderRepository.countByStatusAndDateRange(
                WorkOrderStatus.DONE,
                from,
                to
        );

        long openWorkOrders = workOrderRepository.countByStatusAndDateRange(
                WorkOrderStatus.OPEN,
                from,
                to
        );

        long inProgressWorkOrders = workOrderRepository.countByStatusAndDateRange(
                WorkOrderStatus.IN_PROGRESS,
                from,
                to
        );

        long overdueWorkOrders = workOrderRepository.countOverdue(
                LocalDate.now(),
                List.of(WorkOrderStatus.DONE)
        );

        long totalAssetsDown = assetRepository.countAssetsDown();

        double completionRate = totalWorkOrders == 0
                ? 0.0
                : Math.round((completedWorkOrders * 10000.0 / totalWorkOrders)) / 100.0;

        return new DashboardKpiDTO(
                totalWorkOrders,
                completedWorkOrders,
                openWorkOrders,
                inProgressWorkOrders,
                overdueWorkOrders,
                totalAssetsDown,
                completionRate
        );
    }

    @Override
    public List<DashboardCountDTO> getWorkOrderStatus(LocalDate fromDate, LocalDate toDate) {
        return workOrderRepository.countGroupByStatus(
                toStart(fromDate),
                toEnd(toDate)
        );
    }

    @Override
    public List<DashboardCountDTO> getMaintenanceType(LocalDate fromDate, LocalDate toDate) {
        return workOrderRepository.countGroupByMaintenanceType(
                toStart(fromDate),
                toEnd(toDate)
        );
    }

    @Override
    public DashboardAlertDTO getAlerts() {
        LocalDate today = LocalDate.now();

        long overdueWorkOrders = workOrderRepository.countOverdue(
                today,
                List.of(WorkOrderStatus.DONE)
        );

        long assetsDown = assetRepository.countAssetsDown();

        long upcomingPM = preventiveMaintenanceRepository.countUpcomingPM(
                today,
                today.plusDays(7)
        );

        return new DashboardAlertDTO(
                overdueWorkOrders,
                assetsDown,
                upcomingPM
        );
    }

    private LocalDateTime toStart(LocalDate date) {
        LocalDate safeDate = date == null ? DEFAULT_FROM_DATE : date;
        return safeDate.atStartOfDay();
    }

    private LocalDateTime toEnd(LocalDate date) {
        LocalDate safeDate = date == null ? LocalDate.now() : date;
        return safeDate.plusDays(1).atStartOfDay();
    }
}