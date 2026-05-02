package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.category.CategoryPatchDTO;
import com.emms.backend.entity.MeterCategory;
import com.emms.backend.entity.User;
import com.emms.backend.entity.enums.PermissionEntity;
import com.emms.backend.exception.CustomException;
import com.emms.backend.service.MeterCategoryService;
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
@RequestMapping("/meter-categories")
@Tag(name = "Meter Categories", description = "Operations on meter categories")
public class MeterCategoryController {

    private final MeterCategoryService meterCategoryService;
    private final UserService userService;

    public MeterCategoryController(MeterCategoryService meterCategoryService,
                                   UserService userService) {
        this.meterCategoryService = meterCategoryService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<Collection<MeterCategory>> getAll(HttpServletRequest req) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.SETTINGS);
        return ResponseEntity.ok(meterCategoryService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<MeterCategory> getById(@PathVariable Long id, HttpServletRequest req) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.SETTINGS);

        MeterCategory meterCategory = meterCategoryService.findById(id)
                .orElseThrow(() -> new CustomException("Meter category not found", HttpStatus.NOT_FOUND));

        return ResponseEntity.ok(meterCategory);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<MeterCategory> create(
            @Parameter(description = "Meter category to create")
            @Valid @RequestBody MeterCategory meterCategoryReq,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.SETTINGS);

        MeterCategory saved = meterCategoryService.create(meterCategoryReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<MeterCategory> patch(
            @Parameter(description = "Meter category fields to update")
            @Valid @RequestBody CategoryPatchDTO meterCategory,
            @PathVariable Long id,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.SETTINGS);

        MeterCategory updated = meterCategoryService.update(id, meterCategory);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id, HttpServletRequest req) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.SETTINGS);

        meterCategoryService.delete(id);
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
            throw new CustomException("Access Denied", HttpStatus.FORBIDDEN);
        }
    }
}