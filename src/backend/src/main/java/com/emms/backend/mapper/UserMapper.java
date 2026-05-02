package com.emms.backend.mapper;

import com.emms.backend.dto.user.UserResponseDTO;
import com.emms.backend.dto.user.UserSummaryDTO;
import com.emms.backend.entity.Role;
import com.emms.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());
        dto.setRole(extractRoleName(user.getRole()));
        dto.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setFullName(buildFullName(user));
        dto.setPhone(user.getPhone());
        dto.setJobTitle(user.getJobTitle());
        dto.setEnabled(user.isEnabled());
        dto.setFailedAttempts(user.getFailedAttempts());
        dto.setLastLogin(user.getLastLogin());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        return dto;
    }

    public UserSummaryDTO toSummaryDTO(User user) {
        if (user == null) {
            return null;
        }

        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setId(user.getUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setFullName(buildFullName(user));
        return dto;
    }

    private String buildFullName(User user) {
        if (user == null) {
            return null;
        }

        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String last = user.getLastName() == null ? "" : user.getLastName().trim();

        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? null : fullName;
    }

    private String extractRoleName(Role role) {
        if (role == null) {
            return null;
        }

        if (role.getName() != null && !role.getName().isBlank()) {
            return role.getName().trim();
        }

        if (role.getCode() != null) {
            return role.getCode().name();
        }

        return null;
    }
}