package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.requestPortal.RequestPortalPatchDTO;
import com.emms.backend.dto.requestPortal.RequestPortalPostDTO;
import com.emms.backend.entity.RequestPortal;
import com.emms.backend.entity.User;
import com.emms.backend.entity.enums.PermissionEntity;
import com.emms.backend.exception.CustomException;
import com.emms.backend.service.RequestPortalService;
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
@RequestMapping("/request-portals")
@Tag(name = "Request Portals", description = "Operations on request portals")
public class RequestPortalController {

    private final RequestPortalService requestPortalService;
    private final UserService userService;

    public RequestPortalController(RequestPortalService requestPortalService,
                                   UserService userService) {
        this.requestPortalService = requestPortalService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<List<RequestPortal>> getAll(HttpServletRequest req) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.SETTINGS);

        return ResponseEntity.ok(requestPortalService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<RequestPortal> getById(
            @Parameter(description = "Request portal ID")
            @PathVariable Long id,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.SETTINGS);

        return ResponseEntity.ok(requestPortalService.getById(id));
    }

    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<RequestPortal> getByUuid(
            @Parameter(description = "Request portal UUID")
            @PathVariable String uuid
    ) {
        return ResponseEntity.ok(requestPortalService.getByUuid(uuid));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<RequestPortal> create(
            @Parameter(description = "Request portal data to create")
            @Valid @RequestBody RequestPortalPostDTO requestPortalPostDTO,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.SETTINGS);

        RequestPortal created = requestPortalService.create(requestPortalPostDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<RequestPortal> update(
            @Parameter(description = "Request portal fields to update")
            @Valid @RequestBody RequestPortalPatchDTO requestPortalPatchDTO,
            @PathVariable Long id,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.SETTINGS);

        RequestPortal updated = requestPortalService.update(id, requestPortalPatchDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> delete(
            @Parameter(description = "Request portal ID")
            @PathVariable Long id,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.SETTINGS);

        requestPortalService.delete(id);
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