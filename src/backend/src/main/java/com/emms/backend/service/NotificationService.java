package com.emms.backend.service;

import com.emms.backend.entity.Notification;
import com.emms.backend.entity.User;
import com.emms.backend.exception.CustomException;
import com.emms.backend.repository.NotificationRepository;
import com.emms.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            @Nullable SimpMessageSendingOperations messagingTemplate
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public Notification createNotification(Long userId, String title, String message) {
        if (userId == null) {
            throw new CustomException("User id không được null", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy người dùng với id: " + userId,
                        HttpStatus.NOT_FOUND
                ));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(trim(title));
        notification.setMessage(trim(message));
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);

        pushToUser(user, saved);

        return saved;
    }

    public void createNotificationIfUserExists(Long userId, String title, String message) {
        if (userId == null) return;
        if (!userRepository.existsById(userId)) return;

        createNotification(userId, title, message);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUserId(Long userId) {
        validateUserExists(userId);
        return notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long userId) {
        validateUserExists(userId);
        return notificationRepository.countByUser_UserIdAndIsReadFalse(userId);
    }

    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy thông báo với id: " + notificationId,
                        HttpStatus.NOT_FOUND
                ));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification = notificationRepository.save(notification);
        }

        return notification;
    }

    public void markAllAsRead(Long userId) {
        validateUserExists(userId);

        List<Notification> unreadNotifications =
                notificationRepository.findByUser_UserIdAndIsReadFalse(userId);

        if (unreadNotifications.isEmpty()) return;

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(unreadNotifications);
    }

    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy thông báo với id: " + notificationId,
                        HttpStatus.NOT_FOUND
                ));

        notificationRepository.delete(notification);
    }

    private void validateUserExists(Long userId) {
        if (userId == null || !userRepository.existsById(userId)) {
            throw new CustomException(
                    "Không tìm thấy người dùng với id: " + userId,
                    HttpStatus.NOT_FOUND
            );
        }
    }

    private void pushToUser(User user, Notification notification) {
        if (user == null || notification == null) return;

        if (messagingTemplate == null) {
            log.debug("Không có WebSocket bean, bỏ qua realtime notification cho userId={}", user.getUserId());
            return;
        }

        try {
            messagingTemplate.convertAndSendToUser(
                    user.getUsername(),
                    "/queue/notifications",
                    notification
            );
        } catch (Exception ex) {
            log.warn("Gửi realtime notification thất bại cho userId={}: {}", user.getUserId(), ex.getMessage());
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}