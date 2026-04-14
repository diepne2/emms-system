package com.emms.backend.security;

import com.emms.backend.entity.User;
import com.emms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public CustomUserDetail loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Username or email must not be blank");
        }

        User user = userRepository.findByUsernameOrEmail(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return CustomUserDetail.builder()
                .user(user)
                .build();
    }
}