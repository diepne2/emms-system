package com.emms.backend.dto.user;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "usernameOrEmail không được để trống")
    private String usernameOrEmail;

    @NotBlank(message = "password không được để trống")
    private String password;

    public UserLoginRequest() {
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = trim(usernameOrEmail);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = trim(password);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}