package com.emms.backend.controller;

import com.emms.backend.dto.labor.LaborPatchDTO;
import com.emms.backend.entity.Labor;
import com.emms.backend.service.LaborService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/labors")
@RequiredArgsConstructor
public class LaborController {

    private final LaborService laborService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public ResponseEntity<Labor> create(@RequestBody @Valid Labor labor) {
        Labor createdLabor = laborService.create(labor);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLabor);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public ResponseEntity<Labor> update(
            @PathVariable Long id,
            @RequestBody @Valid LaborPatchDTO dto
    ) {
        Labor updatedLabor = laborService.update(id, dto);
        return ResponseEntity.ok(updatedLabor);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Collection<Labor>> getAll() {
        return ResponseEntity.ok(laborService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Labor> getById(@PathVariable Long id) {
        Labor labor = laborService.findEntityById(id);
        return ResponseEntity.ok(labor);
    }

    @GetMapping("/work-order/{workOrderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Collection<Labor>> getByWorkOrder(@PathVariable Long workOrderId) {
        return ResponseEntity.ok(laborService.findByWorkOrder(workOrderId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        laborService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/stop")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    public ResponseEntity<Labor> stop(@PathVariable Long id) {
        Labor labor = laborService.findEntityById(id);
        Labor stoppedLabor = laborService.stop(labor);
        return ResponseEntity.ok(stoppedLabor);
    }
}