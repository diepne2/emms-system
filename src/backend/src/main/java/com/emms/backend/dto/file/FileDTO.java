package com.emms.backend.dto.file;

public class FileDTO {

    private String name;

    public FileDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = trim(name);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}