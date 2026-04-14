package com.emms.backend.controller;

import com.emms.backend.entity.WorkOrderRequestConfiguration;
import com.emms.backend.service.WorkOrderRequestConfigurationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/work-order-request-configurations")
public class WorkOrderRequestConfigurationController {

    private final WorkOrderRequestConfigurationService workOrderRequestConfigurationService;

    public WorkOrderRequestConfigurationController(
            WorkOrderRequestConfigurationService workOrderRequestConfigurationService
    ) {
        this.workOrderRequestConfigurationService = workOrderRequestConfigurationService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public WorkOrderRequestConfiguration getById(@PathVariable("id") Long id) {
        return workOrderRequestConfigurationService.findById(id);
    }
}