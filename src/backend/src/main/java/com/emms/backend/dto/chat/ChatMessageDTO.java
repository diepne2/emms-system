package com.emms.backend.dto.chat;

import java.time.LocalDateTime;

public class ChatMessageDTO {

    private Long id;

    private Long senderId;
    private String senderUsername;
    private String senderAvatar;  

    private Long receiverId;
    private String receiverAvatar;

    private String content;
    private LocalDateTime createdAt;

    public ChatMessageDTO() {}

    public ChatMessageDTO(Long id,
                          Long senderId,
                          String senderUsername,
                          String senderAvatar,
                          Long receiverId,
                          String receiverAvatar,
                          String content,
                          LocalDateTime createdAt) {
        this.id = id;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.senderAvatar = senderAvatar;
        this.receiverId = receiverId;
        this.receiverAvatar = receiverAvatar;
        this.content = content;
        this.createdAt = createdAt;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverAvatar() {
        return receiverAvatar;
    }

    public void setReceiverAvatar(String receiverAvatar) {
        this.receiverAvatar = receiverAvatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}