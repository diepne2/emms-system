package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.request.RequestCreateResponseDTO;
import com.emms.backend.dto.request.RequestDTO;
import com.emms.backend.dto.request.RequestShowDTO;
import com.emms.backend.dto.request.RequestSummaryDTO;
import com.emms.backend.entity.Request;
import com.emms.backend.mapper.RequestMapper;
import com.emms.backend.service.RequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/requests")
@Tag(name = "Requests", description = "Operations on requests")
public class RequestController {

    private final RequestService requestService;
    private final RequestMapper requestMapper;

    public RequestController(RequestService requestService,
                             RequestMapper requestMapper) {
        this.requestService = requestService;
        this.requestMapper = requestMapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<List<RequestSummaryDTO>> getAll() {
        List<RequestSummaryDTO> response = requestService.findAll()
                .stream()
                .map(requestMapper::toSummaryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<RequestShowDTO> getById(@PathVariable Long id) {
        Request request = requestService.findEntityById(id);
        return ResponseEntity.ok(requestMapper.toShowDto(request));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<RequestCreateResponseDTO> create(@Valid @RequestBody RequestDTO dto) {
        RequestCreateResponseDTO response = requestService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<RequestShowDTO> update(@PathVariable Long id,
                                                 @Valid @RequestBody RequestDTO dto) {
        Request updated = requestService.update(id, dto);
        return ResponseEntity.ok(requestMapper.toShowDto(updated));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<RequestShowDTO> approve(@PathVariable Long id) {
        Request approved = requestService.approve(id);
        return ResponseEntity.ok(requestMapper.toShowDto(approved));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<RequestShowDTO> reject(@PathVariable Long id,
                                                 @RequestParam(required = false) String reason) {
        Request rejected = requestService.reject(id, reason);
        return ResponseEntity.ok(requestMapper.toShowDto(rejected));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<RequestShowDTO> cancel(@PathVariable Long id,
                                                 @RequestParam(required = false) String reason) {
        Request cancelled = requestService.cancel(id, reason);
        return ResponseEntity.ok(requestMapper.toShowDto(cancelled));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        requestService.delete(id);
        return ResponseEntity.ok(new SuccessResponse(true, "Xóa request thành công"));
    }

    @DeleteMapping("/{id}/force")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<SuccessResponse> forceDelete(@PathVariable Long id) {
        requestService.forceDelete(id);
        return ResponseEntity.ok(new SuccessResponse(true, "Xóa vĩnh viễn request thành công"));
    }
}