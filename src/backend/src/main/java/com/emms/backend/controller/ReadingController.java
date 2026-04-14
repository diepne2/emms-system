package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.reading.ReadingDTO;
import com.emms.backend.entity.Reading;
import com.emms.backend.entity.User;
import com.emms.backend.entity.enums.PermissionEntity;
import com.emms.backend.exception.CustomException;
import com.emms.backend.service.ReadingService;
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
@RequestMapping("/readings")
@Tag(name = "Readings", description = "Operations on meter readings")
public class ReadingController {

    private final ReadingService readingService;
    private final UserService userService;

    public ReadingController(ReadingService readingService,
                             UserService userService) {
        this.readingService = readingService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<Collection<Reading>> getAll(HttpServletRequest req) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.ASSET_VIEW);

        return ResponseEntity.ok(readingService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<Reading> getById(
            @Parameter(description = "Reading ID")
            @PathVariable Long id,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.ASSET_VIEW);

        return ResponseEntity.ok(readingService.getById(id));
    }

    @GetMapping("/meter/{meterId}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<Collection<Reading>> getByMeter(
            @Parameter(description = "Meter ID")
            @PathVariable Long meterId,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.ASSET_VIEW);

        return ResponseEntity.ok(readingService.findByMeter(meterId));
    }

    @GetMapping("/meter/{meterId}/latest")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<Reading> getLatestByMeter(
            @Parameter(description = "Meter ID")
            @PathVariable Long meterId,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.ASSET_VIEW);

        Reading reading = readingService.findLatestByMeter(meterId)
                .orElseThrow(() -> new CustomException("Không tìm thấy reading mới nhất", HttpStatus.NOT_FOUND));

        return ResponseEntity.ok(reading);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT')")
    public ResponseEntity<Reading> create(
            @Parameter(description = "Reading data to create")
            @Valid @RequestBody ReadingDTO readingDTO,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.ASSET_UPDATE);

        Reading created = readingService.create(readingDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT')")
    public ResponseEntity<Reading> update(
            @Parameter(description = "Reading data to update")
            @Valid @RequestBody ReadingDTO readingDTO,
            @Parameter(description = "Reading ID")
            @PathVariable Long id,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.ASSET_UPDATE);

        Reading updated = readingService.update(id, readingDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> delete(
            @Parameter(description = "Reading ID")
            @PathVariable Long id,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.ASSET_DELETE);

        readingService.delete(id);
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