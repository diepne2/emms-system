package com.emms.backend.config;

import com.emms.backend.entity.User;
import com.emms.backend.service.UserService;
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
public class AuditConfig {

    @Bean
    public AuditorAware<Long> auditorProvider(UserService userService) {
        return new SpringSecurityAuditorAware(userService);
    }

    public static class SpringSecurityAuditorAware implements AuditorAware<Long> {

        private final UserService userService;

        public SpringSecurityAuditorAware(UserService userService) {
            this.userService = userService;
        }

        @Override
        public Optional<Long> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken) {
                return Optional.empty();
            }

            String usernameOrEmail = authentication.getName();
            if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
                return Optional.empty();
            }

            return userService.findByUsernameOrEmail(usernameOrEmail.trim())
                    .map(User::getUserId);
        }
    }
}