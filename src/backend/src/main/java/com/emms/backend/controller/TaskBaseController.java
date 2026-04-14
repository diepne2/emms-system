package com.emms.backend.controller;

import com.emms.backend.dto.task.TaskBaseDTO;
import com.emms.backend.dto.task.TaskBasePatchDTO;
import com.emms.backend.entity.TaskBase;
import com.emms.backend.service.TaskBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/task-bases")
@RequiredArgsConstructor
@Tag(name = "Task Base", description = "Task base APIs")
public class TaskBaseController {

    private final TaskBaseService taskBaseService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    @Operation(summary = "Create task base")
    public ResponseEntity<TaskBase> create(@Valid @RequestBody TaskBaseDTO dto) {
        TaskBase created = taskBaseService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    @Operation(summary = "Update task base")
    public ResponseEntity<TaskBase> update(@PathVariable Long id,
                                           @Valid @RequestBody TaskBasePatchDTO dto) {
        TaskBase updated = taskBaseService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get task base by id")
    public ResponseEntity<TaskBase> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskBaseService.findById(id));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all task bases")
    public ResponseEntity<Collection<TaskBase>> getAll() {
        return ResponseEntity.ok(taskBaseService.getAll());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_QUANLYKYTHUAT')")
    @Operation(summary = "Delete task base")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskBaseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}