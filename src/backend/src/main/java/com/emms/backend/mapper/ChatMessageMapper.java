package com.emms.backend.mapper;

import com.emms.backend.dto.chat.ChatMessageDTO;
import com.emms.backend.entity.ChatMessage;
import com.emms.backend.entity.User;

public class ChatMessageMapper {

    public static ChatMessageDTO toDTO(ChatMessage m, User sender, User receiver) {

        String senderUsername = sender != null ? sender.getUsername() : "Unknown";

        ChatMessageDTO dto = new ChatMessageDTO();

        dto.setId(m.getId());
        dto.setSenderId(m.getSenderId());
        dto.setSenderUsername(senderUsername);

        dto.setSenderAvatar(sender != null ? sender.getAvatar() : null);

        dto.setReceiverId(m.getReceiverId());
        dto.setReceiverAvatar(receiver != null ? receiver.getAvatar() : null);

        dto.setContent(m.getContent());
        dto.setCreatedAt(m.getCreatedAt());

        return dto;
    }

    public static ChatMessage toEntity(Long senderId, Long receiverId, String content) {

        ChatMessage m = new ChatMessage();

        m.setSenderId(senderId);
        m.setReceiverId(receiverId);
        m.setContent(content);

        return m;
    }
}