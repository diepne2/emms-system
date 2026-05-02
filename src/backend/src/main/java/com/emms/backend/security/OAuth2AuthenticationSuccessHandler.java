package com.emms.backend.security;

import com.emms.backend.entity.User;
import com.emms.backend.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final OAuth2Properties oAuth2Properties;
    private final UserRepository userRepository;

    public OAuth2AuthenticationSuccessHandler(
            JwtUtil jwtUtil,
            OAuth2Properties oAuth2Properties,
            UserRepository userRepository
    ) {
        this.jwtUtil = jwtUtil;
        this.oAuth2Properties = oAuth2Properties;
        this.userRepository = userRepository;
        setDefaultTargetUrl("http://localhost:5173/oauth2/redirect");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        String targetUrl = determineTargetUrl(authentication);

        if (response.isCommitted()) {
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(Authentication authentication) {
        String targetUrl = resolveRedirectUri();

        if (authentication == null) {
            return buildErrorRedirect(targetUrl, "authentication_not_found");
        }

        User user = extractUser(authentication.getPrincipal());

        if (user == null) {
            return buildErrorRedirect(targetUrl, "oauth2_user_not_found");
        }

        String accessToken = jwtUtil.generateToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("userId", user.getUserId())
                .build()
                .toUriString();
    }

    private String resolveRedirectUri() {
        if (oAuth2Properties != null && oAuth2Properties.getRedirectUri() != null && !oAuth2Properties.getRedirectUri().isBlank()) {
            return oAuth2Properties.getRedirectUri().trim();
        }
        if (oAuth2Properties != null && oAuth2Properties.getSuccessRedirectUrl() != null && !oAuth2Properties.getSuccessRedirectUrl().isBlank()) {
            return oAuth2Properties.getSuccessRedirectUrl().trim();
        }
        return "http://localhost:5173/oauth2/redirect";
    }

    private String buildErrorRedirect(String targetUrl, String errorCode) {
        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", errorCode)
                .build()
                .toUriString();
    }

    private User extractUser(Object principal) {
        String username = null;

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        }

        if (username == null || username.isBlank()) {
            return null;
        }

        return userRepository.findByUsernameIgnoreCase(username.trim()).orElse(null);
    }
}