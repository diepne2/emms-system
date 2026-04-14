package com.emms.backend.dto.requestPortal;

public class RequestPortalSummaryDTO {

    private Long id;
    private String title;
    private String uuid;

    public RequestPortalSummaryDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = trim(title);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = trim(uuid);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}