package com.emms.backend.controller;

import com.emms.backend.dto.chat.ChatMessageDTO;
import com.emms.backend.dto.chat.SendChatMessageRequestDTO;
import com.emms.backend.dto.user.UserChatDTO;
import com.emms.backend.entity.ChatMessage;
import com.emms.backend.security.CustomUserPrincipal;
import com.emms.backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@PreAuthorize("isAuthenticated()")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/users")
    public List<UserChatDTO> getUsers(Authentication authentication) {
        Long myId = getCurrentUserId(authentication);
        return chatService.getUsersForChat(myId);
    }


    @GetMapping("/messages")
    public List<ChatMessageDTO> getMessages(
            Authentication authentication,
            @RequestParam Long userId
    ) {
        Long myId = getCurrentUserId(authentication);
        return chatService.getMessages(myId, userId);
    }


    @PostMapping("/send")
    public ChatMessage sendMessage(
            Authentication authentication,
            @RequestBody SendChatMessageRequestDTO dto
    ) {
        Long senderId = getCurrentUserId(authentication);
        return chatService.sendMessage(senderId, dto);
    }


    private Long getCurrentUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserPrincipal customUser) {
            return customUser.getUserId();
        }

        throw new RuntimeException("Invalid authenticated principal");
    }
}