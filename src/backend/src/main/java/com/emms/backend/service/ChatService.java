package com.emms.backend.service;

import com.emms.backend.dto.chat.ChatMessageDTO;
import com.emms.backend.dto.chat.ChatMessageRequest;
import com.emms.backend.entity.ChatConversation;
import com.emms.backend.entity.ChatMessage;
import com.emms.backend.entity.ChatParticipant;
import com.emms.backend.entity.User;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.ChatMessageMapper;
import com.emms.backend.repository.ChatConversationRepository;
import com.emms.backend.repository.ChatMessageRepository;
import com.emms.backend.repository.ChatParticipantRepository;
import com.emms.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatParticipantRepository participantRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final NotificationService notificationService;
    private final PushNotificationTokenService pushNotificationService;

    public ChatService(ChatConversationRepository conversationRepository,
                       ChatParticipantRepository participantRepository,
                       ChatMessageRepository messageRepository,
                       UserRepository userRepository,
                       ChatMessageMapper chatMessageMapper,
                       NotificationService notificationService,
                       PushNotificationTokenService pushNotificationService) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatMessageMapper = chatMessageMapper;
        this.notificationService = notificationService;
        this.pushNotificationService = pushNotificationService;
    }

    public Long createPrivateConversation(Long user1Id, Long user2Id) {
        if (user1Id == null || user2Id == null) {
            throw new CustomException("user1Id và user2Id không được null", HttpStatus.BAD_REQUEST);
        }

        if (user1Id.equals(user2Id)) {
            throw new CustomException("Không thể tạo chat với chính mình", HttpStatus.BAD_REQUEST);
        }

        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new CustomException("Không tìm thấy user1: " + user1Id, HttpStatus.NOT_FOUND));

        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new CustomException("Không tìm thấy user2: " + user2Id, HttpStatus.NOT_FOUND));

        Long existingConversationId = findExistingPrivateConversation(user1Id, user2Id);
        if (existingConversationId != null) {
            return existingConversationId;
        }

        ChatConversation conversation = new ChatConversation();
        conversation = conversationRepository.save(conversation);

        ChatParticipant p1 = new ChatParticipant();
        p1.setConversationId(conversation.getId());
        p1.setUserId(user1.getUserId());

        ChatParticipant p2 = new ChatParticipant();
        p2.setConversationId(conversation.getId());
        p2.setUserId(user2.getUserId());

        participantRepository.save(p1);
        participantRepository.save(p2);

        return conversation.getId();
    }

    public ChatMessageDTO sendMessage(Long senderId, ChatMessageRequest request) {
        if (senderId == null) {
            throw new CustomException("senderId không được null", HttpStatus.BAD_REQUEST);
        }
        if (request == null) {
            throw new CustomException("request không được null", HttpStatus.BAD_REQUEST);
        }
        if (request.getConversationId() == null) {
            throw new CustomException("conversationId không được null", HttpStatus.BAD_REQUEST);
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new CustomException("Nội dung tin nhắn không được để trống", HttpStatus.BAD_REQUEST);
        }

        ChatConversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new CustomException("Không tìm thấy conversation", HttpStatus.NOT_FOUND));

        ChatParticipant senderParticipant = participantRepository
                .findByConversationIdAndUserId(conversation.getId(), senderId)
                .orElseThrow(() -> new CustomException("Bạn không thuộc cuộc trò chuyện này", HttpStatus.FORBIDDEN));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new CustomException("Không tìm thấy sender", HttpStatus.NOT_FOUND));

        String content = request.getContent().trim();

        if (request.getReplyToMessageId() != null) {
            ChatMessage replyTo = messageRepository.findById(request.getReplyToMessageId())
                    .orElseThrow(() -> new CustomException("Không tìm thấy tin nhắn reply", HttpStatus.NOT_FOUND));

            if (!conversation.getId().equals(replyTo.getConversationId())) {
                throw new CustomException("Tin nhắn reply không thuộc conversation này", HttpStatus.BAD_REQUEST);
            }
        }

        ChatMessage message = new ChatMessage();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setContent(content);
        message.setReplyToMessageId(request.getReplyToMessageId());

        ChatMessage saved = messageRepository.save(message);

        senderParticipant.setLastReadMessageId(saved.getId());
        participantRepository.save(senderParticipant);

        ChatMessageDTO dto = chatMessageMapper.toDTO(saved);

        List<ChatParticipant> participants = participantRepository.findByConversationId(conversation.getId());

        for (ChatParticipant participant : participants) {
            User targetUser = userRepository.findById(participant.getUserId())
                    .orElseThrow(() -> new CustomException("Không tìm thấy user tham gia chat", HttpStatus.NOT_FOUND));

            // Realtime update cho UI chat
            pushNotificationService.pushChatToUser(
                    targetUser.getUsername(),
                    conversation.getId(),
                    dto
            );

            // Chỉ tạo notification DB cho người nhận
            if (!participant.getUserId().equals(senderId)) {
                createChatNotification(targetUser, sender, conversation.getId(), content);
            }
        }

        return dto;
    }

    public void markConversationAsRead(Long conversationId, Long currentUserId) {
        if (conversationId == null) {
            throw new CustomException("conversationId không được null", HttpStatus.BAD_REQUEST);
        }
        if (currentUserId == null) {
            throw new CustomException("currentUserId không được null", HttpStatus.BAD_REQUEST);
        }

        ChatParticipant participant = participantRepository
                .findByConversationIdAndUserId(conversationId, currentUserId)
                .orElseThrow(() -> new CustomException("Bạn không thuộc conversation này", HttpStatus.FORBIDDEN));

        List<ChatMessage> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        if (!messages.isEmpty()) {
            ChatMessage lastMessage = messages.get(messages.size() - 1);
            participant.setLastReadMessageId(lastMessage.getId());
            participantRepository.save(participant);
        }
    }

    @Transactional(readOnly = true)
    public long getUnreadChatCount(Long conversationId, Long currentUserId) {
        if (conversationId == null) {
            throw new CustomException("conversationId không được null", HttpStatus.BAD_REQUEST);
        }
        if (currentUserId == null) {
            throw new CustomException("currentUserId không được null", HttpStatus.BAD_REQUEST);
        }

        ChatParticipant participant = participantRepository
                .findByConversationIdAndUserId(conversationId, currentUserId)
                .orElseThrow(() -> new CustomException("Bạn không thuộc conversation này", HttpStatus.FORBIDDEN));

        Long lastReadMessageId = participant.getLastReadMessageId();
        if (lastReadMessageId == null) {
            return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).size();
        }

        return messageRepository.countByConversationIdAndIdGreaterThan(conversationId, lastReadMessageId);
    }

    private void createChatNotification(User targetUser, User sender, Long conversationId, String content) {
        if (targetUser == null || sender == null || conversationId == null) {
            return;
        }

        String title = "Tin nhắn mới từ " + safeUsername(sender);
        String message = truncate(safeUsername(sender) + ": " + content, 255);

        notificationService.createNotification(targetUser.getUserId(), title, message);
    }

    @Transactional(readOnly = true)
    protected Long findExistingPrivateConversation(Long user1Id, Long user2Id) {
        List<ChatParticipant> user1Participations = participantRepository.findByUserId(user1Id);

        for (ChatParticipant p1 : user1Participations) {
            List<ChatParticipant> participants = participantRepository.findByConversationId(p1.getConversationId());

            if (participants.size() == 2) {
                boolean hasUser1 = participants.stream().anyMatch(p -> user1Id.equals(p.getUserId()));
                boolean hasUser2 = participants.stream().anyMatch(p -> user2Id.equals(p.getUserId()));

                if (hasUser1 && hasUser2) {
                    return p1.getConversationId();
                }
            }
        }

        return null;
    }

    private String safeUsername(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
            return "Unknown";
        }
        return user.getUsername().trim();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        if (maxLength <= 3) {
            return text.substring(0, Math.min(text.length(), maxLength));
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}