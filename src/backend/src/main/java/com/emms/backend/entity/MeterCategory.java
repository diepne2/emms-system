package com.emms.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "meter_categories", indexes = {
        @Index(name = "idx_meter_category_name", columnList = "name")
})
public class MeterCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meter_category_id")
    private Long meterCategoryId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    public MeterCategory() {
    }

    public MeterCategory(String name) {
        this.name = name;
    }

    @PrePersist
    @PreUpdate
    private void normalize() {
        name = trim(name);
        description = trim(description);

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name không được để trống");
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    // ===== Getter / Setter =====

    public Long getMeterCategoryId() {
        return meterCategoryId;
    }

    public void setMeterCategoryId(Long meterCategoryId) {
        this.meterCategoryId = meterCategoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = trim(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = trim(description);
    }
}