package com.emms.backend.service;

import com.emms.backend.dto.apiKey.ApiKeyCreateRequest;
import com.emms.backend.dto.apiKey.ApiKeyCreateResponse;
import com.emms.backend.dto.apiKey.ApiKeyCriteria;
import com.emms.backend.dto.apiKey.ApiKeyResponse;
import com.emms.backend.dto.apiKey.ApiKeyUpdateRequest;
import com.emms.backend.entity.ApiKey;
import com.emms.backend.entity.User;
import com.emms.backend.entity.enums.PermissionEntity;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.ApiKeyMapper;
import com.emms.backend.repository.ApiKeyRepository;
import com.emms.backend.utils.Helper;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyMapper apiKeyMapper;

    public ApiKeyService(ApiKeyRepository apiKeyRepository,
                         ApiKeyMapper apiKeyMapper) {
        this.apiKeyRepository = apiKeyRepository;
        this.apiKeyMapper = apiKeyMapper;
    }

    public ApiKeyCreateResponse create(@Valid ApiKeyCreateRequest request, User user) {
        validateApiAccess(user);

        ApiKey apiKey = apiKeyMapper.fromCreateRequest(request);
        apiKey.setUser(user);

        String plainKey = generatePlainApiKey();
        apiKey.setCode(hashKey(plainKey));

        ApiKey saved = apiKeyRepository.save(apiKey);

        ApiKeyCreateResponse response = new ApiKeyCreateResponse();
        response.setApiKey(apiKeyMapper.toResponse(saved));
        response.setPlainKey(plainKey);
        return response;
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> getAll(User user) {
        validateApiAccess(user);

        return apiKeyRepository.findAll(findUserSpecification(user))
                .stream()
                .map(apiKeyMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ApiKeyResponse> findById(Long id, User user) {
        validateApiAccess(user);

        Optional<ApiKey> optional = apiKeyRepository.findById(id);
        optional.ifPresent(apiKey -> ensureOwner(apiKey, user));

        return optional.map(apiKeyMapper::toResponse);
    }

    public ApiKeyResponse update(Long id, @Valid ApiKeyUpdateRequest request, User user) {
        validateApiAccess(user);

        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new CustomException("Api key not found", HttpStatus.NOT_FOUND));

        ensureOwner(apiKey, user);

        ApiKey updated = apiKeyMapper.updateEntity(apiKey, request);
        ApiKey saved = apiKeyRepository.save(updated);
        return apiKeyMapper.toResponse(saved);
    }

    public void delete(Long id, User user) {
        validateApiAccess(user);

        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new CustomException("Api key not found", HttpStatus.NOT_FOUND));

        ensureOwner(apiKey, user);
        apiKeyRepository.delete(apiKey);
    }

    @Transactional(readOnly = true)
    public Page<ApiKeyResponse> findByCriteria(ApiKeyCriteria criteria, Pageable pageable, User user) {
        validateApiAccess(user);

        Specification<ApiKey> specification = buildSpecification(criteria, user);
        return apiKeyRepository.findAll(specification, pageable)
                .map(apiKeyMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<ApiKey> findActiveByPlainKey(String plainKey) {
        if (plainKey == null || plainKey.isBlank()) {
            return Optional.empty();
        }

        String hashedKey = hashKey(plainKey);
        Optional<ApiKey> optional = apiKeyRepository.findByCode(hashedKey);

        if (optional.isEmpty()) {
            return Optional.empty();
        }

        ApiKey apiKey = optional.get();

        if (!apiKey.isActive()) {
            return Optional.empty();
        }

        if (apiKey.getExpiredAt() != null && apiKey.getExpiredAt().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }

        apiKeyRepository.updateLastUsed(apiKey.getApiKeyId(), LocalDateTime.now());
        apiKey.setLastUsed(LocalDateTime.now());

        return Optional.of(apiKey);
    }

    private Specification<ApiKey> buildSpecification(ApiKeyCriteria criteria, User user) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), user.getUserId()));

            if (criteria != null) {
                if (criteria.getQuery() != null && !criteria.getQuery().isBlank()) {
                    String keyword = "%" + criteria.getQuery().trim().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get("label")), keyword));
                }

                if (criteria.getActive() != null) {
                    predicates.add(cb.equal(root.get("active"), criteria.getActive()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<ApiKey> findUserSpecification(User user) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("id"), user.getUserId());
    }

    private void validateApiAccess(User user) {
        if (user == null
                || user.getUserId() == null
                || user.getRole() == null
                || !user.getRole().isActive()
                || !user.getRole().hasPermission(PermissionEntity.SETTINGS)) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
    }

    private void ensureOwner(ApiKey apiKey, User user) {
        if (apiKey == null
                || apiKey.getUser() == null
                || apiKey.getUser().getUserId() == null
                || user == null
                || user.getUserId() == null
                || !apiKey.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
    }

    private String generatePlainApiKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[32];
        secureRandom.nextBytes(key);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(key);
    }

    private String hashKey(String plainKey) {
        return Helper.hashKey(plainKey);
    }

}