package com.emms.backend.controller;

import com.emms.backend.dto.checklist.ChecklistDTO;
import com.emms.backend.dto.checklist.ChecklistPostDTO;
import com.emms.backend.entity.Checklist;
import com.emms.backend.exception.CustomException;
import com.emms.backend.service.ChecklistService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/api/checklists")
@Tag(name = "Checklists", description = "Operations on checklists")
@RequiredArgsConstructor
public class ChecklistController {

    private final ChecklistService checklistService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public Collection<Checklist> getAll() {
        return checklistService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public Checklist getById(@PathVariable("id") Long id) {
        return checklistService.findById(id)
                .orElseThrow(() -> new CustomException("Checklist not found", HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Checklist> create(
            @Parameter(description = "Checklist to create")
            @Valid @RequestBody ChecklistPostDTO checklistReq) {
        Checklist created = checklistService.createPost(checklistReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Checklist patch(
            @Parameter(description = "Checklist fields to update")
            @Valid @RequestBody ChecklistDTO checklistReq,
            @PathVariable("id") Long id) {
        return checklistService.update(id, checklistReq);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        checklistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}