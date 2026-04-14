package com.emms.backend.entity.abstracts;

import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
public abstract class CategoryAbstract extends Audit {

    @NotBlank
    private String name;

    private String description;

    private boolean demo;

    public CategoryAbstract(String name) {
        this.name = normalize(name);
    }

    public void setName(String name) {
        this.name = normalize(name);
    }

    public void setDescription(String description) {
        this.description = normalizeNullable(description);
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }
}