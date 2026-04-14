package com.emms.backend.mapper;

import com.emms.backend.dto.apiKey.ApiKeyCreateRequest;
import com.emms.backend.dto.apiKey.ApiKeyResponse;
import com.emms.backend.dto.apiKey.ApiKeyUpdateRequest;
import com.emms.backend.entity.ApiKey;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyMapper {

    public ApiKey fromCreateRequest(ApiKeyCreateRequest request) {
        if (request == null) {
            return null;
        }

        ApiKey apiKey = new ApiKey();
        apiKey.setLabel(request.getLabel());
        apiKey.setActive(true);
        return apiKey;
    }

    public ApiKey updateEntity(ApiKey entity, ApiKeyUpdateRequest request) {
        if (entity == null || request == null) {
            return entity;
        }

        if (request.getLabel() != null) {
            entity.setLabel(request.getLabel());
        }

        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }

        return entity;
    }

    public ApiKeyResponse toResponse(ApiKey entity) {
        if (entity == null) {
            return null;
        }

        ApiKeyResponse response = new ApiKeyResponse();
        response.setApiKeyId(entity.getApiKeyId());
        response.setLabel(entity.getLabel());
        response.setActive(entity.isActive());
        response.setCreatedAt(entity.getCreatedAt());
        response.setLastUsed(entity.getLastUsed());
        response.setExpiredAt(entity.getExpiredAt());

        if (entity.getUser() != null) {
            response.setUserId(entity.getUser().getUserId());
            response.setUsername(entity.getUser().getUsername());
        }

        return response;
    }
}