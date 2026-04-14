package com.emms.backend.controller;

import com.emms.backend.dto.meter.MeterDTO;
import com.emms.backend.dto.meter.MeterShowDTO;
import com.emms.backend.dto.meter.MeterSummaryDTO;
import com.emms.backend.service.MeterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meters")
@RequiredArgsConstructor
@Tag(name = "Meter", description = "Meter APIs")
public class MeterController {

    private final MeterService meterService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    @Operation(summary = "Create meter")
    public ResponseEntity<MeterShowDTO> create(@Valid @RequestBody MeterDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(meterService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    @Operation(summary = "Update meter")
    public ResponseEntity<MeterShowDTO> update(@PathVariable Long id,
                                               @Valid @RequestBody MeterDTO dto) {
        return ResponseEntity.ok(meterService.update(id, dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get meter by id")
    public ResponseEntity<MeterShowDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(meterService.getById(id));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all meters")
    public ResponseEntity<List<MeterShowDTO>> getAll() {
        return ResponseEntity.ok(meterService.getAll());
    }

    @GetMapping("/summaries")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all meter summaries")
    public ResponseEntity<List<MeterSummaryDTO>> getAllSummary() {
        return ResponseEntity.ok(meterService.getAllSummary());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    @Operation(summary = "Delete meter")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        meterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}