package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.chat.ChatMessageDTO;
import com.emms.backend.dto.chat.ChatMessageRequest;
import com.emms.backend.entity.User;
import com.emms.backend.security.CurrentUser;
import com.emms.backend.service.ChatService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/conversations/private")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<SuccessResponse> createPrivateConversation(
            @RequestParam Long targetUserId,
            @Parameter(hidden = true) @CurrentUser User currentUser
    ) {
        Long conversationId = chatService.createPrivateConversation(currentUser.getUserId(), targetUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse(true, String.valueOf(conversationId)));
    }

    @PostMapping("/messages")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @Valid @RequestBody ChatMessageRequest request,
            @Parameter(hidden = true) @CurrentUser User currentUser
    ) {
        ChatMessageDTO result = chatService.sendMessage(currentUser.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/conversations/{conversationId}/read")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<SuccessResponse> markConversationAsRead(
            @PathVariable Long conversationId,
            @Parameter(hidden = true) @CurrentUser User currentUser
    ) {
        chatService.markConversationAsRead(conversationId, currentUser.getUserId());
        return ResponseEntity.ok(new SuccessResponse(true, "Marked as read"));
    }

    @GetMapping("/conversations/{conversationId}/unread-count")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<SuccessResponse> getUnreadChatCount(
            @PathVariable Long conversationId,
            @Parameter(hidden = true) @CurrentUser User currentUser
    ) {
        long unreadCount = chatService.getUnreadChatCount(conversationId, currentUser.getUserId());
        return ResponseEntity.ok(new SuccessResponse(true, String.valueOf(unreadCount)));
    }
}