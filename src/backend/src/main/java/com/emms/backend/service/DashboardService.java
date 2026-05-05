package com.emms.backend.service;

import com.emms.backend.dto.dashboard.DashboardAlertDTO;
import com.emms.backend.dto.dashboard.DashboardCountDTO;
import com.emms.backend.dto.dashboard.DashboardKpiDTO;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    DashboardKpiDTO getKpi(LocalDate fromDate, LocalDate toDate);

    List<DashboardCountDTO> getWorkOrderStatus(LocalDate fromDate, LocalDate toDate);

    List<DashboardCountDTO> getMaintenanceType(LocalDate fromDate, LocalDate toDate);

    DashboardAlertDTO getAlerts();
}