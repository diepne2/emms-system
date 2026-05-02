package com.emms.backend.controller;

import com.emms.backend.dto.auth.AuthResponse;
import com.emms.backend.dto.auth.LoginRequest;
import com.emms.backend.dto.user.UserResponseDTO;
import com.emms.backend.entity.User;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.UserMapper;
import com.emms.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;

    public AuthController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        if (request == null
                || request.getUsernameOrEmail() == null
                || request.getUsernameOrEmail().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {
            throw new CustomException("Dữ liệu đăng nhập không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        String usernameOrEmail = request.getUsernameOrEmail().trim();
        String password = request.getPassword();

        String accessToken = userService.signin(usernameOrEmail, password);

        User user = userService.getByUsernameOrEmail(usernameOrEmail);
        if (user == null) {
            throw new CustomException("Không tìm thấy người dùng", HttpStatus.UNAUTHORIZED);
        }
        if (user.getRole() == null) {
            throw new CustomException("Người dùng chưa được gán role", HttpStatus.UNAUTHORIZED);
        }
        if (user.getRole().getRoleType() == null) {
            throw new CustomException("RoleType không hợp lệ", HttpStatus.UNAUTHORIZED);
        }

        String primaryRole = user.getRole().getRoleType().getAuthority();
        List<String> roles = List.of(primaryRole);

        List<String> permissions = user.getRole().getPermissions() == null
                ? List.of()
                : user.getRole().getPermissions().stream()
                .filter(p -> p != null)
                .map(Enum::name)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        AuthResponse response = new AuthResponse(
                accessToken,
                accessToken,
                primaryRole,
                roles,
                permissions
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> whoami(HttpServletRequest request) {
        User user = userService.whoami(request);
        return ResponseEntity.ok(userMapper.toResponseDTO(user));
    }
}