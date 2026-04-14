package com.emms.backend.controller;

import com.emms.backend.dto.asset.AssetDowntimeDTO;
import com.emms.backend.entity.AssetDowntime;
import com.emms.backend.service.AssetDowntimeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/asset-downtimes")
public class AssetDowntimeController {

    private final AssetDowntimeService assetDowntimeService;

    public AssetDowntimeController(AssetDowntimeService assetDowntimeService) {
        this.assetDowntimeService = assetDowntimeService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public ResponseEntity<AssetDowntime> create(@Valid @RequestBody AssetDowntime assetDowntime) {
        AssetDowntime created = assetDowntimeService.create(assetDowntime);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public ResponseEntity<AssetDowntime> update(@PathVariable Long id,
                                                @Valid @RequestBody AssetDowntimeDTO dto) {
        AssetDowntime updated = assetDowntimeService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT','ROLE_KYTHUATVIEN','ROLE_OPERATOR','ROLE_REQUESTER','ROLE_VIEWER')")
    public ResponseEntity<Collection<AssetDowntime>> getAll() {
        return ResponseEntity.ok(assetDowntimeService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT','ROLE_KYTHUATVIEN','ROLE_OPERATOR','ROLE_REQUESTER','ROLE_VIEWER')")
    public ResponseEntity<AssetDowntime> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assetDowntimeService.getById(id));
    }

    @GetMapping("/asset/{assetId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT','ROLE_KYTHUATVIEN','ROLE_OPERATOR','ROLE_REQUESTER','ROLE_VIEWER')")
    public ResponseEntity<List<AssetDowntime>> getByAsset(@PathVariable Long assetId) {
        return ResponseEntity.ok(assetDowntimeService.findByAsset(assetId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assetDowntimeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}