package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.task.TaskDTO;
import com.emms.backend.entity.Task;
import com.emms.backend.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<Collection<Task>> getAll() {
        return ResponseEntity.ok(taskService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<Task> getById(@PathVariable("id") Long id) {
        return taskService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new com.emms.backend.exception.CustomException(
                        "Task not found", HttpStatus.NOT_FOUND
                ));
    }

    @GetMapping("/work-order/{workOrderId}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<List<Task>> getByWorkOrder(@PathVariable Long workOrderId) {
        return ResponseEntity.ok(taskService.findByWorkOrder(workOrderId));
    }


    @GetMapping("/preventive-maintenance/{preventiveMaintenanceId}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<List<Task>> getByPreventiveMaintenance(@PathVariable Long preventiveMaintenanceId) {
        return ResponseEntity.ok(taskService.findByPreventiveMaintenance(preventiveMaintenanceId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<Task> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody TaskDTO dto
    ) {
        return ResponseEntity.ok(taskService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable("id") Long id) {
        taskService.delete(id);
        return ResponseEntity.ok(new SuccessResponse(true, "Deleted successfully"));
    }
}