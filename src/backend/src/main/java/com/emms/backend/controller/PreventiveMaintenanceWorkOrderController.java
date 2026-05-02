package com.emms.backend.controller;

import com.emms.backend.dto.workorder.WorkOrderShowDTO;
import com.emms.backend.service.PreventiveMaintenanceService;
import com.emms.backend.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preventive-maintenances")
@RequiredArgsConstructor
public class PreventiveMaintenanceWorkOrderController {

    private final WorkOrderService workOrderService;
    private final PreventiveMaintenanceService preventiveMaintenanceService;

    @GetMapping("/{pmId}/work-orders")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER','ROLE_TECHNICIAN','ROLE_OPERATOR')")
    public ResponseEntity<List<WorkOrderShowDTO>> getWorkOrdersByPreventiveMaintenance(
            @PathVariable Long pmId
    ) {
        return ResponseEntity.ok(
                workOrderService.getByPreventiveMaintenance(pmId)
        );
    }

    @PostMapping("/{pmId}/generate-work-order")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<Void> generateWorkOrder(
            @PathVariable Long pmId
    ) {
        preventiveMaintenanceService.generateWorkOrder(pmId);
        return ResponseEntity.ok().build();
    }
}