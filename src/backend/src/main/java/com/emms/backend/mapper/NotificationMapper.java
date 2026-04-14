package com.emms.backend.mapper;
import com.emms.backend.dto.notification.NotificationPatchDTO;
import com.emms.backend.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notification updateNotification(Notification entity, NotificationPatchDTO dto) {
        if (entity == null) {
            return null;
        }
        if (dto == null) {
            return entity;
        }

        if (dto.getRead() != null) {
            entity.setRead(dto.getRead());
        }

        return entity;
    }

    public NotificationPatchDTO toPatchDto(Notification model) {
        if (model == null) {
            return null;
        }

        NotificationPatchDTO dto = new NotificationPatchDTO();
        dto.setRead(model.isRead());
        return dto;
    }
}