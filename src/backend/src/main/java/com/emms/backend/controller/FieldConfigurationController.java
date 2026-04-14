package com.emms.backend.controller;

import com.emms.backend.dto.fieldConfiguration.FieldConfigurationPatchDTO;
import com.emms.backend.entity.FieldConfiguration;
import com.emms.backend.entity.User;
import com.emms.backend.entity.enums.PermissionEntity;
import com.emms.backend.exception.CustomException;
import com.emms.backend.service.FieldConfigurationService;
import com.emms.backend.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/field-configurations")
@Tag(name = "Field Configuration", description = "Operations on field configurations")
public class FieldConfigurationController {

    private final FieldConfigurationService fieldConfigurationService;
    private final UserService userService;

    public FieldConfigurationController(FieldConfigurationService fieldConfigurationService,
                                        UserService userService) {
        this.fieldConfigurationService = fieldConfigurationService;
        this.userService = userService;
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public FieldConfiguration patch(
            @Parameter(description = "Field configuration fields to update")
            @Valid @RequestBody FieldConfigurationPatchDTO fieldConfiguration,
            @PathVariable("id") Long id,
            HttpServletRequest req
    ) {
        User user = userService.whoami(req);

        if (user == null || user.getUserId() == null) {
            throw new CustomException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        if (user.getRole() == null
                || user.getRole().getPermissions() == null
                || !user.getRole().getPermissions().contains(PermissionEntity.SETTINGS)) {
            throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        }

        fieldConfigurationService.findById(id)
                .orElseThrow(() -> new CustomException("Field configuration not found", HttpStatus.NOT_FOUND));

        return fieldConfigurationService.update(id, fieldConfiguration);
    }
}