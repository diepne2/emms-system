package com.emms.backend.utils;


import com.emms.backend.entity.User;
import com.emms.backend.exception.CustomException;
import com.emms.backend.repository.UserRepository;
import com.emms.backend.security.CustomUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserPrincipal customUserPrincipal) {
            return userRepository.findById(customUserPrincipal.getUserId())
                    .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        }

        if (principal instanceof String username && !"anonymousUser".equals(username)) {
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        }

        throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }
}