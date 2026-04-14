package com.emms.backend.controller;

import com.emms.backend.dto.request.RequestDTO;
import com.emms.backend.entity.Request;
import com.emms.backend.entity.User;
import com.emms.backend.entity.enums.PermissionEntity;
import com.emms.backend.exception.CustomException;
import com.emms.backend.service.RequestService;
import com.emms.backend.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/requests")
@Tag(name = "Requests", description = "Operations on requests")
public class RequestController {

    private final RequestService requestService;
    private final UserService userService;

    public RequestController(RequestService requestService,
                             UserService userService) {
        this.requestService = requestService;
        this.userService = userService;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<Request> update(
            @Parameter(description = "Request fields to update")
            @Valid @RequestBody RequestDTO requestDTO,
            @PathVariable Long id,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.WORK_ORDER_UPDATE);

        Request updated = requestService.update(id, requestDTO);
        return ResponseEntity.ok(updated);
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