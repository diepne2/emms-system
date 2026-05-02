package com.emms.backend.controller;

import com.emms.backend.dto.user.ChangePasswordDTO;
import com.emms.backend.dto.user.UserDropdownDTO;
import com.emms.backend.dto.user.UserInvitationRequestDTO;
import com.emms.backend.dto.user.UserProfileUpdateDTO;
import com.emms.backend.dto.user.UserResponseDTO;
import com.emms.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<Page<UserResponseDTO>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(
                userService.searchUsers(keyword, roleCode, enabled, status, pageable)
        );
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/technicians")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<List<UserDropdownDTO>> getTechnicians() {
        return ResponseEntity.ok(userService.getTechnicianDropdown());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER','TECHNICIAN','OPERATOR')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(userService.getUserById(id, principal.getName()));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> getMyProfile(Principal principal) {
        return ResponseEntity.ok(userService.getCurrentUserProfile(principal.getName()));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> updateMyProfile(
            @Valid @RequestBody UserProfileUpdateDTO request,
            Principal principal
    ) {
        return ResponseEntity.ok(userService.updateMyProfile(principal.getName(), request));
    }

    @PutMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changeMyPassword(
            @Valid @RequestBody ChangePasswordDTO request,
            Principal principal
    ) {
        userService.changeMyPassword(principal.getName(), request);
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    @PostMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> uploadMyAvatar(
            @RequestParam("file") MultipartFile file,
            Principal principal
    ) {
        return ResponseEntity.ok(userService.uploadAvatar(principal.getName(), file));
    }

    @PutMapping("/{id}/enabled")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<UserResponseDTO> updateEnabled(
            @PathVariable Long id,
            @RequestParam boolean enabled
    ) {
        return ResponseEntity.ok(userService.updateEnabled(id, enabled));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<UserResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(userService.updateStatus(id, status));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<UserResponseDTO> updateRole(
            @PathVariable Long id,
            @RequestParam Long roleId
    ) {
        return ResponseEntity.ok(userService.updateRole(id, roleId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<UserResponseDTO> findByUsernameOrEmail(@RequestParam String keyword) {
        return ResponseEntity.ok(userService.findUserResponseByUsernameOrEmail(keyword));
    }

    @PostMapping("/invite")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICAL_MANAGER')")
    public ResponseEntity<String> invite(
            @Valid @RequestBody UserInvitationRequestDTO request,
            Principal principal
    ) {
        userService.inviteUsers(
                request.getEmails(),
                request.getRoleId(),
                principal.getName()
        );
        return ResponseEntity.ok("Gửi lời mời thành công");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Xóa user thành công");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        userService.createForgotPasswordToken(email);
        return ResponseEntity.ok("Nếu email tồn tại, link reset đã được gửi");
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
        @RequestParam String token,
        @RequestParam String newPassword
    ) {
        userService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    @GetMapping("/avatar/{filename:.+}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("/app/uploads/avatars")
                    .resolve(filename)
                    .normalize()
                    .toAbsolutePath();

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}