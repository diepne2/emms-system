package com.emms.backend.config;

import com.emms.backend.entity.User;
import com.emms.backend.repository.UserRepository;
import com.emms.backend.security.CustomUserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditConfig {

    @Bean
    public AuditorAware<User> auditorProvider(UserRepository userRepository) {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken) {
                return Optional.empty();
            }

            Object principal = authentication.getPrincipal();

   
            if (principal instanceof CustomUserPrincipal customUserPrincipal) {
                return userRepository.findById(customUserPrincipal.getUserId());
            }

            // fallback
            if (principal instanceof String username && !"anonymousUser".equals(username)) {
                return userRepository.findByUsername(username);
            }

            return Optional.empty();
        };
    }
}