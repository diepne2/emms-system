package com.emms.backend.controller;

import com.emms.backend.advancedsearch.SearchCriteria;
import com.emms.backend.dto.asset.AssetPUTDTO;
import com.emms.backend.dto.asset.AssetShowDTO;
import com.emms.backend.entity.Asset;
import com.emms.backend.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    private static final String ASSET_WRITE_AUTH =
            "hasAnyAuthority('ROLE_ADMIN','ADMIN','ROLE_TECHNICAL_MANAGER','TECHNICAL_MANAGER')";

    @PostMapping
    @PreAuthorize(ASSET_WRITE_AUTH)
    public ResponseEntity<AssetShowDTO> create(@RequestBody AssetPUTDTO dto) {
        Asset asset = assetService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assetService.getShowDtoById(asset.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize(ASSET_WRITE_AUTH)
    public ResponseEntity<AssetShowDTO> update(
            @PathVariable Long id,
            @RequestBody AssetPUTDTO dto
    ) {
        Asset updated = assetService.update(id, dto);
        return ResponseEntity.ok(assetService.getShowDtoById(updated.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AssetShowDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.getShowDtoById(id));
    }

    @GetMapping("/{id}/children")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AssetShowDTO>> getChildren(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.getChildren(id));
    }

    @PostMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AssetShowDTO>> search(@RequestBody SearchCriteria criteria) {
        return ResponseEntity.ok(assetService.search(criteria));
    }

    @PutMapping("/{id}/decommission")
    @PreAuthorize(ASSET_WRITE_AUTH)
    public ResponseEntity<AssetShowDTO> decommission(@PathVariable Long id) {
        Asset asset = assetService.decommission(id);
        return ResponseEntity.ok(assetService.getShowDtoById(asset.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ASSET_WRITE_AUTH)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assetService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AssetShowDTO>> getAll() {
        return ResponseEntity.ok(assetService.getAll());
    }
}