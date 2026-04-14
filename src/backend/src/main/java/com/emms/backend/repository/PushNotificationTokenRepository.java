package com.emms.backend.repository;

import com.emms.backend.entity.PushNotificationToken;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PushNotificationTokenRepository extends JpaRepository<PushNotificationToken, Long> {
    Optional<PushNotificationToken> findByUser_Id(Long id);

    
} 