package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenanceDTO;
import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenancePostDTO;
import com.emms.backend.entity.PreventiveMaintenance;
import com.emms.backend.entity.User;
import com.emms.backend.entity.enums.PermissionEntity;
import com.emms.backend.exception.CustomException;
import com.emms.backend.service.PreventiveMaintenanceService;
import com.emms.backend.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/preventive-maintenances")
@Tag(name = "Preventive Maintenances", description = "Operations on preventive maintenances")
public class PreventiveMaintenanceController {

    private final PreventiveMaintenanceService preventiveMaintenanceService;
    private final UserService userService;

    public PreventiveMaintenanceController(PreventiveMaintenanceService preventiveMaintenanceService,
                                           UserService userService) {
        this.preventiveMaintenanceService = preventiveMaintenanceService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<Collection<PreventiveMaintenance>> getAll(HttpServletRequest req) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.MAINTENANCE_PLAN_VIEW);

        return ResponseEntity.ok(preventiveMaintenanceService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<PreventiveMaintenance> getById(@PathVariable Long id,
                                                         HttpServletRequest req) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.MAINTENANCE_PLAN_VIEW);

        return ResponseEntity.ok(preventiveMaintenanceService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<PreventiveMaintenance> create(
            @Parameter(description = "Preventive maintenance data to create")
            @Valid @RequestBody PreventiveMaintenancePostDTO preventiveMaintenancePost,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.MAINTENANCE_PLAN_CREATE);

        PreventiveMaintenance created = preventiveMaintenanceService.create(preventiveMaintenancePost);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<PreventiveMaintenance> update(
            @Parameter(description = "Preventive maintenance fields to update")
            @Valid @RequestBody PreventiveMaintenanceDTO preventiveMaintenance,
            @PathVariable Long id,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.MAINTENANCE_PLAN_UPDATE);

        PreventiveMaintenance updated = preventiveMaintenanceService.update(id, preventiveMaintenance);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id,
                                                  HttpServletRequest req) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.MAINTENANCE_PLAN_DELETE);

        preventiveMaintenanceService.delete(id);
        return ResponseEntity.ok(new SuccessResponse(true, "Deleted successfully"));
    }

    private User requireUser(HttpServletRequest req) {
        User user = userService.whoami(req);
        if (user == null || user.getUserId() == null) {
            throw new CustomException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        return user;
    }

    private void requirePermission(User user, PermissionEntity permission) {
        if (user == null
                || user.getRole() == null
                || user.getRole().getPermissions() == null
                || !user.getRole().getPermissions().contains(permission)) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
    }
}