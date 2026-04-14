package com.emms.backend.mapper;

import com.emms.backend.dto.requestPortal.RequestPortalPatchDTO;
import com.emms.backend.dto.requestPortal.RequestPortalPostDTO;
import com.emms.backend.dto.requestPortal.RequestPortalPublicDTO;
import com.emms.backend.dto.requestPortal.RequestPortalShowDTO;
import com.emms.backend.dto.requestPortal.RequestPortalSummaryDTO;
import com.emms.backend.entity.RequestPortal;
import org.springframework.stereotype.Component;

@Component
public class RequestPortalMapper {

    public RequestPortal updateRequestPortal(RequestPortal entity, RequestPortalPatchDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }

        if (dto.getWelcomeMessage() != null) {
            entity.setWelcomeMessage(dto.getWelcomeMessage());
        }

        return entity;
    }

    public RequestPortal fromPostDto(RequestPortalPostDTO dto) {
        if (dto == null) {
            return null;
        }

        RequestPortal entity = new RequestPortal();
        entity.setTitle(dto.getTitle());
        entity.setWelcomeMessage(dto.getWelcomeMessage());
        return entity;
    }

    public RequestPortalShowDTO toShowDto(RequestPortal model) {
        if (model == null) {
            return null;
        }

        RequestPortalShowDTO dto = new RequestPortalShowDTO();
        dto.setTitle(model.getTitle());
        dto.setWelcomeMessage(model.getWelcomeMessage());
        dto.setUuid(model.getUuid());

        if (model.getRequestPortalId() != null) {
            dto.setId(model.getRequestPortalId());
        }
        if (model.getCreatedAt() != null) {
            dto.setCreatedAt(model.getCreatedAt());
        }
        if (model.getUpdatedAt() != null) {
            dto.setUpdatedAt(model.getUpdatedAt());
        }

        return dto;
    }

    public RequestPortalPublicDTO toPublicDto(RequestPortal model) {
        if (model == null) {
            return null;
        }

        RequestPortalPublicDTO dto = new RequestPortalPublicDTO();
        dto.setTitle(model.getTitle());
        dto.setWelcomeMessage(model.getWelcomeMessage());
        dto.setUuid(model.getUuid());

        if (model.getRequestPortalId() != null) {
            dto.setId(model.getRequestPortalId());
        }
        if (model.getCreatedAt() != null) {
            dto.setCreatedAt(model.getCreatedAt());
        }
        if (model.getUpdatedAt() != null) {
            dto.setUpdatedAt(model.getUpdatedAt());
        }

        return dto;
    }

    public RequestPortalSummaryDTO toSummaryDto(RequestPortal model) {
        if (model == null) {
            return null;
        }

        RequestPortalSummaryDTO dto = new RequestPortalSummaryDTO();
        dto.setId(model.getRequestPortalId());
        dto.setTitle(model.getTitle());
        dto.setUuid(model.getUuid());
        return dto;
    }
}