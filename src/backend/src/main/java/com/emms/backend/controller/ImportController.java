package com.emms.backend.controller;

import com.emms.backend.dto.importData.AssetImportDTO;
import com.emms.backend.dto.importData.ImportResponse;
import com.emms.backend.dto.importData.LocationImportDTO;
import com.emms.backend.dto.importData.MeterImportDTO;
import com.emms.backend.dto.importData.PartImportDTO;
import com.emms.backend.dto.importData.PreventiveMaintenanceImportDTO;
import com.emms.backend.dto.importData.WorkOrderImportDTO;
import com.emms.backend.entity.User;
import com.emms.backend.entity.enums.PermissionEntity;
import com.emms.backend.exception.CustomException;
import com.emms.backend.service.ImportService;
import com.emms.backend.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/import")
@Tag(name = "Import", description = "Operations for importing data")
public class ImportController {

    private final UserService userService;
    private final ImportService importService;

    public ImportController(UserService userService,
                            ImportService importService) {
        this.userService = userService;
        this.importService = importService;
    }

    @PostMapping("/work-orders")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<ImportResponse> importWorkOrders(
            @Parameter(description = "List of work orders to import")
            @Valid @RequestBody List<WorkOrderImportDTO> toImport,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.WORK_ORDER_CREATE);

        return ResponseEntity.ok(importService.importWorkOrders(toImport));
    }

    @PostMapping("/assets")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<ImportResponse> importAssets(
            @Parameter(description = "List of assets to import")
            @Valid @RequestBody List<AssetImportDTO> toImport,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.ASSET_CREATE);

        return ResponseEntity.ok(importService.importAssets(toImport));
    }

    @PostMapping("/locations")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<ImportResponse> importLocations(
            @Parameter(description = "List of locations to import")
            @Valid @RequestBody List<LocationImportDTO> toImport,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.ASSET_CREATE);
        return ResponseEntity.ok(importService.importLocations(toImport));
    }

    @PostMapping("/meters")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<ImportResponse> importMeters(
            @Parameter(description = "List of meters to import")
            @Valid @RequestBody List<MeterImportDTO> toImport,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.ASSET_CREATE);
        return ResponseEntity.ok(importService.importMeters(toImport));
    }

    @PostMapping("/parts")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<ImportResponse> importParts(
            @Parameter(description = "List of parts to import")
            @Valid @RequestBody List<PartImportDTO> toImport,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.PART_CREATE);

        return ResponseEntity.ok(importService.importParts(toImport));
    }

    @PostMapping("/preventive-maintenances")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<ImportResponse> importPreventiveMaintenances(
            @Parameter(description = "List of preventive maintenances to import")
            @Valid @RequestBody List<PreventiveMaintenanceImportDTO> toImport,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.MAINTENANCE_PLAN_CREATE);

        return ResponseEntity.ok(importService.importPreventiveMaintenances(toImport));
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
            throw new CustomException("Access Denied", HttpStatus.FORBIDDEN);
        }
    }
}