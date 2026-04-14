package com.emms.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_role", columnList = "role_id"),
        @Index(name = "idx_user_status", columnList = "status"),
        @Index(name = "idx_user_reset_token", columnList = "reset_password_token")
})
public class User {

    // ================= ENUM =================
    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        LOCKED
    }

    // ================= ID =================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // ================= RELATION =================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // ================= BASIC =================
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "job_title", length = 150)
    private String jobTitle;

    @Column(name = "username", unique = true, nullable = false, length = 100)
    private String username;

    @JsonIgnore
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "email", unique = true, nullable = false, length = 150)
    private String email;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    // ================= RESET PASSWORD =================
    @Column(name = "reset_password_token", length = 255)
    private String resetPasswordToken;

    @Column(name = "reset_password_expiry")
    private LocalDateTime resetPasswordExpiry;

    // ================= AUDIT =================
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= CONSTRUCTOR =================
    public User() {
    }

    // ================= LIFECYCLE =================
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        normalize();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        normalize();
    }

    // ================= BUSINESS LOGIC =================
    private void normalize() {
        firstName = trim(firstName);
        lastName = trim(lastName);
        phone = trim(phone);
        jobTitle = trim(jobTitle);
        username = trim(username);
        email = trimLower(email);

        if (isBlank(firstName)) {
            throw new IllegalArgumentException("firstName không được để trống");
        }
        if (isBlank(lastName)) {
            throw new IllegalArgumentException("lastName không được để trống");
        }
        if (isBlank(username)) {
            throw new IllegalArgumentException("username không được để trống");
        }
        if (isBlank(password)) {
            throw new IllegalArgumentException("password không được để trống");
        }
        if (isBlank(email)) {
            throw new IllegalArgumentException("email không được để trống");
        }
        if (role == null) {
            throw new IllegalArgumentException("role không được null");
        }
    }

    public String getFullName() {
        String fn = firstName == null ? "" : firstName;
        String ln = lastName == null ? "" : lastName;
        return (fn + " " + ln).trim();
    }

    public boolean isLocked() {
        return status == UserStatus.LOCKED;
    }

    public void markLoginSuccess() {
        this.failedAttempts = 0;
        this.lastLogin = LocalDateTime.now();
        this.status = UserStatus.ACTIVE;
        this.enabled = true;
    }

    public void increaseFailedAttempts() {
        this.failedAttempts++;
        if (this.failedAttempts >= 5) {
            this.status = UserStatus.LOCKED;
            this.enabled = false;
        }
    }

    // ================= HELPER =================
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String trimLower(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    // ================= GETTER SETTER =================
    public Long getUserId() {
        return userId;
    }

    public Long getId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = trim(firstName);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = trim(lastName);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = trim(phone);
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = trim(jobTitle);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = trim(username);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = trimLower(email);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String token) {
        this.resetPasswordToken = trim(token);
    }

    public LocalDateTime getResetPasswordExpiry() {
        return resetPasswordExpiry;
    }

    public void setResetPasswordExpiry(LocalDateTime expiry) {
        this.resetPasswordExpiry = expiry;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}