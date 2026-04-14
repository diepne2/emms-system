package com.emms.backend.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for patching a task option")
public class TaskOptionPatchDTO {

    @Schema(description = "Option ID (required for update)")
    private Long id;

    @Schema(description = "Option label")
    private String label;

    // ===== Constructor =====
    public TaskOptionPatchDTO() {
    }

    // ===== Getter & Setter =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = trim(label);
    }

    // ===== Utils =====
    private String trim(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}