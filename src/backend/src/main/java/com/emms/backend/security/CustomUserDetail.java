package com.emms.backend.security;

import com.emms.backend.entity.Role;
import com.emms.backend.entity.User;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Builder
public class CustomUserDetail implements UserDetails {

    private static final long serialVersionUID = 1L;

    private User user;

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user == null) {
            return Collections.emptyList();
        }

        Role role = user.getRole();
        if (role == null) {
            return Collections.emptyList();
        }

        /*
         * Ưu tiên role.getName()
         * Ví dụ DB lưu:
         * ROLE_ADMIN
         * ROLE_TECHNICAL_MANAGER
         * ROLE_TECHNICIAN
         * ROLE_OPERATOR
         */
        if (role.getName() != null && !role.getName().isBlank()) {
            return Collections.singletonList(
                    new SimpleGrantedAuthority(role.getName())
            );
        }

        /*
         * Fallback nếu hệ thống bạn dùng enum code
         * Ví dụ: ADMIN -> ROLE_ADMIN
         */
        if (role.getCode() != null) {
            return Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + role.getCode().name())
            );
        }

        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return user != null ? user.getPassword() : null;
    }

    @Override
    public String getUsername() {
        /*
         * Trả về username để SecurityContextHolder.getContext().getAuthentication().getName()
         * đồng bộ với user thật trong hệ thống.
         */
        return user != null ? user.getUsername() : null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        /*
         * Nếu sau này bạn có field locked riêng thì đổi tại đây.
         * Hiện tại dùng enabled như một điều kiện tối thiểu.
         */
        return user != null && user.isEnabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user != null && user.isEnabled();
    }
}