package com.emms.backend.repository;

import com.emms.backend.entity.User;
import com.emms.backend.entity.User.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(Long userId);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
        select u
        from User u
        where lower(u.username) = lower(:value)
           or lower(u.email) = lower(:value)
    """)
    Optional<User> findByUsernameOrEmail(@Param("value") String value);

    Optional<User> findByResetPasswordToken(String token);

    List<User> findByStatus(UserStatus status);

    List<User> findByEnabled(Boolean enabled);

    List<User> findByEnabledTrue();
}