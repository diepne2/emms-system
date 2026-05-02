package com.emms.backend.controller;

import com.emms.backend.dto.part.UsePartDTO;
import com.emms.backend.dto.part.WorkOrderPartShowDTO;
import com.emms.backend.service.WorkOrderPartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-order-parts")
public class WorkOrderPartController {

    private final WorkOrderPartService service;

    public WorkOrderPartController(WorkOrderPartService service) {
        this.service = service;
    }

    @PostMapping("/{woId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void usePart(@PathVariable Long woId, @Valid @RequestBody UsePartDTO dto) {
        service.usePart(woId, dto.getPartId(), dto.getQuantity());
    }

    @GetMapping("/{woId}")
    public List<WorkOrderPartShowDTO> getParts(@PathVariable Long woId) {
        return service.getByWorkOrderDto(woId);
    }
}