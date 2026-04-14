package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.woflow.WorkflowPostDTO;
import com.emms.backend.entity.Workflow;
import com.emms.backend.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public Collection<Workflow> getAll() {
        return workflowService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public Workflow getById(@PathVariable("id") Long id) {
        return workflowService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUANLYKYTHUAT')")
    public Workflow create(@Valid @RequestBody WorkflowPostDTO dto) {
        return workflowService.create(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuccessResponse delete(@PathVariable("id") Long id) {
        workflowService.delete(id);
        return new SuccessResponse(true, "Deleted successfully");
    }
}