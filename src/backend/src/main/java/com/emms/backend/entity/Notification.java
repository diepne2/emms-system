package com.emms.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.emms.backend.entity.enums.NotificationType;

@Entity
@Table(name = "notifications")
public class Notification {

    public enum Type {
        PUSH,
        EMAIL,
        DASHBOARD
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    public enum Category {
        ALERT,
        WORK_ORDER,
        MAINTENANCE_PLAN,
        MATERIAL,
        CHAT,
        ANNOUNCEMENT
    }

    public enum Status {
        PENDING,
        DELIVERED,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "recipient_email", length = 255)
    private String recipientEmail;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private Category category = Category.ALERT;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, length = 2000)
    private String message;

    @Column(name = "source_type", length = 100)
    private String sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;
    

    @Column(name = "is_important", nullable = false)
    private boolean isImportant = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private Status status = Status.PENDING;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Notification(String message2, User notifiedUser, NotificationType workOrder, Long workOrderId) {
        //TODO Auto-generated constructor stub
    }

    @PrePersist
    public void prePersist() {

        if (retryCount == null) {
            retryCount = 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (status == null) {
            status = Status.PENDING;
        }
        if (priority == null) {
            priority = Priority.MEDIUM;
        }
        if (category == null) {
            category = Category.ALERT;
        }
        isRead = false;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void markRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void markUnread() {
        this.isRead = false;
        this.readAt = null;
    }

    public void markImportant() {
        this.isImportant = true;
    }

    public void unmarkImportant() {
        this.isImportant = false;
    }

    public void markDelivered() {
        this.status = Status.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = Status.FAILED;
        this.failedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Type getType() {
        return type;
    }

    public Priority getPriority() {
        return priority;
    }

    public Category getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public void setFailedAt(LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getRetryCount() {
        return retryCount;
    }
    
    
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }


    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }

    public Notification() {
    }
}