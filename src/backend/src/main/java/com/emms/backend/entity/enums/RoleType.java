package com.emms.backend.entity.enums;
import org.springframework.security.core.GrantedAuthority;

public enum RoleType implements GrantedAuthority {
    ROLE_ADMIN,
    ROLE_QUANLYKYTHUAT,
    ROLE_NHANVIENKYTHUAT,
    ROLE_NHANVIENVANHANH;

    public String getAuthority() {
        return name();
    }
}