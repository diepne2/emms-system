package com.emms.backend.dto.apiKey;

public class ApiKeyCreateResponse {

    private ApiKeyResponse apiKey;
    private String plainKey;

    public ApiKeyResponse getApiKey() {
        return apiKey;
    }

    public void setApiKey(ApiKeyResponse apiKey) {
        this.apiKey = apiKey;
    }

    public String getPlainKey() {
        return plainKey;
    }

    public void setPlainKey(String plainKey) {
        this.plainKey = plainKey;
    }
}