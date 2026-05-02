package com.emms.backend.controller;

import com.emms.backend.dto.asset.AssetDowntimeDTO;
import com.emms.backend.dto.asset.AssetDowntimeShowDTO;
import com.emms.backend.service.AssetDowntimeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asset-downtimes")
public class AssetDowntimeController {

    private final AssetDowntimeService assetDowntimeService;

    public AssetDowntimeController(AssetDowntimeService assetDowntimeService) {
        this.assetDowntimeService = assetDowntimeService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<AssetDowntimeShowDTO> create(@Valid @RequestBody AssetDowntimeDTO dto) {
        AssetDowntimeShowDTO created = assetDowntimeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<AssetDowntimeShowDTO> update(@PathVariable Long id,
                                                       @Valid @RequestBody AssetDowntimeDTO dto) {
        AssetDowntimeShowDTO updated = assetDowntimeService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<List<AssetDowntimeShowDTO>> getAll() {
        return ResponseEntity.ok(assetDowntimeService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<AssetDowntimeShowDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assetDowntimeService.getById(id));
    }

    @GetMapping("/asset/{assetId}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<List<AssetDowntimeShowDTO>> getByAsset(@PathVariable Long assetId) {
        return ResponseEntity.ok(assetDowntimeService.findByAsset(assetId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assetDowntimeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}