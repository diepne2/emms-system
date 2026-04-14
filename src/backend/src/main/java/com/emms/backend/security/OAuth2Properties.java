package com.emms.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Properties {

    private String redirectUri;
    private String successRedirectUrl;
    private String failureRedirectUrl;
    private String provider;

    public String getRedirectUri() {
        return trim(redirectUri);
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = trim(redirectUri);
    }

    public String getSuccessRedirectUrl() {
        return trim(successRedirectUrl);
    }

    public void setSuccessRedirectUrl(String successRedirectUrl) {
        this.successRedirectUrl = trim(successRedirectUrl);
    }

    public String getFailureRedirectUrl() {
        return trim(failureRedirectUrl);
    }

    public void setFailureRedirectUrl(String failureRedirectUrl) {
        this.failureRedirectUrl = trim(failureRedirectUrl);
    }

    public String getProvider() {
        return trim(provider);
    }

    public void setProvider(String provider) {
        this.provider = trim(provider);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}