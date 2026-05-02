package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.part.PartPatchDTO;
import com.emms.backend.entity.Part;
import com.emms.backend.service.PartService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/parts")
@Tag(name = "Parts", description = "Operations on parts")
public class PartController {

    private final PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER','ROLE_TECHNICIAN','ROLE_OPERATOR')")
    public ResponseEntity<Collection<Part>> getAll() {
        return ResponseEntity.ok(partService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER','ROLE_TECHNICIAN','ROLE_OPERATOR')")
    public ResponseEntity<Part> getById(
            @Parameter(description = "Part ID") @PathVariable Long id
    ) {
        return ResponseEntity.ok(partService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<Part> create(
            @Parameter(description = "Part data to create")
            @Valid @RequestBody Part partReq
    ) {
        Part saved = partService.create(partReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<Part> patch(
            @Parameter(description = "Part fields to update")
            @Valid @RequestBody PartPatchDTO part,
            @Parameter(description = "Part ID") @PathVariable Long id
    ) {
        Part updated = partService.update(id, part);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<SuccessResponse> delete(
            @Parameter(description = "Part ID") @PathVariable Long id
    ) {
        partService.delete(id);
        return ResponseEntity.ok(new SuccessResponse(true, "Deleted successfully"));
    }

    @PutMapping("/{id}/increase-stock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<Part> increaseStock(
            @PathVariable Long id,
            @RequestParam Integer amount
    ) {
        return ResponseEntity.ok(partService.increaseStock(id, amount));
    }

    @PutMapping("/{id}/decrease-stock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<Part> decreaseStock(
            @PathVariable Long id,
            @RequestParam Integer amount
    ) {
        return ResponseEntity.ok(partService.decreaseStock(id, amount));
    }
}