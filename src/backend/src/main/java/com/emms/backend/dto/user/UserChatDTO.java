package com.emms.backend.dto.user;

import java.time.LocalDateTime;

public class UserChatDTO {

    private Long userId;
    private String username;
    private String fullName;
    private String avatar;

    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;

    public UserChatDTO() {
    }

    public UserChatDTO(Long userId, String username, String fullName, String avatar) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.avatar = avatar;
        this.lastMessage = null;
        this.lastMessageAt = null;
        this.unreadCount = 0L;
    }

    public UserChatDTO(
            Long userId,
            String username,
            String fullName,
            String avatar,
            String lastMessage,
            LocalDateTime lastMessageAt,
            Long unreadCount
    ) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.avatar = avatar;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.unreadCount = unreadCount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }
}