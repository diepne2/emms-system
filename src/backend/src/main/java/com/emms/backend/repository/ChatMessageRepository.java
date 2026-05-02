package com.emms.backend.repository;

import com.emms.backend.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
        SELECT m FROM ChatMessage m
        WHERE (m.senderId = :user1 AND m.receiverId = :user2)
           OR (m.senderId = :user2 AND m.receiverId = :user1)
        ORDER BY m.createdAt ASC
    """)
    List<ChatMessage> findConversation(
            @Param("user1") Long user1,
            @Param("user2") Long user2
    );

    @Query("""
        SELECT m FROM ChatMessage m
        WHERE (m.senderId = :user1 AND m.receiverId = :user2)
           OR (m.senderId = :user2 AND m.receiverId = :user1)
        ORDER BY m.createdAt DESC
    """)
    List<ChatMessage> findLastMessageList(
            @Param("user1") Long user1,
            @Param("user2") Long user2,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(m) FROM ChatMessage m
        WHERE m.senderId = :senderId
          AND m.receiverId = :receiverId
          AND m.isRead = false
    """)
    Long countUnread(
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE ChatMessage m
        SET m.isRead = true
        WHERE m.senderId = :senderId
          AND m.receiverId = :receiverId
          AND m.isRead = false
    """)
    void markAsRead(
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId
    );
}