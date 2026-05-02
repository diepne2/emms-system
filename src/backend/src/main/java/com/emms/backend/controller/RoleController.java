package com.emms.backend.controller;

import com.emms.backend.dto.role.RoleDTO;
import com.emms.backend.entity.Role;
import com.emms.backend.entity.enums.PermissionEntity;
import com.emms.backend.entity.enums.RoleType;
import com.emms.backend.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> createRole(
            @RequestParam("roleType") RoleType roleType,
            @Valid @RequestBody RoleDTO dto
    ) {
        Role createdRole = roleService.create(roleType, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleDTO dto
    ) {
        return ResponseEntity.ok(roleService.update(roleId, dto));
    }

    @GetMapping
    public ResponseEntity<Collection<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAll());
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long roleId) {
        return ResponseEntity.ok(roleService.getById(roleId));
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        roleService.delete(roleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> addPermission(
            @PathVariable Long roleId,
            @RequestParam("permission") PermissionEntity permission
    ) {
        return ResponseEntity.ok(roleService.addPermission(roleId, permission));
    }

    @DeleteMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> removePermission(
            @PathVariable Long roleId,
            @RequestParam("permission") PermissionEntity permission
    ) {
        return ResponseEntity.ok(roleService.removePermission(roleId, permission));
    }
}