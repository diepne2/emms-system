package com.emms.backend.dto.checklist;

public class ChecklistSummaryDTO {

    private Long id;   
    private String name;

    // ===== Constructor =====
    public ChecklistSummaryDTO() {
    }

    public ChecklistSummaryDTO(Long id, String name) {
        this.id = id;
        this.name = trim(name);
    }

    // ===== Getter =====
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // ===== Setter =====
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = trim(name);
    }

    // ===== Utils =====
    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}