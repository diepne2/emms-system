package com.emms.backend.controller;

import com.emms.backend.dto.woMeterTrigger.WorkOrderMeterTriggerDTO;
import com.emms.backend.entity.WorkOrderMeterTrigger;
import com.emms.backend.service.WorkOrderMeterTriggerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/work-order-meter-triggers")
@RequiredArgsConstructor
@Tag(name = "WorkOrder Meter Trigger Controller", description = "APIs quản lý trigger work order theo meter")
public class WorkOrderMeterTriggerController {

    private final WorkOrderMeterTriggerService workOrderMeterTriggerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    @Operation(summary = "Tạo meter trigger")
    public ResponseEntity<WorkOrderMeterTrigger> create(@Valid @RequestBody WorkOrderMeterTriggerDTO dto) {
        WorkOrderMeterTrigger created = workOrderMeterTriggerService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    @Operation(summary = "Cập nhật meter trigger")
    public ResponseEntity<WorkOrderMeterTrigger> update(@PathVariable Long id,
                                                        @Valid @RequestBody WorkOrderMeterTriggerDTO dto) {
        WorkOrderMeterTrigger updated = workOrderMeterTriggerService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy tất cả meter trigger")
    public ResponseEntity<Collection<WorkOrderMeterTrigger>> getAll() {
        return ResponseEntity.ok(workOrderMeterTriggerService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy meter trigger theo id")
    public ResponseEntity<WorkOrderMeterTrigger> getById(@PathVariable Long id) {
        return ResponseEntity.ok(workOrderMeterTriggerService.findEntityById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    @Operation(summary = "Xóa meter trigger")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workOrderMeterTriggerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/meter/{meterId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy meter trigger theo meter id")
    public ResponseEntity<Collection<WorkOrderMeterTrigger>> getByMeter(@PathVariable Long meterId) {
        return ResponseEntity.ok(workOrderMeterTriggerService.getByMeter(meterId));
    }
}