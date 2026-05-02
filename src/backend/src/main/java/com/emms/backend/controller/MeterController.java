package com.emms.backend.controller;

import com.emms.backend.dto.meter.MeterDTO;
import com.emms.backend.dto.meter.MeterShowDTO;
import com.emms.backend.dto.meter.MeterSummaryDTO;
import com.emms.backend.entity.Meter;
import com.emms.backend.service.MeterService;
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
public class MeterController {

    private final MeterService meterService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<MeterShowDTO> create(@Valid @RequestBody MeterDTO dto) {
        Meter meter = meterService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(meterService.getShowDtoById(meter.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<MeterShowDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody MeterDTO dto
    ) {
        Meter meter = meterService.update(id, dto);
        return ResponseEntity.ok(meterService.getShowDtoById(meter.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeterShowDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(meterService.getShowDtoById(id));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MeterShowDTO>> getAll() {
        return ResponseEntity.ok(meterService.getAllShowDtos());
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MeterSummaryDTO>> getAllSummary() {
        return ResponseEntity.ok(meterService.getAllSummaryDtos());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        meterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}