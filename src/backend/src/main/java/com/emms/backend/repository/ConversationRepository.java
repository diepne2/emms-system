package com.emms.backend.repository;

import com.emms.backend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
        SELECT c FROM Conversation c
        WHERE (c.user1.userId = :userA AND c.user2.userId = :userB)
           OR (c.user1.userId = :userB AND c.user2.userId = :userA)
    """)
    Optional<Conversation> findConversation(
            @Param("userA") Long userA,
            @Param("userB") Long userB
    );
}