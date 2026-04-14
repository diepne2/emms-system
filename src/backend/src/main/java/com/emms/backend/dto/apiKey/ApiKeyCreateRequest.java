package com.emms.backend.dto.apiKey;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ApiKeyCreateRequest {

    @NotBlank
    @Size(max = 150)
    private String label;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = trim(label);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}