package com.emms.backend.security;

import com.emms.backend.exception.CustomException;
import com.emms.backend.entity.enums.RoleType;
import com.emms.backend.utils.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;

    @Value("${security.jwt.token.expire-length:3600000}")
    private long validityInMilliseconds;

    private SecretKey key;

    private final CustomUserDetailsService customUserDetailsService;

    // ================= INIT =================
    @PostConstruct
    protected void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(String username, List<RoleType> roles) {

    Claims claims = Jwts.claims().setSubject(username);

    List<String> authorities = roles == null
            ? List.of()
            : roles.stream()
                .filter(Objects::nonNull)
                .map(RoleType::name)
                .toList();

    claims.put("auth", authorities);

    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key)
            .compact();
}

    // ================= AUTHENTICATION =================
    public Authentication getAuthentication(String token) {

        String username = getUsername(token);

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(username);

        if (!userDetails.isEnabled()) {
            throw new CustomException("User account is disabled", HttpStatus.UNAUTHORIZED);
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    // ================= GET USERNAME =================
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // ================= RESOLVE TOKEN =================
    public String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith(Consts.TOKEN_PREFIX)) {
            return bearerToken.substring(Consts.TOKEN_PREFIX.length());
        }


        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("swagger_jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    // ================= VALIDATE =================
    public boolean validateToken(String token) {

        try {
            parseClaims(token);
            return true;

        } catch (ExpiredJwtException e) {
            throw new CustomException("JWT token expired", HttpStatus.UNAUTHORIZED);

        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException("Invalid JWT token", HttpStatus.UNAUTHORIZED);
        }
    }

    // ================= PARSE =================
    private Claims parseClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ================= OPTIONAL =================
    // nếu muốn lấy roles từ token (không bắt buộc)
    public List<SimpleGrantedAuthority> getAuthorities(String token) {

        Claims claims = parseClaims(token);

        Object auth = claims.get("auth");

        if (auth instanceof List<?> roles) {
            return roles.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    public String generateToken(Authentication authentication) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateToken'");
    }
}