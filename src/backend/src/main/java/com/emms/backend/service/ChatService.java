package com.emms.backend.service;

import com.emms.backend.dto.chat.ChatMessageDTO;
import com.emms.backend.dto.chat.SendChatMessageRequestDTO;
import com.emms.backend.dto.user.UserChatDTO;
import com.emms.backend.entity.ChatMessage;
import com.emms.backend.entity.Conversation;
import com.emms.backend.entity.User;
import com.emms.backend.mapper.ChatMessageMapper;
import com.emms.backend.repository.ChatMessageRepository;
import com.emms.backend.repository.ConversationRepository;
import com.emms.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    public List<ChatMessageDTO> getMessages(Long myId, Long userId) {

        validateUserExists(myId);
        validateUserExists(userId);

        chatMessageRepository.markAsRead(userId, myId);

        List<ChatMessage> messages = chatMessageRepository.findConversation(myId, userId);
        List<ChatMessageDTO> result = new ArrayList<>();

        for (ChatMessage m : messages) {
            User sender = userRepository.findById(m.getSenderId()).orElse(null);
            User receiver = userRepository.findById(m.getReceiverId()).orElse(null);
            result.add(ChatMessageMapper.toDTO(m, sender, receiver));
        }

        return result;
    }

    public ChatMessage sendMessage(Long senderId, SendChatMessageRequestDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException("Request không hợp lệ");
        }

        if (dto.getReceiverId() == null) {
            throw new IllegalArgumentException("receiverId không được null");
        }

        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("content không được để trống");
        }

        if (dto.getContent().trim().length() > 5000) {
            throw new IllegalArgumentException("content quá dài");
        }

        if (senderId.equals(dto.getReceiverId())) {
            throw new IllegalArgumentException("Không thể gửi tin nhắn cho chính mình");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại, id = " + senderId));

        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại, id = " + dto.getReceiverId()));

        Conversation conversation = conversationRepository
                .findConversation(senderId, dto.getReceiverId())
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setUser1(sender);
                    c.setUser2(receiver);
                    return conversationRepository.save(c);
                });

        ChatMessage message = ChatMessageMapper.toEntity(
                senderId,
                dto.getReceiverId(),
                dto.getContent().trim()
        );

        message.setConversationId(conversation.getConversationId());
        message.setIsRead(false);

        return chatMessageRepository.save(message);
    }

    public List<UserChatDTO> getUsersForChat(Long myId) {
        validateUserExists(myId);

        List<User> users = userRepository.findAll();
        List<UserChatDTO> result = new ArrayList<>();

        for (User user : users) {
            if (user.getUserId() != null && !user.getUserId().equals(myId)) {

                List<ChatMessage> lastMessages = chatMessageRepository.findLastMessageList(
                        myId,
                        user.getUserId(),
                        PageRequest.of(0, 1)
                );

                ChatMessage lastMessage = lastMessages.isEmpty() ? null : lastMessages.get(0);

                Long unreadCount = chatMessageRepository.countUnread(
                        user.getUserId(),
                        myId
                );

                UserChatDTO dto = new UserChatDTO();
                dto.setUserId(user.getUserId());
                dto.setUsername(user.getUsername());
                dto.setFullName(user.getFullName());
                dto.setAvatar(user.getAvatar());
                dto.setUnreadCount(unreadCount == null ? 0L : unreadCount);

                if (lastMessage != null) {
                    dto.setLastMessage(lastMessage.getContent());
                    dto.setLastMessageAt(lastMessage.getCreatedAt());
                }

                result.add(dto);
            }
        }

        result.sort((a, b) -> {
            if (a.getLastMessageAt() == null && b.getLastMessageAt() == null) return 0;
            if (a.getLastMessageAt() == null) return 1;
            if (b.getLastMessageAt() == null) return -1;
            return b.getLastMessageAt().compareTo(a.getLastMessageAt());
        });

        return result;
    }

    private void validateUserExists(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại, id = " + userId));
    }
}