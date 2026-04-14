package com.emms.backend.security;

import com.emms.backend.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2Properties oAuth2Properties;

    public OAuth2AuthenticationSuccessHandler(
            JwtTokenProvider jwtTokenProvider,
            OAuth2Properties oAuth2Properties
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.oAuth2Properties = oAuth2Properties;

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

        Long userId = resolveUserId(user);
        if (userId == null) {
            return buildErrorRedirect(targetUrl, "user_id_not_found");
        }

        String token = jwtTokenProvider.generateToken(authentication);

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token)
                .queryParam("userId", userId)
                .build()
                .toUriString();
    }

    private String resolveRedirectUri() {
        if (oAuth2Properties != null
                && oAuth2Properties.getRedirectUri() != null
                && !oAuth2Properties.getRedirectUri().isBlank()) {
            return oAuth2Properties.getRedirectUri().trim();
        }

        if (oAuth2Properties != null
                && oAuth2Properties.getSuccessRedirectUrl() != null
                && !oAuth2Properties.getSuccessRedirectUrl().isBlank()) {
            return oAuth2Properties.getSuccessRedirectUrl().trim();
        }

        String defaultTargetUrl = getDefaultTargetUrl();
        if (defaultTargetUrl == null || defaultTargetUrl.isBlank()) {
            return "http://localhost:5173/oauth2/redirect";
        }

        return defaultTargetUrl;
    }

    private String buildErrorRedirect(String targetUrl, String errorCode) {
        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", errorCode)
                .build()
                .toUriString();
    }

    private User extractUser(Object principal) {
        if (principal == null) {
            return null;
        }

        if (principal instanceof CustomUserDetail customUserDetail) {
            return customUserDetail.getUser();
        }

        return null;
    }

    private Long resolveUserId(User user) {
        if (user == null) {
            return null;
        }

        return user.getUserId();
    }
}