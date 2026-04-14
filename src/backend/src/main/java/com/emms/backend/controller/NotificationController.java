package com.emms.backend.controller;

import com.emms.backend.advancedsearch.FilterField;
import com.emms.backend.advancedsearch.SearchCriteria;
import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.notification.NotificationPatchDTO;
import com.emms.backend.entity.Notification;
import com.emms.backend.entity.User;
import com.emms.backend.entity.enums.PermissionEntity;
import com.emms.backend.exception.CustomException;
import com.emms.backend.service.NotificationService;
import com.emms.backend.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "Operations on notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService,
                                  UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<Collection<Notification>> getAll(HttpServletRequest req) {
        User user = requireUser(req);

        if (!hasPermission(user, PermissionEntity.NOTIFICATION_VIEW)) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(notificationService.findByUser(user.getUserId()));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<Page<Notification>> search(
            @Parameter(description = "Notification search criteria")
            @RequestBody SearchCriteria searchCriteria,
            HttpServletRequest req
    ) {
        User user = requireUser(req);

        if (!hasPermission(user, PermissionEntity.NOTIFICATION_VIEW)) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }

        if (searchCriteria == null) {
            throw new CustomException("Search criteria must not be null", HttpStatus.BAD_REQUEST);
        }

        if (searchCriteria.getFilterFields() == null) {
            searchCriteria.setFilterFields(new ArrayList<>());
        }

        FilterField filterField = new FilterField();
        filterField.setField("user.userId");
        filterField.setValue(user.getUserId());
        filterField.setOperation("eq");
        filterField.setValues(new ArrayList<>());

        searchCriteria.getFilterFields().add(filterField);

        return ResponseEntity.ok(notificationService.findBySearchCriteria(searchCriteria));
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<SuccessResponse> readAll(HttpServletRequest req) {
        User user = requireUser(req);

        if (!hasPermission(user, PermissionEntity.NOTIFICATION_VIEW)) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }

        notificationService.readAll(user.getUserId());
        return ResponseEntity.ok(new SuccessResponse(true, "Notifications read"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<Notification> getById(@PathVariable Long id, HttpServletRequest req) {
        User user = requireUser(req);

        if (!hasPermission(user, PermissionEntity.NOTIFICATION_VIEW)) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }

        Notification notification = notificationService.findById(id);
        checkAccessToNotification(notification, user);

        return ResponseEntity.ok(notification);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<Notification> patch(
            @Parameter(description = "Notification fields to update")
            @Valid @RequestBody NotificationPatchDTO notification,
            @PathVariable Long id,
            HttpServletRequest req
    ) {
        User user = requireUser(req);

        if (!hasPermission(user, PermissionEntity.NOTIFICATION_VIEW)) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }

        Notification existing = notificationService.findById(id);
        checkAccessToNotification(existing, user);

        return ResponseEntity.ok(notificationService.update(id, notification));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id, HttpServletRequest req) {
        User user = requireUser(req);

        if (!hasPermission(user, PermissionEntity.NOTIFICATION_VIEW)) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }

        Notification existing = notificationService.findById(id);
        checkAccessToNotification(existing, user);

        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/{id}/unread")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<Notification> markAsUnread(@PathVariable Long id, HttpServletRequest req) {
        User user = requireUser(req);

        if (!hasPermission(user, PermissionEntity.NOTIFICATION_VIEW)) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }

        Notification existing = notificationService.findById(id);
        checkAccessToNotification(existing, user);

        return ResponseEntity.ok(notificationService.markAsUnread(id));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<SuccessResponse> unreadCount(HttpServletRequest req) {
        User user = requireUser(req);

        if (!hasPermission(user, PermissionEntity.NOTIFICATION_VIEW)) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }

        long count = notificationService.countUnreadByUser(user.getUserId());
        return ResponseEntity.ok(new SuccessResponse(true, String.valueOf(count)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id, HttpServletRequest req) {
        User user = requireUser(req);

        if (!hasPermission(user, PermissionEntity.NOTIFICATION_VIEW)) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }

        Notification existing = notificationService.findById(id);
        checkAccessToNotification(existing, user);

        notificationService.delete(id);
        return ResponseEntity.ok(new SuccessResponse(true, "Deleted successfully"));
    }

    private User requireUser(HttpServletRequest req) {
        User user = userService.whoami(req);
        if (user == null || user.getUserId() == null) {
            throw new CustomException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        return user;
    }

    private boolean hasPermission(User user, PermissionEntity permission) {
        return user != null
                && user.getRole() != null
                && user.getRole().getPermissions() != null
                && user.getRole().getPermissions().contains(permission);
    }

    private void checkAccessToNotification(Notification notification, User user) {
        if (notification == null
                || notification.getUser() == null
                || notification.getUser().getUserId() == null
                || user == null
                || user.getUserId() == null
                || !notification.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
    }
}