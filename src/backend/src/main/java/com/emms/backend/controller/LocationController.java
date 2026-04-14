package com.emms.backend.controller;

import com.emms.backend.dto.location.LocationDTO;
import com.emms.backend.dto.location.LocationShowDTO;
import com.emms.backend.dto.location.LocationSummaryDTO;

import com.emms.backend.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public LocationShowDTO create(@Valid @RequestBody LocationDTO dto) {
        return locationService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public LocationShowDTO update(@PathVariable Long id,
                                  @Valid @RequestBody LocationDTO dto) {
        return locationService.update(id, dto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public LocationShowDTO getById(@PathVariable Long id) {
        return locationService.getById(id);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<LocationShowDTO> getAll() {
        return locationService.getAll();
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public List<LocationSummaryDTO> getAllSummary() {
        return locationService.getAllSummary();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public void delete(@PathVariable Long id) {
        locationService.delete(id);
    }
}