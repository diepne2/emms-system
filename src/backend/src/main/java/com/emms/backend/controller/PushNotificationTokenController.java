package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.entity.PushNotificationToken;
import com.emms.backend.entity.User;
import com.emms.backend.entity.enums.PermissionEntity;
import com.emms.backend.exception.CustomException;
import com.emms.backend.service.PushNotificationTokenService;
import com.emms.backend.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/push-tokens")
@Tag(name = "Push Notification Tokens", description = "Operations on push notification tokens")
public class PushNotificationTokenController {

    private final PushNotificationTokenService pushNotificationTokenService;
    private final UserService userService;

    public PushNotificationTokenController(PushNotificationTokenService pushNotificationTokenService,
                                           UserService userService) {
        this.pushNotificationTokenService = pushNotificationTokenService;
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<PushNotificationToken> getMyToken(HttpServletRequest req) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.NOTIFICATION_VIEW);

        return ResponseEntity.ok(
                pushNotificationTokenService.findRequiredByUser(user.getUserId())
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<PushNotificationToken> createOrUpsert(
            @Valid @RequestBody PushNotificationToken token,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.NOTIFICATION_VIEW);

        if (token == null) {
            throw new CustomException("Push notification token data must not be null", HttpStatus.BAD_REQUEST);
        }

        token.setUser(user);
        PushNotificationToken saved = pushNotificationTokenService.create(token);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<PushNotificationToken> updateMyToken(
            @Valid @RequestBody PushNotificationToken token,
            HttpServletRequest req
    ) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.NOTIFICATION_VIEW);

        if (token == null) {
            throw new CustomException("Push notification token data must not be null", HttpStatus.BAD_REQUEST);
        }

        PushNotificationToken existing = pushNotificationTokenService.findRequiredByUser(user.getUserId());

        token.setPushNotificationTokenId(existing.getPushNotificationTokenId());
        token.setUser(user);

        return ResponseEntity.ok(pushNotificationTokenService.update(token));
    }

    @PutMapping("/me/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<SuccessResponse> deactivateMyToken(HttpServletRequest req) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.NOTIFICATION_VIEW);

        pushNotificationTokenService.deactivateByUser(user.getUserId());
        return ResponseEntity.ok(new SuccessResponse(true, "Push token deactivated"));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<SuccessResponse> deleteMyToken(HttpServletRequest req) {
        User user = requireUser(req);
        requirePermission(user, PermissionEntity.NOTIFICATION_VIEW);

        PushNotificationToken existing = pushNotificationTokenService.findRequiredByUser(user.getUserId());
        pushNotificationTokenService.delete(existing.getPushNotificationTokenId());

        return ResponseEntity.ok(new SuccessResponse(true, "Push token deleted"));
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