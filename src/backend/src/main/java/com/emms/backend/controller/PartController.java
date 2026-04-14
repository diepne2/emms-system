package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.part.PartPatchDTO;
import com.emms.backend.entity.Part;
import com.emms.backend.entity.User;
import com.emms.backend.entity.enums.PermissionEntity;
import com.emms.backend.exception.CustomException;
import com.emms.backend.service.PartService;
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
@RequestMapping("/parts")
@Tag(name = "Parts", description = "Operations on parts")
public class PartController {

    private final PartService partService;
    private final UserService userService;

    public PartController(PartService partService,
                          UserService userService) {
        this.partService = partService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<Collection<Part>> getAll(HttpServletRequest req) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.PART_VIEW);

        return ResponseEntity.ok(partService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<Part> getById(
            @Parameter(description = "Part ID") @PathVariable Long id,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.PART_VIEW);

        return ResponseEntity.ok(partService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<Part> create(
            @Parameter(description = "Part data to create")
            @Valid @RequestBody Part partReq,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.PART_CREATE);

        Part saved = partService.create(partReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<Part> patch(
            @Parameter(description = "Part fields to update")
            @Valid @RequestBody PartPatchDTO part,
            @Parameter(description = "Part ID") @PathVariable Long id,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.PART_UPDATE);

        Part updated = partService.update(id, part);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> delete(
            @Parameter(description = "Part ID") @PathVariable Long id,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.PART_DELETE);

        partService.delete(id);
        return ResponseEntity.ok(new SuccessResponse(true, "Deleted successfully"));
    }

    @PutMapping("/{id}/increase-stock")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<Part> increaseStock(
            @PathVariable Long id,
            @RequestParam Integer amount,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.PART_STOCK_ADJUST);

        return ResponseEntity.ok(partService.increaseStock(id, amount));
    }

    @PutMapping("/{id}/decrease-stock")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<Part> decreaseStock(
            @PathVariable Long id,
            @RequestParam Integer amount,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.PART_STOCK_ADJUST);

        return ResponseEntity.ok(partService.decreaseStock(id, amount));
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