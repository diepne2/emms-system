package com.emms.backend.service;

import com.emms.backend.advancedsearch.SearchCriteria;
import com.emms.backend.advancedsearch.SpecificationBuilder;
import com.emms.backend.dto.notification.NotificationPatchDTO;
import com.emms.backend.entity.Notification;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.NotificationMapper;
import com.emms.backend.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationMapper notificationMapper,
                               SimpMessageSendingOperations messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.messagingTemplate = messagingTemplate;
    }

    public Notification create(Notification notification) {
        validateNotification(notification);
        normalizeNotification(notification);

        Notification savedNotification = notificationRepository.save(notification);

        pushToWeb(savedNotification);
        pushUnreadCount(extractUserId(savedNotification));

        return savedNotification;
    }

    public List<Notification> createMultiple(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return List.of();
        }

        for (Notification notification : notifications) {
            validateNotification(notification);
            normalizeNotification(notification);
        }

        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);

        for (Notification notification : savedNotifications) {
            pushToWeb(notification);
            pushUnreadCount(extractUserId(notification));
        }

        return savedNotifications;
    }

    public Notification update(Long id, NotificationPatchDTO notificationPatchDTO) {
        if (notificationPatchDTO == null) {
            throw new CustomException("Notification patch must not be null", HttpStatus.BAD_REQUEST);
        }

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Notification not found", HttpStatus.NOT_FOUND));

        notificationMapper.updateNotification(notification, notificationPatchDTO);
        normalizeNotification(notification);

        Notification savedNotification = notificationRepository.save(notification);

        pushToWeb(savedNotification);
        pushUnreadCount(extractUserId(savedNotification));

        return savedNotification;
    }

    @Transactional(readOnly = true)
    public Collection<Notification> getAll() {
        return notificationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Notification findById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Notification not found", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Collection<Notification> findByUser(Long userId) {
        validateUserId(userId);
        return notificationRepository.findByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public Page<Notification> findBySearchCriteria(SearchCriteria searchCriteria) {
        if (searchCriteria == null) {
            throw new CustomException("Search criteria must not be null", HttpStatus.BAD_REQUEST);
        }

        SpecificationBuilder<Notification> builder = new SpecificationBuilder<>();
        if (searchCriteria.getFilterFields() != null) {
            searchCriteria.getFilterFields().forEach(builder::with);
        }

        Pageable pageable = PageRequest.of(
                searchCriteria.getPageNum(),
                searchCriteria.getPageSize(),
                searchCriteria.getDirection(),
                searchCriteria.getSortField()
        );

        return notificationRepository.findAll(builder.build(), pageable);
    }

    public void delete(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Notification not found", HttpStatus.NOT_FOUND));

        Long userId = extractUserId(notification);

        notificationRepository.delete(notification);

        pushUnreadCount(userId);
    }

    public void readAll(Long userId) {
        validateUserId(userId);
        notificationRepository.readAll(userId);
        pushUnreadCount(userId);
    }

    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Notification not found", HttpStatus.NOT_FOUND));

        if (!notification.isRead()) {
            notification.markRead(); // fix: set readAt luôn
            notification = notificationRepository.save(notification);
        }

        pushToWeb(notification);
        pushUnreadCount(extractUserId(notification));

        return notification;
    }

    public Notification markAsUnread(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new CustomException("Notification not found", HttpStatus.NOT_FOUND));

        if (notification.isRead()) {
            notification.markUnread();
            notification = notificationRepository.save(notification);
        }

        pushToWeb(notification);
        pushUnreadCount(extractUserId(notification));

        return notification;
    }

    @Transactional(readOnly = true)
    public long countUnreadByUser(Long userId) {
        validateUserId(userId);
        return notificationRepository.countByUser_IdAndReadFalse(userId);
    }

    private void pushToWeb(Notification notification) {
        Long userId = extractUserId(notification);
        if (userId == null) {
            return;
        }

        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }

    private void pushUnreadCount(Long userId) {
        if (userId == null) {
            return;
        }

        long unreadCount = notificationRepository.countByUser_IdAndReadFalse(userId);
        messagingTemplate.convertAndSend("/topic/notifications/" + userId + "/unread-count", unreadCount);
    }

    private Long extractUserId(Notification notification) {
        if (notification == null || notification.getUser() == null) {
            return null;
        }
        return notification.getUser().getUserId();
    }

    private void validateNotification(Notification notification) {
        if (notification == null) {
            throw new CustomException("Notification must not be null", HttpStatus.BAD_REQUEST);
        }

        boolean hasUser = notification.getUser() != null && notification.getUser().getUserId() != null;
        boolean hasUsername = notification.getUsername() != null && !notification.getUsername().trim().isBlank();
        boolean hasRecipientEmail = notification.getRecipientEmail() != null
                && !notification.getRecipientEmail().trim().isBlank();

        if (!hasUser && !hasUsername && !hasRecipientEmail) {
            throw new CustomException(
                    "Notification must have at least one receiver: user, username or recipientEmail",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (notification.getTitle() == null || notification.getTitle().trim().isBlank()) {
            throw new CustomException("Notification title must not be blank", HttpStatus.BAD_REQUEST);
        }

        if (notification.getMessage() == null || notification.getMessage().trim().isBlank()) {
            throw new CustomException("Notification message must not be blank", HttpStatus.BAD_REQUEST);
        }

        if (notification.getType() == null) {
            throw new CustomException("Notification type must not be null", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new CustomException("User id must not be null", HttpStatus.BAD_REQUEST);
        }
    }

    private void normalizeNotification(Notification notification) {
        if (notification.getUsername() != null) {
            notification.setUsername(notification.getUsername().trim());
        }

        if (notification.getRecipientEmail() != null) {
            notification.setRecipientEmail(notification.getRecipientEmail().trim());
        }

        if (notification.getCreatedBy() != null) {
            notification.setCreatedBy(notification.getCreatedBy().trim());
        }

        if (notification.getTitle() != null) {
            notification.setTitle(notification.getTitle().trim());
        }

        if (notification.getMessage() != null) {
            notification.setMessage(notification.getMessage().trim());
        }

        if (notification.getSourceType() != null) {
            notification.setSourceType(notification.getSourceType().trim());
        }

        if (notification.getActionUrl() != null) {
            notification.setActionUrl(notification.getActionUrl().trim());
        }

        if (notification.getPriority() == null) {
            notification.setPriority(Notification.Priority.MEDIUM);
        }

        if (notification.getCategory() == null) {
            notification.setCategory(Notification.Category.ALERT);
        }

        if (notification.getStatus() == null) {
            notification.setStatus(Notification.Status.PENDING);
        }

        if (notification.getRetryCount() == null) {
            notification.setRetryCount(0);
        }
    }

    public void createMultiple(List<Notification> notifications, boolean b, String message) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createMultiple'");
    }
}