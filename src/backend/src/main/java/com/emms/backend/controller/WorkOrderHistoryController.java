package com.emms.backend.controller;

import com.emms.backend.dto.wo_history.WorkOrderHistoryShowDTO;
import com.emms.backend.service.WorkOrderHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-order-histories")
public class WorkOrderHistoryController {

    private final WorkOrderHistoryService workOrderHistoryService;

    public WorkOrderHistoryController(WorkOrderHistoryService workOrderHistoryService) {
        this.workOrderHistoryService = workOrderHistoryService;
    }

    @GetMapping("/work-order/{workOrderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorkOrderHistoryShowDTO>> getByWorkOrder(@PathVariable Long workOrderId) {
        return ResponseEntity.ok(workOrderHistoryService.getHistoryByWorkOrder(workOrderId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workOrderHistoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/done-cancelled")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorkOrderHistoryShowDTO>> getDoneAndCancelled() {
        return ResponseEntity.ok(workOrderHistoryService.getDoneAndCancelledHistories());

    }
    
}