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
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<LocationShowDTO> create(@Valid @RequestBody LocationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<LocationShowDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody LocationDTO dto
    ) {
        return ResponseEntity.ok(locationService.update(id, dto));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LocationShowDTO>> getAll() {
        return ResponseEntity.ok(locationService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LocationShowDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getById(id));
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LocationSummaryDTO>> getAllSummary() {
        return ResponseEntity.ok(locationService.getAllSummary());
    }

    @GetMapping("/dropdown")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LocationSummaryDTO>> getDropdown() {
        return ResponseEntity.ok(locationService.getAllSummary());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}