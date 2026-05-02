package com.emms.backend.security;

import com.emms.backend.entity.User;
import com.emms.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = Logger.getLogger(JwtAuthenticationFilter.class.getName());

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path.startsWith("/uploads/")) {
            return true;
        }

        if (path.equals("/api/labors") || path.startsWith("/api/labors/")) {
            return true;
        
        }


        String method = request.getMethod();

        return "POST".equalsIgnoreCase(method) && (
                "/api/auth/login".equals(path) ||
                "/api/auth/refresh".equals(path) ||
                "/api/auth/forgot-password".equals(path) ||
                "/api/auth/reset-password".equals(path)
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7).trim();
            if (token.isEmpty()) {
                writeUnauthorized(response, request, "JWT token is blank");
                return;
            }

            String username = jwtUtil.extractUsername(token);

            if (username == null || username.isBlank()) {
                writeUnauthorized(response, request, "Invalid JWT subject");
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findByUsernameIgnoreCase(username.trim()).orElse(null);

                if (user == null) {
                    writeUnauthorized(response, request, "User not found: " + username);
                    return;
                }

                if (user.getRole() == null) {
                    writeUnauthorized(response, request, "User has no role assigned");
                    return;
                }

                if (user.getRole().getRoleType() == null) {
                    writeUnauthorized(response, request, "User roleType is invalid");
                    return;
                }

                if (user.isLocked() || user.getStatus() == User.UserStatus.LOCKED) {
                    writeUnauthorized(response, request, "User account is locked");
                    return;
                }

                if (user.getStatus() != User.UserStatus.ACTIVE) {
                    writeUnauthorized(response, request, "User account is not active");
                    return;
                }

                if (!jwtUtil.isValidToken(token, user.getUsername())) {
                    writeUnauthorized(response, request, "Invalid or expired JWT");
                    return;
                }

                List<GrantedAuthority> authorities = buildAuthorities(user);

                log.info("JWT subject = " + username);
                log.info("DB roleType = " + user.getRole().getRoleType().name());
                log.info("Built authorities = " + authorities);

                CustomUserPrincipal principal = new CustomUserPrincipal(
                        user.getUserId(),
                        user.getUsername(),
                        user.getPassword(),
                        authorities
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("Authenticated user: " + user.getUsername());
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            log.warning("JWT authentication error: " + ex.getMessage());
            SecurityContextHolder.clearContext();
            writeUnauthorized(
                    response,
                    request,
                    ex.getMessage() == null || ex.getMessage().isBlank()
                            ? "Authentication failed"
                            : ex.getMessage()
            );
        }
    }

    private List<GrantedAuthority> buildAuthorities(User user) {
        if (user.getRole() == null || user.getRole().getRoleType() == null) {
            return List.of();
        }

        String authority = user.getRole().getRoleType().getAuthority();
        return List.of(new SimpleGrantedAuthority(authority));
    }

    private void writeUnauthorized(HttpServletResponse response,
                                   HttpServletRequest request,
                                   String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String json = """
                {
                  "timestamp": "%s",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "%s",
                  "path": "%s"
                }
                """.formatted(
                LocalDateTime.now(),
                escapeJson(message),
                request.getRequestURI()
        );

        response.getWriter().write(json);
    }

    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}