package com.emms.backend.controller;

import com.emms.backend.dto.location.LocationDTO;
import com.emms.backend.dto.location.LocationShowDTO;
import com.emms.backend.dto.location.LocationSummaryDTO;
import com.emms.backend.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public ResponseEntity<LocationShowDTO> create(@Valid @RequestBody LocationDTO dto) {
        LocationShowDTO response = locationService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public ResponseEntity<LocationShowDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody LocationDTO dto
    ) {
        LocationShowDTO response = locationService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LocationShowDTO> getById(@PathVariable Long id) {
        LocationShowDTO response = locationService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LocationShowDTO>> getAll() {
        List<LocationShowDTO> response = locationService.getAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LocationSummaryDTO>> getAllSummary() {
        List<LocationSummaryDTO> response = locationService.getAllSummary();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}