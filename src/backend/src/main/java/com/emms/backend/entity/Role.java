package com.emms.backend.entity;

import com.emms.backend.entity.User.UserStatus;
import com.emms.backend.entity.enums.PermissionEntity;
import com.emms.backend.entity.enums.RoleCode;
import com.emms.backend.entity.enums.RoleType;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_roles_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_roles_role_type", columnNames = "role_type")
        }
)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 50)
    private RoleType roleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, length = 50)
    private RoleCode code = RoleCode.USER_DEFINED;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 100)
    private Set<PermissionEntity> permissions = new HashSet<>();

    public Role() {
    }

    public Role(RoleType roleType, String name) {
        this.roleType = roleType;
        this.name = trim(name);
        this.code = mapCodeFromRoleType(roleType);
        this.active = true;
        this.permissions = new HashSet<>();
    }

    @PrePersist
    @PreUpdate
    private void normalize() {
        this.name = trim(this.name);
        this.description = trim(this.description);

        if (this.roleType == null) {
            throw new IllegalArgumentException("roleType không được để trống");
        }

        if (this.name == null || this.name.isBlank()) {
            throw new IllegalArgumentException("name không được để trống");
        }

        if (this.code == null) {
            this.code = mapCodeFromRoleType(this.roleType);
        }

        if (this.permissions == null) {
            this.permissions = new HashSet<>();
        }
    }

    private RoleCode mapCodeFromRoleType(RoleType roleType) {
        if (roleType == null) {
            return RoleCode.USER_DEFINED;
        }

        return switch (roleType) {
            case ROLE_ADMIN -> RoleCode.ADMIN;
            case ROLE_QUANLYKYTHUAT -> RoleCode.TECHNICAL_MANAGER;
            case ROLE_NHANVIENKYTHUAT -> RoleCode.TECHNICIAN;
            case ROLE_NHANVIENVANHANH -> RoleCode.OPERATOR;
        };
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    public boolean hasPermission(PermissionEntity permission) {
        return permission != null && permissions != null && permissions.contains(permission);
    }

    public boolean isAdminRole() {
        return this.roleType == RoleType.ROLE_ADMIN;
    }

    public boolean isTechnicalManagerRole() {
        return this.roleType == RoleType.ROLE_QUANLYKYTHUAT;
    }

    public boolean isTechnicianRole() {
        return this.roleType == RoleType.ROLE_NHANVIENKYTHUAT;
    }

    public boolean isOperatorRole() {
        return this.roleType == RoleType.ROLE_NHANVIENVANHANH;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
        if (this.code == null || this.code == RoleCode.USER_DEFINED) {
            this.code = mapCodeFromRoleType(roleType);
        }
    }

    public RoleCode getCode() {
        return code;
    }

    public void setCode(RoleCode code) {
        this.code = (code == null) ? RoleCode.USER_DEFINED : code;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<PermissionEntity> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionEntity> permissions) {
        this.permissions = (permissions == null) ? new HashSet<>() : permissions;
    }

    public void addPermission(PermissionEntity permission) {
        if (permission == null) {
            return;
        }
        if (this.permissions == null) {
            this.permissions = new HashSet<>();
        }
        this.permissions.add(permission);
    }

    public void removePermission(PermissionEntity permission) {
        if (permission == null || this.permissions == null) {
            return;
        }
        this.permissions.remove(permission);
    }

    public Object getResetPasswordExpiry() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getResetPasswordExpiry'");
    }

    public void setPassword(String encode) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setPassword'");
    }

    public void setResetPasswordToken(Object object) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setResetPasswordToken'");
    }

    public void setFailedAttempts(int i) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setFailedAttempts'");
    }

    public void setEnabled(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setEnabled'");
    }

    public void setStatus(UserStatus active2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setStatus'");
    }

    public UserStatus getStatus() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStatus'");
    }
}