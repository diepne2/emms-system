package com.emms.backend.controller;

import com.emms.backend.entity.AssetCategory;
import com.emms.backend.service.AssetCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/asset-categories")
public class AssetCategoryController {

    private final AssetCategoryService assetCategoryService;

    public AssetCategoryController(AssetCategoryService assetCategoryService) {
        this.assetCategoryService = assetCategoryService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public ResponseEntity<AssetCategory> create(@Valid @RequestBody AssetCategory assetCategory) {
        AssetCategory created = assetCategoryService.create(assetCategory);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public ResponseEntity<AssetCategory> update(@PathVariable Long id,
                                                @Valid @RequestBody AssetCategory assetCategory) {
        AssetCategory updated = assetCategoryService.update(id, assetCategory);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT','ROLE_KYTHUATVIEN','ROLE_OPERATOR','ROLE_REQUESTER','ROLE_VIEWER')")
    public ResponseEntity<Collection<AssetCategory>> getAll() {
        return ResponseEntity.ok(assetCategoryService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT','ROLE_KYTHUATVIEN','ROLE_OPERATOR','ROLE_REQUESTER','ROLE_VIEWER')")
    public ResponseEntity<AssetCategory> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assetCategoryService.getById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assetCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}