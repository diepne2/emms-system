package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.apiKey.ApiKeyCreateRequest;
import com.emms.backend.dto.apiKey.ApiKeyCreateResponse;
import com.emms.backend.dto.apiKey.ApiKeyCriteria;
import com.emms.backend.dto.apiKey.ApiKeyResponse;
import com.emms.backend.dto.apiKey.ApiKeyUpdateRequest;
import com.emms.backend.entity.User;
import com.emms.backend.exception.CustomException;
import com.emms.backend.security.CurrentUser;
import com.emms.backend.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api-keys")
@Hidden
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<Page<ApiKeyResponse>> search(
            @Parameter(description = "API key search criteria")
            @RequestBody(required = false) ApiKeyCriteria criteria,
            @Parameter(hidden = true) @CurrentUser User user,
            Pageable pageable
    ) {
        Page<ApiKeyResponse> result = apiKeyService.findByCriteria(criteria, pageable, user);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<List<ApiKeyResponse>> getAll(
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        return ResponseEntity.ok(apiKeyService.getAll(user));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<ApiKeyCreateResponse> create(
            @Parameter(description = "API key to create")
            @Valid @RequestBody ApiKeyCreateRequest request,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        ApiKeyCreateResponse result = apiKeyService.create(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<ApiKeyResponse> getById(
            @PathVariable Long id,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        ApiKeyResponse result = apiKeyService.findById(id, user)
                .orElseThrow(() -> new CustomException("Api key not found", HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<ApiKeyResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ApiKeyUpdateRequest request,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        ApiKeyResponse result = apiKeyService.update(id, request, user);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<SuccessResponse> delete(
            @PathVariable Long id,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        apiKeyService.delete(id, user);
        return ResponseEntity.ok(new SuccessResponse(true, "Deleted successfully"));
    }
}