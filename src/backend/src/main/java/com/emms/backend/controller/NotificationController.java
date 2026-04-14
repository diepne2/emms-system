package com.emms.backend.controller;

import com.emms.backend.dto.notification.NotificationShowDTO;
import com.emms.backend.entity.Notification;
import com.emms.backend.entity.User;
import com.emms.backend.mapper.NotificationMapper;
import com.emms.backend.service.NotificationService;
import com.emms.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<NotificationShowDTO>> getMyNotifications(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<Notification> notifications = notificationService.getNotificationsByUserId(currentUser.getId());
        return ResponseEntity.ok(notificationMapper.toShowDtoList(notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> countUnread(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        long unreadCount = notificationService.countUnreadNotifications(currentUser.getId());
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationShowDTO> markAsRead(@PathVariable Long id) {
        Notification notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notificationMapper.toShowDto(notification));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser(Authentication authentication) {
        String usernameOrEmail = authentication.getName();
        return userService.getByUsernameOrEmail(usernameOrEmail);
    }
}