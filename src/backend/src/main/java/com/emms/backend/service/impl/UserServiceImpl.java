package com.emms.backend.service.impl;
import com.emms.backend.entity.enums.RoleCode;
import com.emms.backend.dto.auth.UpdatePasswordRequest;
import com.emms.backend.dto.user.ChangePasswordDTO;
import com.emms.backend.dto.user.UserDropdownDTO;

import com.emms.backend.dto.user.UserProfileUpdateDTO;
import com.emms.backend.dto.user.UserResponseDTO;
import com.emms.backend.entity.Role;
import com.emms.backend.entity.User;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.UserMapper;
import com.emms.backend.repository.RoleRepository;
import com.emms.backend.repository.UserRepository;
import com.emms.backend.security.CustomUserPrincipal;
import com.emms.backend.security.JwtUtil;
import com.emms.backend.service.UserService;
import com.emms.backend.service.MailService;
import com.emms.backend.specification.UserSpecifications;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; 
    private static final String AVATAR_UPLOAD_DIR = "/app/uploads/avatars";

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final MailService mailService;

    @Override
    public String signin(String usernameOrEmail, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtUtil.generateToken(authentication.getName());

            log.info("Login success for user={}", authentication.getName());
            return token;
        } catch (Exception ex) {
            log.error("Login failed for usernameOrEmail={}: {}", usernameOrEmail, ex.getMessage(), ex);
            throw new CustomException("Sai tài khoản hoặc mật khẩu", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAllByOrderByFirstNameAscLastNameAsc()
                .stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    @Override
    public Page<UserResponseDTO> searchUsers(
            String keyword,
            String roleCode,
            Boolean enabled,
            String status,
            Pageable pageable
    ) {
        return userRepository.findAll(
                        UserSpecifications.filter(keyword, roleCode, enabled, status),
                        pageable
                )
                .map(userMapper::toResponseDTO);
    }

    @Override
    public List<UserDropdownDTO> getTechnicianDropdown() {
        List<User> users = userRepository.findAll(
                UserSpecifications.filter(null, "TECHNICIAN", true, "ACTIVE")
        );

        List<UserDropdownDTO> result = new ArrayList<>();
        for (User user : users) {
            if (user == null) {
                continue;
            }
            result.add(toDropdownDTO(user));
        }
        return result;
    }

    @Override
    public UserResponseDTO getUserById(Long id, String currentUsername) {
        User target = findEntityById(id);

        if (currentUsername == null || currentUsername.isBlank()) {
            throw new CustomException("Tên người dùng hiện tại là bắt buộc", HttpStatus.UNAUTHORIZED);
        }

        User currentUser = getByUsernameOrEmail(currentUsername);

        boolean isSelf = target.getUsername() != null
                && currentUser.getUsername() != null
                && target.getUsername().equalsIgnoreCase(currentUser.getUsername());

        if (!isSelf && !hasAdminOrManagerRole(currentUser)) {
            throw new CustomException("Bạn không có quyền truy cập người dùng này", HttpStatus.FORBIDDEN);
        }

        return userMapper.toResponseDTO(target);
    }

    @Override
    public UserResponseDTO getCurrentUserProfile(String username) {
        User user = getByUsernameOrEmail(username);
        return userMapper.toResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateMyProfile(String username, UserProfileUpdateDTO request) {
        if (request == null) {
            throw new CustomException("Dữ liệu cập nhật hồ sơ là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        User user = getByUsernameOrEmail(username);

        if (request.getFirstName() != null) {
            user.setFirstName(trim(request.getFirstName()));
        }
        if (request.getLastName() != null) {
            user.setLastName(trim(request.getLastName()));
        }
        if (request.getPhone() != null) {
            user.setPhone(trim(request.getPhone()));
        }
        if (request.getJobTitle() != null) {
            user.setJobTitle(trim(request.getJobTitle()));
        }

        User saved = userRepository.save(user);
        return userMapper.toResponseDTO(saved);
    }

    @Override
    public void changeMyPassword(String username, ChangePasswordDTO request) {
        if (request == null) {
            throw new CustomException("Yêu cầu thay đổi mật khẩu là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        User user = getByUsernameOrEmail(username);

        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();

        validateNewPassword(newPassword);

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new CustomException("Mật khẩu hiện tại không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new CustomException("Mật khẩu hiện tại không đúng", HttpStatus.BAD_REQUEST);
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new CustomException("Mật khẩu mới không được trùng mật khẩu cũ", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void updateMyPassword(String username, UpdatePasswordRequest request) {
        if (request == null || request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new CustomException("Dữ liệu cập nhật mật khẩu là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        User user = getByUsernameOrEmail(username);
        validateNewPassword(request.getNewPassword());

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new CustomException("Mật khẩu mới không được trùng mật khẩu cũ", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public UserResponseDTO updateEnabled(Long id, boolean enabled) {
        User user = findEntityById(id);
        user.setEnabled(enabled);
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    @Override
    public UserResponseDTO updateStatus(Long id, String status) {
        User user = findEntityById(id);

        if (status == null || status.isBlank()) {
            throw new CustomException("Trạng thái là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        applyStatus(user, status.trim().toUpperCase(Locale.ROOT));
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    @Override
    public UserResponseDTO updateRole(Long id, Long roleId) {
        if (roleId == null) {
            throw new CustomException("ID vai trò là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        User user = findEntityById(id);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new CustomException("Vai trò không tồn tại", HttpStatus.NOT_FOUND));

        user.setRole(role);
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    @Override
    public UserResponseDTO findUserResponseByUsernameOrEmail(String usernameOrEmail) {
        return userMapper.toResponseDTO(getByUsernameOrEmail(usernameOrEmail));
    }

    @Override
    public void deleteUser(Long id) {
        User existing = findEntityById(id);
        try {
            userRepository.delete(existing);
        } catch (Exception e) {
            throw new CustomException(
                "Không thể xóa người dùng vì đã được gán trong Work Order",
                HttpStatus.CONFLICT
            );
        }
    }

    @Override
    public void inviteUsers(List<String> emails, String roleName, String invitedBy) {
        if (emails == null || emails.isEmpty()) {
            throw new CustomException("Danh sách email không được để trống", HttpStatus.BAD_REQUEST);
        }
        
        if (roleName == null || roleName.isBlank()) {
            throw new CustomException("Vai trò không được để trống", HttpStatus.BAD_REQUEST);
        }
        
        String roleCode = roleName.trim().toUpperCase(Locale.ROOT);
        if (roleCode.startsWith("ROLE_")) {
            roleCode = roleCode.substring(5);
        }
        
        RoleCode roleEnum;
        try {
            roleEnum = RoleCode.valueOf(roleCode);
        } catch (IllegalArgumentException ex) {
            throw new CustomException("Vai trò không hợp lệ: " + roleName, HttpStatus.BAD_REQUEST);
        }
        
        
        Role role = roleRepository.findByCode(roleEnum)
            .orElseThrow(() -> new CustomException("Vai trò không tồn tại", HttpStatus.NOT_FOUND));
        int createdCount = 0;
        
        for (String rawEmail : emails) {
            String email = trim(rawEmail);
            
            if (email == null || email.isBlank()) {
                throw new CustomException("Email không được để trống", HttpStatus.BAD_REQUEST);
            }
            
            if (userRepository.existsByEmailIgnoreCase(email)) {
                throw new CustomException(
                    "Email đã tồn tại trong hệ thống: " + email,
                    HttpStatus.CONFLICT
                );
            }
            String tempPassword = generateTemporaryPassword();
            
            User user = new User();
            user.setEmail(email);
            user.setUsername(generateUsernameFromEmail(email));
            user.setEnabled(true);
            user.setRole(role);
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setFirstName("User");
            user.setLastName("Invited");
            
            
            trySetDefaultActiveStatus(user);
            
            String loginLink = frontendUrl + "/#/login";
            
            
            try {
                mailService.sendSimpleMessage(
                    new String[]{email},
                    "Lời mời tham gia hệ thống EMMS",
                    "Bạn đã được mời vào hệ thống Quản lý thiết bị và bảo trì EMMS.\n\n"
                            + "Email đăng nhập: " + email + "\n"
                            + "Mật khẩu tạm: " + tempPassword + "\n\n"
                            + "Đăng nhập tại: " + loginLink + "\n\n"
                            + "Vui lòng đổi mật khẩu sau khi đăng nhập."
                );
            } catch (Exception ex) {
                log.error("Send invite mail failed for {}: {}", email, ex.getMessage(), ex);
                    throw new CustomException(
                        "Không gửi được email mời đến: " + email,
                        HttpStatus.INTERNAL_SERVER_ERROR
                    );
            }
            
            userRepository.save(user);
            createdCount++;
        }
        log.info(
            "Invited users by {}: created={}, total={}",
            invitedBy,
            createdCount,
            emails.size()
        );
    }

    

    @Override
    public User findEntityById(Long id) {
        if (id == null) {
            throw new CustomException("ID người dùng là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException("Người dùng không tồn tại", HttpStatus.NOT_FOUND));
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
        return userRepository.findAllByOrderByFirstNameAscLastNameAsc();
    }

    @Override
    public User getByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new CustomException("Tên người dùng hoặc email là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        return userRepository.findByUsernameOrEmail(usernameOrEmail.trim())
                .orElseThrow(() -> new CustomException("Người dùng không tồn tại", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public User whoami(HttpServletRequest request) {
        return whoami();
    }

    @Override
    public void createForgotPasswordToken(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(email.trim());
        if (optionalUser.isEmpty()) {
            return; 
        }

        User user = optionalUser.get();
        String token = UUID.randomUUID().toString();

        user.setResetPasswordToken(token);
        user.setResetPasswordExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        String link = frontendUrl + "/#/reset-password?token=" + token;

        mailService.sendSimpleMessage(
                new String[]{user.getEmail()},
                "Reset password",
                "Click link để đổi mật khẩu: " + link + "\n\nLink hết hạn sau 15 phút."
        );

        log.info("Forgot password reset link sent to {}", user.getEmail());
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new CustomException("Reset token is required", HttpStatus.BAD_REQUEST);
        }

        validateNewPassword(newPassword);

        User user = userRepository.findByResetPasswordToken(token.trim())
                .orElseThrow(() -> new CustomException("Token không hợp lệ", HttpStatus.BAD_REQUEST));

        if (user.getResetPasswordExpiry() == null
                || user.getResetPasswordExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException("Token đã hết hạn", HttpStatus.BAD_REQUEST);
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new CustomException("Mật khẩu mới không được trùng mật khẩu cũ", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiry(null);
        userRepository.save(user);

        log.info("Password reset successfully for user={}", user.getEmail());
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new CustomException("Người dùng là bắt buộc", HttpStatus.BAD_REQUEST);
        }
        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        User existing = findEntityById(id);
        try {
            userRepository.delete(existing);
        } catch (Exception ex) {
            throw new CustomException(
                "Không thể xóa người dùng vì đã được sử dụng trong hệ thống",
                HttpStatus.CONFLICT
            );
        }
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByUsernameOrEmail(usernameOrEmail.trim());
    }

    @Override
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (user == null) {
            throw new CustomException("Người dùng là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new CustomException("Mật khẩu hiện tại là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new CustomException("Mật khẩu hiện tại không đúng", HttpStatus.BAD_REQUEST);
        }

        validateNewPassword(newPassword);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new CustomException("Mật khẩu mới không được trùng với mật khẩu cũ", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public User whoami() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new CustomException("Người dùng chưa đăng nhập", HttpStatus.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserPrincipal customUserPrincipal) {
            Long userId = customUserPrincipal.getUserId();

            if (userId == null) {
                throw new CustomException("Không xác định được id người dùng hiện tại", HttpStatus.UNAUTHORIZED);
            }

            return userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException("Người dùng không tồn tại", HttpStatus.NOT_FOUND));
        }

        String usernameOrEmail = authentication.getName();
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new CustomException("Không xác định được người dùng hiện tại", HttpStatus.UNAUTHORIZED);
        }

        return getByUsernameOrEmail(usernameOrEmail);
    }

    @Override
    public UserResponseDTO uploadAvatar(String username, MultipartFile file) {
        if (username == null || username.isBlank()) {
            throw new CustomException("Tên người dùng là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        if (file == null || file.isEmpty()) {
            throw new CustomException("Tệp avatar không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new CustomException("Avatar tối đa 5MB", HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equalsIgnoreCase("image/png")
                        || contentType.equalsIgnoreCase("image/jpeg")
                        || contentType.equalsIgnoreCase("image/jpg")
                        || contentType.equalsIgnoreCase("image/webp"))) {
            throw new CustomException("Chỉ chấp nhận JPG, PNG, WEBP", HttpStatus.BAD_REQUEST);
        }

        User user = getByUsernameOrEmail(username);

        try {
            String extension = getExtension(file.getOriginalFilename(), contentType);
            String fileName = UUID.randomUUID() + "." + extension;

            Path uploadDir = Paths.get(AVATAR_UPLOAD_DIR);
            Files.createDirectories(uploadDir);

            Path filePath = uploadDir.resolve(fileName);

            System.out.println("UPLOAD DIR = " + uploadDir.toAbsolutePath());
            System.out.println("FILE PATH = " + filePath.toAbsolutePath());

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            user.setAvatar("/api/users/avatar/" + fileName);

            User saved = userRepository.save(user);
            return userMapper.toResponseDTO(saved);

        } catch (IOException ex) {
            log.error("Upload avatar failed for user={}: {}", username, ex.getMessage(), ex);
            throw new CustomException("Upload avatar thất bại", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private UserDropdownDTO toDropdownDTO(User user) {
        UserDropdownDTO dto = new UserDropdownDTO();
        dto.setId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(buildFullName(user));

        if (user.getRole() != null && user.getRole().getCode() != null) {
            dto.setRoleCode(user.getRole().getCode().toString());
        }

        return dto;
    }

    private String buildFullName(User user) {
        if (user == null) {
            return null;
        }

        String firstName = trim(user.getFirstName());
        String lastName = trim(user.getLastName());

        if (firstName == null && lastName == null) {
            return user.getUsername();
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return (firstName + " " + lastName).trim();
    }

    private void validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new CustomException("Mật khẩu mới không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (newPassword.length() < 6) {
            throw new CustomException("Mật khẩu mới phải có ít nhất 6 ký tự", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean hasAdminOrManagerRole(User user) {
        if (user == null || user.getRole() == null || user.getRole().getCode() == null) {
            return false;
        }

        String code = normalizeRoleCode(user.getRole().getCode().toString());
        return "ADMIN".equals(code)
                || "TECHNICAL_MANAGER".equals(code)
                || "QUANLYKYTHUAT".equals(code);
    }

    private String normalizeRoleCode(String code) {
        if (code == null) {
            return "";
        }
        String value = code.trim().toUpperCase(Locale.ROOT);
        if (value.startsWith("ROLE_")) {
            value = value.substring(5);
        }
        return value;
    }

    private void applyStatus(User user, String normalizedStatus) {
        try {
            Method getter = user.getClass().getMethod("getStatus");
            Object currentStatus = getter.invoke(user);

            Method setter = null;
            for (Method method : user.getClass().getMethods()) {
                if ("setStatus".equals(method.getName()) && method.getParameterCount() == 1) {
                    setter = method;
                    break;
                }
            }

            if (setter == null) {
                throw new CustomException("Người dùng không tìm thấy bộ thiết lập trạng thái", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Class<?> parameterType = setter.getParameterTypes()[0];

            if (parameterType.isEnum()) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                Enum enumValue = Enum.valueOf((Class<Enum>) parameterType.asSubclass(Enum.class), normalizedStatus);
                setter.invoke(user, enumValue);
            } else if (parameterType == String.class) {
                setter.invoke(user, normalizedStatus);
            } else if (currentStatus != null && currentStatus.getClass().isEnum()) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                Enum enumValue = Enum.valueOf((Class<Enum>) currentStatus.getClass().asSubclass(Enum.class), normalizedStatus);
                setter.invoke(user, enumValue);
            } else {
                throw new CustomException("Loại trạng thái người dùng không được hỗ trợ", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (CustomException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw new CustomException("Trạng thái người dùng không hợp lệ: " + normalizedStatus, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            throw new CustomException("Không thể cập nhật trạng thái người dùng", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void trySetDefaultActiveStatus(User user) {
        try {
            applyStatus(user, "ACTIVE");
        } catch (Exception ignored) {
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }

    private String generateUsernameFromEmail(String email) {
        String atSafeEmail = email == null ? "" : email;
        int atIndex = atSafeEmail.indexOf("@");

        String base = (atIndex > 0 ? atSafeEmail.substring(0, atIndex) : atSafeEmail)
                .replaceAll("[^a-zA-Z0-9._]", "")
                .toLowerCase(Locale.ROOT);

        if (base.isBlank()) {
            base = "user";
        }

        String username = base;
        int counter = 1;
        while (userRepository.existsByUsernameIgnoreCase(username)) {
            username = base + counter;
            counter++;
        }
        return username;
    }

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String getExtension(String fileName, String contentType) {
        if (fileName != null && fileName.contains(".")) {
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1)
                    .trim()
                    .toLowerCase(Locale.ROOT);
            if (!ext.isBlank()) {
                return ext;
            }
        }

        if (contentType == null) {
            return "png";
        }

        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "png";
        };
    }
}