package com.emms.backend.service;

import com.emms.backend.dto.role.RoleDTO;
import com.emms.backend.entity.Role;
import com.emms.backend.entity.enums.PermissionEntity;
import com.emms.backend.entity.enums.RoleCode;
import com.emms.backend.entity.enums.RoleType;
import com.emms.backend.exception.CustomException;
import com.emms.backend.repository.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role create(RoleType roleType, RoleDTO dto) {
        validateCreateInput(roleType, dto);

        String roleName = normalize(dto.getName());

        if (roleRepository.findByName(roleName).isPresent()) {
            throw new CustomException("Tên role đã tồn tại: " + roleName, HttpStatus.BAD_REQUEST);
        }

        Role role = new Role();
        role.setRoleType(roleType);
        role.setCode(mapCodeFromRoleType(roleType));
        role.setName(roleName);
        role.setDescription(normalize(dto.getDescription()));
        role.setActive(dto.getActive() == null || dto.getActive());
        role.setPermissions(safePermissions(dto.getPermissions()));

        return roleRepository.save(role);
    }

    public Role update(Long roleId, RoleDTO dto) {
        if (roleId == null) {
            throw new CustomException("roleId không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto == null) {
            throw new CustomException("Dữ liệu cập nhật role không được để trống", HttpStatus.BAD_REQUEST);
        }

        Role savedRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new CustomException("Không tìm thấy role với id: " + roleId, HttpStatus.NOT_FOUND));

        String newName = normalize(dto.getName());
        if (newName != null && !newName.equalsIgnoreCase(savedRole.getName())) {
            Optional<Role> existing = roleRepository.findByName(newName);
            if (existing.isPresent() && !existing.get().getRoleId().equals(roleId)) {
                throw new CustomException("Tên role đã tồn tại: " + newName, HttpStatus.BAD_REQUEST);
            }
            savedRole.setName(newName);
        }

        if (dto.getDescription() != null) {
            savedRole.setDescription(normalize(dto.getDescription()));
        }

        if (dto.getActive() != null) {
            savedRole.setActive(dto.getActive());
        }

        if (dto.getPermissions() != null) {
            savedRole.setPermissions(safePermissions(dto.getPermissions()));
        }

        if (savedRole.getRoleType() != null) {
            savedRole.setCode(mapCodeFromRoleType(savedRole.getRoleType()));
        }

        return roleRepository.save(savedRole);
    }

    @Transactional(readOnly = true)
    public Collection<Role> getAll() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Role getById(Long roleId) {
        if (roleId == null) {
            throw new CustomException("roleId không được để trống", HttpStatus.BAD_REQUEST);
        }

        return roleRepository.findById(roleId)
                .orElseThrow(() -> new CustomException("Không tìm thấy role với id: " + roleId, HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        String normalized = normalize(name);
        if (normalized == null) {
            return Optional.empty();
        }
        return roleRepository.findByName(normalized);
    }

    public void delete(Long roleId) {
        if (roleId == null) {
            throw new CustomException("roleId không được để trống", HttpStatus.BAD_REQUEST);
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new CustomException("Không tìm thấy role với id: " + roleId, HttpStatus.NOT_FOUND));

        roleRepository.delete(role);
    }

    public Role addPermission(Long roleId, PermissionEntity permission) {
        if (permission == null) {
            throw new CustomException("permission không được để trống", HttpStatus.BAD_REQUEST);
        }

        Role role = getById(roleId);
        role.addPermission(permission);
        return roleRepository.save(role);
    }

    public Role removePermission(Long roleId, PermissionEntity permission) {
        if (permission == null) {
            throw new CustomException("permission không được để trống", HttpStatus.BAD_REQUEST);
        }

        Role role = getById(roleId);
        role.removePermission(permission);
        return roleRepository.save(role);
    }

    private void validateCreateInput(RoleType roleType, RoleDTO dto) {
        if (roleType == null) {
            throw new CustomException("roleType không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto == null) {
            throw new CustomException("Dữ liệu role không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (normalize(dto.getName()) == null) {
            throw new CustomException("Tên role không được để trống", HttpStatus.BAD_REQUEST);
        }
    }

    private Set<PermissionEntity> safePermissions(Set<PermissionEntity> permissions) {
        return permissions == null ? new HashSet<>() : new HashSet<>(permissions);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private RoleCode mapCodeFromRoleType(RoleType roleType) {
        if (roleType == null) {
            return RoleCode.OPERATOR;
        }

        return switch (roleType) {
            case ROLE_ADMIN -> RoleCode.ADMIN;
            case ROLE_TECHNICAL_MANAGER -> RoleCode.TECHNICAL_MANAGER;
            case ROLE_TECHNICIAN -> RoleCode.TECHNICIAN;
            case ROLE_OPERATOR -> RoleCode.OPERATOR;
        };
    }
}