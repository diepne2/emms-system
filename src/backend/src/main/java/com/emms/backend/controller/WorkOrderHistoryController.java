package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.entity.WorkOrderHistory;
import com.emms.backend.service.WorkOrderHistoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/work-order-histories")
public class WorkOrderHistoryController {

    private final WorkOrderHistoryService workOrderHistoryService;

    public WorkOrderHistoryController(WorkOrderHistoryService workOrderHistoryService) {
        this.workOrderHistoryService = workOrderHistoryService;
    }

    /**
     * Lấy tất cả work order histories
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<Collection<WorkOrderHistory>> getAll() {
        return ResponseEntity.ok(workOrderHistoryService.getAll());
    }

    /**
     * Lấy work order history theo id
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<WorkOrderHistory> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(workOrderHistoryService.findEntityById(id));
    }

    /**
     * Lấy lịch sử theo work order id
     */
    @GetMapping("/work-order/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<Collection<WorkOrderHistory>> getByWorkOrder(@PathVariable("id") Long id) {
        return ResponseEntity.ok(workOrderHistoryService.findByWorkOrder(id));
    }

    /**
     * Tạo mới work order history
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<WorkOrderHistory> create(
            @Valid @RequestBody WorkOrderHistory payload
    ) {
        WorkOrderHistory created = workOrderHistoryService.create(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật work order history
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<WorkOrderHistory> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody WorkOrderHistory payload
    ) {
        payload.setId(id);
        return ResponseEntity.ok(workOrderHistoryService.update(payload));
    }

    /**
     * Xóa work order history
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable("id") Long id) {
        workOrderHistoryService.delete(id);
        return ResponseEntity.ok(new SuccessResponse(true, "Deleted successfully"));
    }
}