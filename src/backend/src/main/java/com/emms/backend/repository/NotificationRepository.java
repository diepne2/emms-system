package com.emms.backend.repository;

import com.emms.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUser_UserIdAndIsReadFalse(Long userId);

    long countByUser_UserIdAndIsReadFalse(Long userId);
}