package com.emms.backend.controller;

import com.emms.backend.dto.labor.LaborCreateDTO;
import com.emms.backend.dto.labor.LaborPatchDTO;
import com.emms.backend.dto.labor.LaborShowDTO;
import com.emms.backend.service.LaborService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labors")
public class LaborController {

    private final LaborService laborService;

    public LaborController(LaborService laborService) {
        this.laborService = laborService;
    }

    @PostMapping
    public ResponseEntity<LaborShowDTO> create(@RequestBody LaborCreateDTO dto) {
        return ResponseEntity.ok(laborService.create(dto));
    }

    @GetMapping("/my")
    public ResponseEntity<List<LaborShowDTO>> getMyLabors() {
        return ResponseEntity.ok(laborService.getMyLabors());
    }

    @GetMapping
    public ResponseEntity<List<LaborShowDTO>> getAll() {
        return ResponseEntity.ok(laborService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaborShowDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(laborService.getById(id));
    }

    @GetMapping("/work-order/{workOrderId}")
    public ResponseEntity<List<LaborShowDTO>> findByWorkOrder(@PathVariable Long workOrderId) {
        return ResponseEntity.ok(laborService.findByWorkOrder(workOrderId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LaborShowDTO> update(
            @PathVariable Long id,
            @RequestBody LaborPatchDTO dto
    ) {
        return ResponseEntity.ok(laborService.update(id, dto));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<LaborShowDTO> stop(@PathVariable Long id) {
        return ResponseEntity.ok(laborService.stop(id));
    }
}