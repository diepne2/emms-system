package com.emms.backend.config;

import com.emms.backend.security.ApiKeyAuthFilter;
import com.emms.backend.security.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;
    private final ApiKeyAuthFilter apiKeyAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)

                // JWT / API key => stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        // public cơ bản
                        .requestMatchers("/", "/error").permitAll()

                        // auth public endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // oauth2 public endpoints
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // swagger / openapi
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // public request portal by uuid
                        .requestMatchers("/request-portals/uuid/**").permitAll()

                        // CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // protected routes ngoài /api
                        .requestMatchers(
                                "/notifications/**",
                                "/parts/**",
                                "/preventive-maintenances/**",
                                "/meter-categories/**",
                                "/field-configurations/**",
                                "/push-tokens/**",
                                "/import/**",
                                "/api-keys/**",
                                "/readings/**",
                                "/requests/**",
                                "/request-portals/**",
                                "/test/**"
                        ).authenticated()

                        // protected routes có /api
                        .requestMatchers("/api/**").authenticated()

                        // mặc định: phải authenticated
                        .anyRequest().authenticated()
                )

                // API key trước, JWT sau
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}