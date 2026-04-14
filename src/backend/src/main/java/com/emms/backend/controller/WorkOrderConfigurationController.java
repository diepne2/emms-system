package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.entity.WorkOrderConfiguration;
import com.emms.backend.exception.CustomException;
import com.emms.backend.service.WorkOrderConfigurationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/work-order-configurations")
public class WorkOrderConfigurationController {

    private final WorkOrderConfigurationService workOrderConfigurationService;

    public WorkOrderConfigurationController(WorkOrderConfigurationService workOrderConfigurationService) {
        this.workOrderConfigurationService = workOrderConfigurationService;
    }

    /**
     * Lấy tất cả cấu hình work order
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<Collection<WorkOrderConfiguration>> getAll() {
        return ResponseEntity.ok(workOrderConfigurationService.getAll());
    }

    /**
     * Lấy cấu hình theo id
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<WorkOrderConfiguration> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(workOrderConfigurationService.findEntityById(id));
    }

    /**
     * Lấy cấu hình theo configCode
     * Example: GET /api/work-order-configurations/by-code?code=DEFAULT_WO
     */
    @GetMapping("/by-code")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<WorkOrderConfiguration> getByCode(@RequestParam("code") String code) {
        return workOrderConfigurationService.findByConfigCode(code)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new CustomException(
                        "WorkOrderConfiguration not found",
                        HttpStatus.NOT_FOUND
                ));
    }

    /**
     * Tạo mới cấu hình
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkOrderConfiguration> create(
            @Valid @RequestBody WorkOrderConfiguration payload
    ) {
        WorkOrderConfiguration created = workOrderConfigurationService.create(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật cấu hình
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkOrderConfiguration> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody WorkOrderConfiguration payload
    ) {
        return ResponseEntity.ok(workOrderConfigurationService.update(id, payload));
    }

    /**
     * Xóa cấu hình
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable("id") Long id) {
        workOrderConfigurationService.delete(id);
        return ResponseEntity.ok(new SuccessResponse(true, "Deleted successfully"));
    }
}