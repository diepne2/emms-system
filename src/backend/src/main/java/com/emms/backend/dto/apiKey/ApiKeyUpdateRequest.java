package com.emms.backend.dto.apiKey;

import jakarta.validation.constraints.Size;

public class ApiKeyUpdateRequest {

    @Size(max = 150)
    private String label;

    private Boolean active;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = trim(label);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}