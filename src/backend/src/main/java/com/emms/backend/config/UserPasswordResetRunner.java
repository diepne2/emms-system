package com.emms.backend.config;

import com.emms.backend.entity.User;
import com.emms.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserPasswordResetRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public UserPasswordResetRunner(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   Environment environment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        boolean enabled = Boolean.parseBoolean(
                environment.getProperty("app.reset-all-password.enabled", "false")
        );

        if (!enabled) {
            System.out.println("Skip UserPasswordResetRunner because app.reset-all-password.enabled=false");
            return;
        }

        String rawPassword = environment.getProperty("app.reset-all-password.value", "123456");

        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            System.out.println("No users found to reset password.");
            return;
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        for (User user : users) {
            user.setPassword(encodedPassword);
        }

        userRepository.saveAll(users);

        System.out.println("Reset password successfully for " + users.size() + " users.");
    }
}