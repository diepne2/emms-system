package com.emms.backend.dto.chat;

public class SendChatMessageRequestDTO {

    private Long receiverId;
    private String content;

    public SendChatMessageRequestDTO() {
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}