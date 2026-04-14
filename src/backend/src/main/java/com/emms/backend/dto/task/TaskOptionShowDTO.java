package com.emms.backend.dto.task;

import com.emms.backend.dto.audit.AuditShowDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for displaying task option details in API responses")
public class TaskOptionShowDTO extends AuditShowDTO {

    @Schema(description = "Option ID")
    private Long id;

    @Schema(description = "Option label")
    private String label;

    // ===== Constructor =====
    public TaskOptionShowDTO() {
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