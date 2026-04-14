package com.emms.backend.controller;

import com.emms.backend.dto.user.ChangePasswordDTO;
import com.emms.backend.dto.user.UserInvitationRequestDTO;
import com.emms.backend.dto.user.UserProfileUpdateDTO;
import com.emms.backend.dto.user.UserResponseDTO;
import com.emms.backend.dto.user.UserSummaryDTO;
import com.emms.backend.mapper.UserMapper;
import com.emms.backend.repository.UserRepository;
import com.emms.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserController(UserService userService,
                          UserRepository userRepository,
                          UserMapper userMapper) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
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
    public ResponseEntity<UserResponseDTO> updateMyProfile(@Valid @RequestBody UserProfileUpdateDTO request,
                                                           Principal principal) {
        return ResponseEntity.ok(userService.updateMyProfile(principal.getName(), request));
    }

    @PutMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changeMyPassword(@Valid @RequestBody ChangePasswordDTO request,
                                                   Principal principal) {
        userService.changeMyPassword(principal.getName(), request);
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    @PutMapping("/{id}/enabled")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<UserResponseDTO> updateEnabled(@PathVariable Long id,
                                                         @RequestParam boolean enabled) {
        return ResponseEntity.ok(userService.updateEnabled(id, enabled));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<UserResponseDTO> updateStatus(@PathVariable Long id,
                                                        @RequestParam String status) {
        return ResponseEntity.ok(userService.updateStatus(id, status));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<UserResponseDTO> updateRole(@PathVariable Long id,
                                                      @RequestParam Long roleId) {
        return ResponseEntity.ok(userService.updateRole(id, roleId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<UserResponseDTO> findByUsernameOrEmail(@RequestParam String keyword) {
        return ResponseEntity.ok(userService.findUserResponseByUsernameOrEmail(keyword));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Xóa user thành công");
    }

    @PostMapping("/invite")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT')")
    public ResponseEntity<String> invite(@Valid @RequestBody UserInvitationRequestDTO request,
                                         Principal principal) {
        userService.inviteUsers(
                request.getEmails(),
                request.getRoleId(),
                principal.getName()
        );
        return ResponseEntity.ok("Gửi lời mời thành công");
    }

    @GetMapping("/mini")
    @PreAuthorize("hasAnyRole('ADMIN','QUANLYKYTHUAT','NHANVIENKYTHUAT','NHANVIENVANHANH')")
    public ResponseEntity<List<UserSummaryDTO>> getMini() {
        List<UserSummaryDTO> result = userRepository.findByEnabledTrue()
                .stream()
                .map(userMapper::toSummaryDTO)
                .toList();

        return ResponseEntity.ok(result);
    }
}