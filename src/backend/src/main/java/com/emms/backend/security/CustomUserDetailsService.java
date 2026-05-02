package com.emms.backend.security;

import com.emms.backend.entity.User;
import com.emms.backend.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = Logger.getLogger(CustomUserDetailsService.class.getName());

    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Username cannot be empty");
        }

        User user = userRepo.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (user.isLocked() || user.getStatus() == User.UserStatus.LOCKED) {
            throw new LockedException("Tài khoản đã bị khóa. Liên hệ ADMIN để mở khóa.");
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new DisabledException("Tài khoản không hoạt động.");
        }

        if (user.getRole() == null) {
            throw new UsernameNotFoundException("User has no role: " + username);
        }

        if (user.getRole().getRoleType() == null) {
            throw new UsernameNotFoundException("User roleType is null: " + username);
        }

        String authority = user.getRole().getRoleType().getAuthority();

        log.info("Loaded user: " + username + ", roleType: " + authority);

        return new CustomUserPrincipal(
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}