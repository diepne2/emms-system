package com.emms.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "push_notification_tokens", indexes = {
        @Index(name = "idx_push_token_user", columnList = "user_id"),
        @Index(name = "idx_push_token_token", columnList = "token"),
        @Index(name = "idx_push_token_active", columnList = "active")
})
public class PushNotificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "push_notification_token_id")
    private Long pushNotificationTokenId;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PushNotificationToken() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.lastUsedAt == null) {
            this.lastUsedAt = now;
        }
        normalize();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        normalize();
    }

    private void normalize() {
        token = trim(token);
        deviceType = trim(deviceType);
        deviceName = trim(deviceName);

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token không được để trống");
        }
        if (user == null) {
            throw new IllegalArgumentException("user không được null");
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    public void markUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public Long getPushNotificationTokenId() {
        return pushNotificationTokenId;
    }

    public void setPushNotificationTokenId(Long pushNotificationTokenId) {
        this.pushNotificationTokenId = pushNotificationTokenId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = trim(token);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = trim(deviceType);
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = trim(deviceName);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}