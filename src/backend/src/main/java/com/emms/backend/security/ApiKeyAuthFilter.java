package com.emms.backend.security;

import com.emms.backend.entity.ApiKey;
import com.emms.backend.entity.User;
import com.emms.backend.exception.CustomException;
import com.emms.backend.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "x-api-key";

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        String rawApiKey = request.getHeader(API_KEY_HEADER);

        if (hasText(rawApiKey)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                authenticateByApiKey(rawApiKey.trim(), request);
            } catch (CustomException ex) {
                SecurityContextHolder.clearContext();
                writeError(response, ex.getHttpStatus(), ex.getMessage());
                return;
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
                writeError(response, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process API key");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateByApiKey(String plainApiKey, HttpServletRequest request) {
        Optional<ApiKey> optionalApiKey = apiKeyService.findActiveByPlainKey(plainApiKey);

        if (optionalApiKey.isEmpty()) {
            return;
        }

        ApiKey apiKey = optionalApiKey.get();
        User user = apiKey.getUser();

        if (user == null) {
            return;
        }

        CustomUserDetail customUserDetail = CustomUserDetail.builder()
                .user(user)
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        customUserDetail,
                        null,
                        customUserDetail.getAuthorities()
                );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void writeError(HttpServletResponse response,
                            HttpStatus status,
                            String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("""
                {
                  "message": "%s"
                }
                """.formatted(escapeJson(message)));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}