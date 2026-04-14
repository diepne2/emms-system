package com.emms.backend.service;

import com.emms.backend.dto.auth.UpdatePasswordRequest;
import com.emms.backend.dto.user.ChangePasswordDTO;
import com.emms.backend.dto.user.UserProfileUpdateDTO;
import com.emms.backend.dto.user.UserResponseDTO;
import com.emms.backend.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(Long id, String currentUsername);

    UserResponseDTO getCurrentUserProfile(String username);

    UserResponseDTO updateMyProfile(String username, UserProfileUpdateDTO request);

    void changeMyPassword(String username, ChangePasswordDTO request);

    void updateMyPassword(String username, UpdatePasswordRequest request);

    UserResponseDTO updateEnabled(Long id, boolean enabled);

    UserResponseDTO updateStatus(Long id, String status);

    UserResponseDTO updateRole(Long id, Long roleId);

    UserResponseDTO findUserResponseByUsernameOrEmail(String usernameOrEmail);

    void deleteUser(Long id);

    void inviteUsers(List<String> emails, Long roleId, String invitedBy);

    User findEntityById(Long id);

    Optional<User> findById(Long id);

    List<User> findAll();

    User getByUsernameOrEmail(String usernameOrEmail);

    User whoami(HttpServletRequest request);

    String signin(String usernameOrEmail, String password);

    void createForgotPasswordToken(String email);

    void resetPassword(String token, String newPassword);

    User save(User user);

    void deleteById(Long id);

    Optional<User> findByUsernameOrEmail(String usernameOrEmail);

    void changePassword(User user, String currentPassword, String newPassword);
}