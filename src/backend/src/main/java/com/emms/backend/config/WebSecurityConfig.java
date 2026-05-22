package com.emms.backend.config;

import com.emms.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/avatar/**").permitAll()

                        .requestMatchers(
                                "/api/auth/**",
                                "/api/v1/auth/**", 
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/uploads/**",
                                "/api/users/avatar/**",
                                "/api/users/forgot-password",
                                "/api/users/reset-password",
                                "/api/auth/**",
                                "/api/ai/**"

                        ).permitAll()

                        .requestMatchers("/api/ai/**").permitAll()
                        .requestMatchers("/api/ai-risk/**").permitAll()
                        .requestMatchers("/api/ai-decision/**").permitAll()

                        .requestMatchers("/api/dashboard/**").permitAll()

                        .requestMatchers("/api/labors", "/api/labors/**").permitAll()

                        .requestMatchers("/preventive-maintenances/**").authenticated()

                        .requestMatchers("/api/chat/**").authenticated()

                        .requestMatchers("/api/ai-risk/**")
                        .hasAnyRole("ADMIN", "TECHNICAL_MANAGER", "TECHNICIAN")

                        .requestMatchers("/api/ai-decision/**")
                        
                        .hasAnyRole("ADMIN", "TECHNICAL_MANAGER", "TECHNICIAN")
                 
                        

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

}