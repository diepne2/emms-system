package com.emms.backend.controller;

import com.emms.backend.dto.asset.AssetPUTDTO;
import com.emms.backend.dto.asset.AssetShowDTO;
import com.emms.backend.dto.asset.AssetSummaryDTO;
import com.emms.backend.entity.Asset;
import com.emms.backend.entity.User;
import com.emms.backend.service.AssetService;
import com.emms.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@Tag(name = "Asset Controller", description = "APIs quản lý tài sản")
public class AssetController {

    private final AssetService assetService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    @Operation(summary = "Tạo asset mới")
    public ResponseEntity<AssetShowDTO> create(@Valid @RequestBody AssetPUTDTO dto,
                                               HttpServletRequest request) {
        User currentUser = userService.whoami(request);
        Asset created = assetService.createFromDto(dto, currentUser);
        AssetShowDTO response = assetService.getShowDtoById(created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    @Operation(summary = "Cập nhật asset")
    public ResponseEntity<AssetShowDTO> update(@PathVariable Long id,
                                               @Valid @RequestBody AssetPUTDTO dto,
                                               HttpServletRequest request) {
        User currentUser = userService.whoami(request);
        Asset updated = assetService.update(id, dto, currentUser);
        AssetShowDTO response = assetService.getShowDtoById(updated.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy chi tiết asset theo id")
    public ResponseEntity<AssetShowDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.getShowDtoById(id));
    }

    @GetMapping("/{id}/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy summary asset theo id")
    public ResponseEntity<AssetSummaryDTO> getSummaryById(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.getSummaryDtoById(id));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy danh sách asset detail")
    public ResponseEntity<List<AssetShowDTO>> getAll() {
        return ResponseEntity.ok(assetService.getAllShowDtos());
    }

    @GetMapping("/summaries")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy danh sách asset summary")
    public ResponseEntity<List<AssetSummaryDTO>> getAllSummaries() {
        return ResponseEntity.ok(assetService.getAllSummaryDtos());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    @Operation(summary = "Xóa asset")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}