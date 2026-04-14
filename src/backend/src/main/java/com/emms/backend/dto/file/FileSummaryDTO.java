package com.emms.backend.dto.file;

public class FileSummaryDTO {

    private Long id;
    private String name;
    private String url;

    public FileSummaryDTO() {
    }

    public FileSummaryDTO(Long id, String name, String url) {
        this.id = id;
        this.name = trim(name);
        this.url = trim(url);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = trim(name);
    }

    public void setUrl(String url) {
        this.url = trim(url);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}