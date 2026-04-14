package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.schedule.ScheduleDTO;
import com.emms.backend.entity.Schedule;
import com.emms.backend.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * Tạo schedule cho preventive maintenance
     * POST /api/schedules?preventiveMaintenanceId=1
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<Schedule> create(
            @RequestParam("preventiveMaintenanceId") Long preventiveMaintenanceId,
            @Valid @RequestBody ScheduleDTO dto
    ) {
        Schedule created = scheduleService.create(preventiveMaintenanceId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Lấy tất cả schedule
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT', 'NHANVIENKYTHUAT', 'NHANVIENVANHANH')")
    public ResponseEntity<Collection<Schedule>> getAll() {
        return ResponseEntity.ok(scheduleService.getAll());
    }

    /**
     * Lấy schedule theo id
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT', 'NHANVIENKYTHUAT', 'NHANVIENVANHANH')")
    public ResponseEntity<Schedule> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(scheduleService.getById(id));
    }

    /**
     * Lấy schedule theo preventiveMaintenanceId
     * GET /api/schedules/by-preventive-maintenance/1
     */
    @GetMapping("/by-preventive-maintenance/{preventiveMaintenanceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT', 'NHANVIENKYTHUAT', 'NHANVIENVANHANH')")
    public ResponseEntity<Schedule> getByPreventiveMaintenanceId(
            @PathVariable Long preventiveMaintenanceId
    ) {
        return ResponseEntity.ok(scheduleService.getByPreventiveMaintenanceId(preventiveMaintenanceId));
    }

    /**
     * Cập nhật schedule
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<Schedule> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody ScheduleDTO dto
    ) {
        return ResponseEntity.ok(scheduleService.update(id, dto));
    }

    /**
     * Xóa schedule
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable("id") Long id) {
        scheduleService.delete(id);
        return ResponseEntity.ok(new SuccessResponse(true, "Deleted successfully"));
    }

    /**
     * Enable schedule
     */
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<Schedule> enable(@PathVariable("id") Long id) {
        return ResponseEntity.ok(scheduleService.enable(id));
    }

    /**
     * Disable schedule
     */
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public ResponseEntity<Schedule> disable(@PathVariable("id") Long id) {
        return ResponseEntity.ok(scheduleService.disable(id));
    }

    /**
     * Kiểm tra schedule active ở 1 ngày cụ thể hay không
     * Example: GET /api/schedules/1/active-on?date=2026-04-12
     */
    @GetMapping("/{id}/active-on")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT', 'NHANVIENKYTHUAT', 'NHANVIENVANHANH')")
    public ResponseEntity<Boolean> isActiveOnDate(
            @PathVariable("id") Long id,
            @RequestParam("date") LocalDate date
    ) {
        return ResponseEntity.ok(scheduleService.isActiveOnDate(id, date));
    }
}