package com.emms.backend.controller;

import com.emms.backend.dto.checklist.ChecklistDTO;
import com.emms.backend.entity.Checklist;
import com.emms.backend.service.ChecklistService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checklists")
@Tag(name = "Checklists", description = "Operations on checklists")
@RequiredArgsConstructor
public class ChecklistController {

    private final ChecklistService checklistService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Checklist>> search(
            @RequestParam(value = "q", required = false) String q
    ) {
        return ResponseEntity.ok(checklistService.search(q));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Checklist> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(checklistService.findEntityById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<Checklist> create(
            @Parameter(description = "Checklist to create")
            @Valid @RequestBody ChecklistDTO checklistReq
    ) {
        Checklist created = checklistService.create(checklistReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<Checklist> patch(
            @Parameter(description = "Checklist fields to update")
            @Valid @RequestBody ChecklistDTO checklistReq,
            @PathVariable("id") Long id
    ) {
        Checklist updated = checklistService.update(id, checklistReq);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        checklistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}