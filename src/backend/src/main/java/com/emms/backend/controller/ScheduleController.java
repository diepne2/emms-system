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


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<Schedule> create(
            @RequestParam("preventiveMaintenanceId") Long preventiveMaintenanceId,
            @Valid @RequestBody ScheduleDTO dto
    ) {
        Schedule created = scheduleService.create(preventiveMaintenanceId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<Collection<Schedule>> getAll() {
        return ResponseEntity.ok(scheduleService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<Schedule> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(scheduleService.getById(id));
    }

    @GetMapping("/by-preventive-maintenance/{preventiveMaintenanceId}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<Schedule> getByPreventiveMaintenanceId(
            @PathVariable Long preventiveMaintenanceId
    ) {
        return ResponseEntity.ok(scheduleService.getByPreventiveMaintenanceId(preventiveMaintenanceId));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<Schedule> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody ScheduleDTO dto
    ) {
        return ResponseEntity.ok(scheduleService.update(id, dto));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable("id") Long id) {
        scheduleService.delete(id);
        return ResponseEntity.ok(new SuccessResponse(true, "Deleted successfully"));
    }

    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<Schedule> enable(@PathVariable("id") Long id) {
        return ResponseEntity.ok(scheduleService.enable(id));
    }

    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<Schedule> disable(@PathVariable("id") Long id) {
        return ResponseEntity.ok(scheduleService.disable(id));
    }


    @GetMapping("/{id}/active-on")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<Boolean> isActiveOnDate(
            @PathVariable("id") Long id,
            @RequestParam("date") LocalDate date
    ) {
        return ResponseEntity.ok(scheduleService.isActiveOnDate(id, date));
    }
}