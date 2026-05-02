package com.emms.backend.controller;

import com.emms.backend.dto.workorder.WorkOrderChangeStatusDTO;
import com.emms.backend.dto.workorder.WorkOrderDTO;
import com.emms.backend.dto.workorder.WorkOrderPostDTO;
import com.emms.backend.dto.workorder.WorkOrderShowDTO;
import com.emms.backend.entity.User;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.exception.CustomException;
import com.emms.backend.service.UserService;
import com.emms.backend.service.WorkOrderService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
@Tag(name = "Work Orders", description = "Operations on work orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final UserService userService;

    public WorkOrderController(WorkOrderService workOrderService,
                               UserService userService) {
        this.workOrderService = workOrderService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<WorkOrderShowDTO> create(
            @Parameter(description = "Work order data to create")
            @Valid @RequestBody WorkOrderPostDTO dto
    ) {
        WorkOrderShowDTO result = workOrderService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<List<WorkOrderShowDTO>> getAll() {
        return ResponseEntity.ok(workOrderService.getAll());
    }


    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<List<WorkOrderShowDTO>> getMyWorkOrders(HttpServletRequest request) {
        User currentUser = userService.whoami(request);
        return ResponseEntity.ok(workOrderService.getWorkOrdersForUser(currentUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<WorkOrderShowDTO> getById(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        User currentUser = userService.whoami(request);
        workOrderService.checkAccessToWorkOrderId(id, currentUser);
        return ResponseEntity.ok(workOrderService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<WorkOrderShowDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderDTO dto,
            HttpServletRequest request
    ) {
        User currentUser = userService.whoami(request);
        workOrderService.checkAccessToWorkOrderId(id, currentUser);

        WorkOrderShowDTO result = workOrderService.update(id, dto);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN')")
    public ResponseEntity<WorkOrderShowDTO> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderChangeStatusDTO dto,
            HttpServletRequest request
    ) {
        User currentUser = userService.whoami(request);
        workOrderService.checkCanChangeStatus(id, currentUser, dto.getStatus());

        if (dto.getStatus() == null) {
            throw new CustomException("Status must not be null", HttpStatus.BAD_REQUEST);
        }

        WorkOrderShowDTO result = workOrderService.changeStatus(
                id,
                dto.getStatus(),
                dto.getFeedback()
        );

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<WorkOrderShowDTO> markCompleted(
            @PathVariable Long id,
            @RequestParam Long completedByUserId,
            @RequestParam(required = false) String feedback,
            HttpServletRequest request
    ) {
        User currentUser = userService.whoami(request);
        workOrderService.checkAccessToWorkOrderId(id, currentUser);

        WorkOrderShowDTO result = workOrderService.markCompleted(id, completedByUserId, feedback);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<WorkOrderShowDTO> archive(
            @PathVariable Long id,
            @RequestParam boolean archived,
            HttpServletRequest request
    ) {
        User currentUser = userService.whoami(request);
        workOrderService.checkAccessToWorkOrderId(id, currentUser);

        WorkOrderShowDTO result = workOrderService.archive(id, archived);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        User currentUser = userService.whoami(request);
        workOrderService.checkAccessToWorkOrderId(id, currentUser);

        workOrderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/entity")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<WorkOrder> getEntityById(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        User currentUser = userService.whoami(request);
        WorkOrder entity = workOrderService.checkAccessToWorkOrderId(id, currentUser);
        return ResponseEntity.ok(entity);
    }

    
}