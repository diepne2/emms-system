package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.part.UsePartDTO;
import com.emms.backend.dto.part.WorkOrderPartShowDTO;
import com.emms.backend.service.WorkOrderPartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-order-parts")
public class WorkOrderPartController {

    private final WorkOrderPartService service;

    public WorkOrderPartController(WorkOrderPartService service) {
        this.service = service;
    }

  
    @PostMapping("/{workOrderId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER','ROLE_TECHNICIAN')")
    public ResponseEntity<SuccessResponse> usePart(
            @PathVariable Long workOrderId,
            @Valid @RequestBody UsePartDTO dto
    ) {
        service.usePart(
                workOrderId,
                dto.getPartId(),
                dto.getQuantity()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SuccessResponse(true, "Xuất vật tư cho Work Order thành công")
        );
    }

  
    @GetMapping("/{workOrderId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TECHNICAL_MANAGER','ROLE_TECHNICIAN','ROLE_OPERATOR')")
    public ResponseEntity<List<WorkOrderPartShowDTO>> getParts(
            @PathVariable Long workOrderId
    ) {
        return ResponseEntity.ok(
                service.getByWorkOrderDto(workOrderId)
        );
    }
}