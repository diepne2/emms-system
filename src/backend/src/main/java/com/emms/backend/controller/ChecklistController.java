package com.emms.backend.controller;

import com.emms.backend.dto.checklist.ChecklistDTO;
import com.emms.backend.entity.Checklist;
import com.emms.backend.service.ChecklistService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/checklists")
@Tag(name = "Checklists", description = "Operations on checklists")
@RequiredArgsConstructor
public class ChecklistController {

    private final ChecklistService checklistService;

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Checklist> getById(@PathVariable("id") Long id) {
        Checklist checklist = checklistService.findEntityById(id);
        return ResponseEntity.ok(checklist);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Checklist> create(
            @Parameter(description = "Checklist to create")
            @Valid @RequestBody ChecklistDTO checklistReq
    ) {
        Checklist created = checklistService.create(checklistReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Checklist> patch(
            @Parameter(description = "Checklist fields to update")
            @Valid @RequestBody ChecklistDTO checklistReq,
            @PathVariable("id") Long id
    ) {
        Checklist updated = checklistService.update(id, checklistReq);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        checklistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}