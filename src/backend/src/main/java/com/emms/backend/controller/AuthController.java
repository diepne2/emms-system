package com.emms.backend.controller;

import com.emms.backend.dto.SuccessResponse;
import com.emms.backend.dto.auth.AuthResponse;
import com.emms.backend.dto.auth.ForgotPasswordRequest;
import com.emms.backend.dto.auth.LoginRequest;
import com.emms.backend.dto.auth.ResetPasswordRequest;
import com.emms.backend.dto.auth.UpdatePasswordRequest;
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;

    public AuthController(UserService userService,
                          UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new CustomException("Email không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new CustomException("Mật khẩu không được để trống", HttpStatus.BAD_REQUEST);
        }

        String token = userService.signin(
                request.getEmail().trim().toLowerCase(),
                request.getPassword()
        );

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> whoami(HttpServletRequest request) {
        User user = userService.whoami(request);
        return ResponseEntity.ok(userMapper.toResponseDTO(user));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<SuccessResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new CustomException("Email không được để trống", HttpStatus.BAD_REQUEST);
        }

        userService.createForgotPasswordToken(request.getEmail().trim().toLowerCase());

        return ResponseEntity.ok(new SuccessResponse(true, "Password reset email sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<SuccessResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        if (request == null || request.getToken() == null || request.getToken().isBlank()) {
            throw new CustomException("Token không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new CustomException("Mật khẩu mới không được để trống", HttpStatus.BAD_REQUEST);
        }

        userService.resetPassword(
                request.getToken().trim(),
                request.getNewPassword().trim()
        );

        return ResponseEntity.ok(new SuccessResponse(true, "Password reset successfully"));
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> changePassword(@Valid @RequestBody UpdatePasswordRequest request,
                                                          HttpServletRequest httpRequest) {
        User user = userService.whoami(httpRequest);

        if (user == null || user.getUserId() == null) {
            throw new CustomException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        userService.changePassword(
                user,
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(new SuccessResponse(true, "Password changed successfully"));
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse> deleteMyAccount(HttpServletRequest request) {
        User user = userService.whoami(request);

        if (user == null || user.getUserId() == null) {
            throw new CustomException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        userService.deleteById(user.getUserId());
        return ResponseEntity.ok(new SuccessResponse(true, "Account deleted successfully"));
    }
}