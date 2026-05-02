package com.emms.backend.entity.enums;

import org.springframework.security.core.GrantedAuthority;
public enum RoleType implements GrantedAuthority {
    ROLE_ADMIN,
    ROLE_TECHNICAL_MANAGER,
    ROLE_TECHNICIAN,
    ROLE_OPERATOR;

    @Override
    public String getAuthority() {
        return name();
    }
}