package com.emms.backend.service.impl;

import com.emms.backend.dto.auth.UpdatePasswordRequest;
import com.emms.backend.dto.user.ChangePasswordDTO;
import com.emms.backend.dto.user.UserProfileUpdateDTO;
import com.emms.backend.dto.user.UserResponseDTO;
import com.emms.backend.entity.Role;
import com.emms.backend.entity.User;
import com.emms.backend.entity.UserInvitation;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.UserMapper;
import com.emms.backend.repository.RoleRepository;
import com.emms.backend.repository.UserRepository;
import com.emms.backend.service.UserInvitationService;
import com.emms.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserInvitationService userInvitationService;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           UserInvitationService userInvitationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userInvitationService = userInvitationService;
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    @Override
    public UserResponseDTO getUserById(Long id, String currentUsername) {
        User targetUser = getUserEntityById(id);
        User currentUser = getUserEntityByUsernameOrEmail(currentUsername);

        boolean isAdminOrManager = hasAdminOrManagerRole(currentUser);
        boolean isSelf = targetUser.getUserId().equals(currentUser.getUserId());

        if (!isAdminOrManager && !isSelf) {
            throw new CustomException("Bạn không có quyền xem thông tin người dùng này", HttpStatus.FORBIDDEN);
        }

        return userMapper.toResponseDTO(targetUser);
    }

    @Override
    public UserResponseDTO getCurrentUserProfile(String username) {
        User currentUser = getUserEntityByUsernameOrEmail(username);
        return userMapper.toResponseDTO(currentUser);
    }

    @Override
    public UserResponseDTO updateMyProfile(String username, UserProfileUpdateDTO request) {
        if (request == null) {
            throw new CustomException("Dữ liệu cập nhật không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        User currentUser = getUserEntityByUsernameOrEmail(username);

        if (request.getFirstName() != null) {
            currentUser.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            currentUser.setLastName(request.getLastName().trim());
        }
        if (request.getPhone() != null) {
            currentUser.setPhone(request.getPhone().trim());
        }
        if (request.getJobTitle() != null) {
            currentUser.setJobTitle(request.getJobTitle().trim());
        }

        User savedUser = userRepository.save(currentUser);
        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    public void changeMyPassword(String username, ChangePasswordDTO request) {
        if (request == null) {
            throw new CustomException("Dữ liệu đổi mật khẩu không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        User currentUser = getUserEntityByUsernameOrEmail(username);
        changePassword(currentUser, request.getCurrentPassword(), request.getNewPassword());
    }

    @Override
    public void updateMyPassword(String username, UpdatePasswordRequest request) {
        if (request == null) {
            throw new CustomException("Dữ liệu đổi mật khẩu không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        User currentUser = getUserEntityByUsernameOrEmail(username);
        changePassword(currentUser, request.getCurrentPassword(), request.getNewPassword());
    }

    @Override
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (user == null || user.getUserId() == null) {
            throw new CustomException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new CustomException("Mật khẩu hiện tại không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new CustomException("Mật khẩu mới không được để trống", HttpStatus.BAD_REQUEST);
        }

        String trimmedCurrentPassword = currentPassword.trim();
        String trimmedNewPassword = newPassword.trim();

        if (!passwordEncoder.matches(trimmedCurrentPassword, user.getPassword())) {
            throw new CustomException("Mật khẩu hiện tại không đúng", HttpStatus.BAD_REQUEST);
        }

        if (trimmedCurrentPassword.equals(trimmedNewPassword)) {
            throw new CustomException("Mật khẩu mới không được trùng mật khẩu cũ", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(trimmedNewPassword));
        userRepository.save(user);
    }

    @Override
    public UserResponseDTO updateEnabled(Long id, boolean enabled) {
        User user = getUserEntityById(id);
        user.setEnabled(enabled);

        if (!enabled) {
            user.setStatus(User.UserStatus.INACTIVE);
        } else if (user.getStatus() == User.UserStatus.INACTIVE) {
            user.setStatus(User.UserStatus.ACTIVE);
        }

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO updateStatus(Long id, String status) {
        User user = getUserEntityById(id);

        if (status == null || status.isBlank()) {
            throw new CustomException("Trạng thái không được để trống", HttpStatus.BAD_REQUEST);
        }

        User.UserStatus newStatus;
        try {
            newStatus = User.UserStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(
                    "Trạng thái không hợp lệ. Chỉ chấp nhận: ACTIVE, INACTIVE, LOCKED",
                    HttpStatus.BAD_REQUEST
            );
        }

        user.setStatus(newStatus);

        if (newStatus == User.UserStatus.INACTIVE) {
            user.setEnabled(false);
        } else if (newStatus == User.UserStatus.ACTIVE) {
            user.setEnabled(true);
            user.setFailedAttempts(0);
        } else if (newStatus == User.UserStatus.LOCKED) {
            user.setEnabled(false);
        }

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO updateRole(Long id, Long roleId) {
        User user = getUserEntityById(id);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new CustomException("Không tìm thấy role", HttpStatus.NOT_FOUND));

        user.setRole(role);
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO findUserResponseByUsernameOrEmail(String usernameOrEmail) {
        User user = getUserEntityByUsernameOrEmail(usernameOrEmail);
        return userMapper.toResponseDTO(user);
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByUsernameOrEmail(usernameOrEmail.trim());
    }

    @Override
    public void deleteUser(Long id) {
        User user = getUserEntityById(id);
        userRepository.delete(user);
    }

    @Override
    public void inviteUsers(List<String> emails, Long roleId, String invitedBy) {
        if (emails == null || emails.isEmpty()) {
            throw new CustomException("Danh sách email không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (roleId == null) {
            throw new CustomException("roleId không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (invitedBy == null || invitedBy.isBlank()) {
            throw new CustomException("invitedBy không được để trống", HttpStatus.BAD_REQUEST);
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new CustomException("Không tìm thấy role", HttpStatus.NOT_FOUND));

        User inviter = getUserEntityByUsernameOrEmail(invitedBy);

        for (String rawEmail : emails) {
            if (rawEmail == null || rawEmail.isBlank()) {
                continue;
            }

            String email = rawEmail.trim().toLowerCase();

            if (userRepository.existsByEmail(email)) {
                continue;
            }

            UserInvitation invitation = new UserInvitation();
            invitation.setEmail(email);
            invitation.setRole(role);
            invitation.setCreatedBy(inviter);
            invitation.setToken(UUID.randomUUID().toString());
            invitation.setCreatedAt(LocalDateTime.now());
            invitation.setUsed(false);

            userInvitationService.create(invitation);
        }
    }

    @Override
    public User findEntityById(Long id) {
        if (id == null) {
            throw new CustomException("User id không được để trống", HttpStatus.BAD_REQUEST);
        }
        return getUserEntityById(id);
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User getByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new CustomException("Username hoặc email không được để trống", HttpStatus.BAD_REQUEST);
        }
        return getUserEntityByUsernameOrEmail(usernameOrEmail.trim());
    }

    @Override
    public User whoami(HttpServletRequest request) {
        if (request == null || request.getUserPrincipal() == null) {
            throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        String username = request.getUserPrincipal().getName();
        if (username == null || username.isBlank()) {
            throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        return getUserEntityByUsernameOrEmail(username);
    }

    @Override
    public String signin(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new CustomException("Email hoặc username không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (password == null || password.isBlank()) {
            throw new CustomException("Mật khẩu không được để trống", HttpStatus.BAD_REQUEST);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail.trim(), password)
            );

            User user = getUserEntityByUsernameOrEmail(usernameOrEmail.trim());
            user.markLoginSuccess();
            userRepository.save(user);

            return authentication.getName();
        } catch (BadCredentialsException ex) {
            Optional<User> optionalUser = userRepository.findByUsernameOrEmail(usernameOrEmail.trim());
            optionalUser.ifPresent(savedUser -> {
                savedUser.increaseFailedAttempts();
                userRepository.save(savedUser);
            });

            throw new CustomException("Sai tài khoản hoặc mật khẩu", HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            throw new CustomException("Đăng nhập thất bại", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public void createForgotPasswordToken(String email) {
        if (email == null || email.isBlank()) {
            throw new CustomException("Email không được để trống", HttpStatus.BAD_REQUEST);
        }

        User user = getUserEntityByUsernameOrEmail(email.trim().toLowerCase());

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordExpiry(LocalDateTime.now().plusMinutes(30));

        userRepository.save(user);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new CustomException("Token không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new CustomException("Mật khẩu mới không được để trống", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByResetPasswordToken(token.trim())
                .orElseThrow(() -> new CustomException("Token không hợp lệ", HttpStatus.BAD_REQUEST));

        if (user.getResetPasswordExpiry() == null
                || user.getResetPasswordExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException("Token đã hết hạn", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiry(null);
        user.setFailedAttempts(0);
        user.setEnabled(true);

        if (user.getStatus() == User.UserStatus.LOCKED || user.getStatus() == User.UserStatus.INACTIVE) {
            user.setStatus(User.UserStatus.ACTIVE);
        }

        userRepository.save(user);
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new CustomException("User không được null", HttpStatus.BAD_REQUEST);
        }
        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        User user = getUserEntityById(id);
        userRepository.delete(user);
    }

    private User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy người dùng", HttpStatus.NOT_FOUND));
    }

    private User getUserEntityByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new CustomException("username hoặc email không được để trống", HttpStatus.BAD_REQUEST);
        }

        return userRepository.findByUsernameOrEmail(usernameOrEmail.trim())
                .orElseThrow(() -> new CustomException("Không tìm thấy người dùng", HttpStatus.NOT_FOUND));
    }

    private boolean hasAdminOrManagerRole(User user) {
        if (user == null || user.getRole() == null || user.getRole().getCode() == null) {
            return false;
        }

        String roleCode = user.getRole().getCode().name();
        return "ADMIN".equals(roleCode)
                || "QUANLYKYTHUAT".equals(roleCode)
                || "TECHNICAL_MANAGER".equals(roleCode);
    }
}