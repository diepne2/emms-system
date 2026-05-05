package com.emms.backend.controller;

import com.emms.backend.dto.dashboard.*;
import com.emms.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/kpi")
    public ResponseEntity<DashboardKpiDTO> getKpi(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(dashboardService.getKpi(fromDate, toDate));
    }

    @GetMapping("/wo-status")
    public ResponseEntity<List<DashboardCountDTO>> getWorkOrderStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(dashboardService.getWorkOrderStatus(fromDate, toDate));
    }

    @GetMapping("/maintenance-type")
    public ResponseEntity<List<DashboardCountDTO>> getMaintenanceType(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(dashboardService.getMaintenanceType(fromDate, toDate));
    }

    @GetMapping("/alerts")
    public ResponseEntity<DashboardAlertDTO> getAlerts() {
        return ResponseEntity.ok(dashboardService.getAlerts());
    }
}