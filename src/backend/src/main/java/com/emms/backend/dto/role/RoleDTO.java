package com.emms.backend.dto.role;

import com.emms.backend.entity.enums.PermissionEntity;

import java.util.HashSet;
import java.util.Set;

public class RoleDTO {

    private String name;
    private String description;
    private Boolean active;
    private Set<PermissionEntity> permissions = new HashSet<>();

    public RoleDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = trim(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = trim(description);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<PermissionEntity> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionEntity> permissions) {
        this.permissions = (permissions == null) ? new HashSet<>() : permissions;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}