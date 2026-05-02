package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenanceDTO;
import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenancePostDTO;
import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenanceSummaryDTO;
import com.emms.backend.exception.CustomException;
import com.emms.backend.security.CustomUserPrincipal;
import com.emms.backend.service.PreventiveMaintenanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/preventive-maintenances")
@Tag(name = "Preventive Maintenance")
public class PreventiveMaintenanceController {

    private final PreventiveMaintenanceService service;

    public PreventiveMaintenanceController(PreventiveMaintenanceService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER','ROLE_TECHNICIAN','ROLE_OPERATOR')")
    public ResponseEntity<List<PreventiveMaintenanceSummaryDTO>> getAll() {
        requireUser();
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER','ROLE_TECHNICIAN','ROLE_OPERATOR')")
    public ResponseEntity<PreventiveMaintenanceSummaryDTO> getById(@PathVariable Long id) {
        requireUser();
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<PreventiveMaintenanceSummaryDTO> create(
            @Valid @RequestBody PreventiveMaintenancePostDTO dto
    ) {
        requireUser();

        PreventiveMaintenanceSummaryDTO created = service.create(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<PreventiveMaintenanceSummaryDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PreventiveMaintenanceDTO dto
    ) {
        requireUser();

        PreventiveMaintenanceSummaryDTO updated = service.update(id, dto);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        requireUser();
        service.delete(id);

        return ResponseEntity.ok(
                new SuccessResponse(true, "Deleted successfully")
        );
    }

    private CustomUserPrincipal requireUser() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null
                || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof CustomUserPrincipal principal)) {
            throw new CustomException(
                    "User not authenticated",
                    HttpStatus.UNAUTHORIZED
            );
        }

        return principal;
    }
}