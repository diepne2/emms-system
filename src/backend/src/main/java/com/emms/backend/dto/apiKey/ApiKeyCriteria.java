package com.emms.backend.dto.apiKey;


public class ApiKeyCriteria {

    private String query;
    private Boolean active;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = trim(query);
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