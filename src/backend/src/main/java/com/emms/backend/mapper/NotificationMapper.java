package com.emms.backend.mapper;

import com.emms.backend.dto.notification.NotificationShowDTO;
import com.emms.backend.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationMapper {

    public NotificationShowDTO toShowDto(Notification entity) {
        if (entity == null) {
            return null;
        }

        NotificationShowDTO dto = new NotificationShowDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setMessage(entity.getMessage());
        dto.setRead(entity.isRead());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public List<NotificationShowDTO> toShowDtoList(List<Notification> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toShowDto)
                .collect(Collectors.toList());
    }
}