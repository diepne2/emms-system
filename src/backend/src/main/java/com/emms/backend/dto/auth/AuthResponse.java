package com.emms.backend.dto.auth;

import java.util.List;

public class AuthResponse {

    private String token;
    private String refreshToken;
    private String role;
    private List<String> roles;
    private List<String> permissions;

    public AuthResponse() {
    }

    public AuthResponse(String token,
                        String refreshToken,
                        String role,
                        List<String> roles,
                        List<String> permissions) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.role = role;
        this.roles = roles;
        this.permissions = permissions;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}