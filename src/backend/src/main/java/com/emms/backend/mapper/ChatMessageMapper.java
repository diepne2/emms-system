package com.emms.backend.mapper;

import com.emms.backend.dto.chat.ChatMessageDTO;
import com.emms.backend.entity.ChatMessage;
import com.emms.backend.entity.User;
import com.emms.backend.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper {

    private final UserRepository userRepository;

    public ChatMessageMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ChatMessageDTO toDTO(ChatMessage entity) {
        if (entity == null) {
            return null;
        }

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(entity.getId());
        dto.setConversationId(entity.getConversationId());
        dto.setSenderId(entity.getSenderId());
        dto.setContent(entity.getContent());
        dto.setReplyToMessageId(entity.getReplyToMessageId());
        dto.setCreatedAt(entity.getCreatedAt());

        userRepository.findById(entity.getSenderId())
                .map(User::getUsername)
                .ifPresent(dto::setSenderUsername);

        return dto;
    }
}