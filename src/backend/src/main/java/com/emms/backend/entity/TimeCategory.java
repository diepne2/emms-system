package com.emms.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "time_categories")
public class TimeCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "time_category_id")
    private Long timeCategoryId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "hourly_rate", nullable = false)
    private Double hourlyRate = 0.0;

    public TimeCategory() {
    }

    public Long getTimeCategoryId() {
        return timeCategoryId;
    }

    public void setTimeCategoryId(Long timeCategoryId) {
        this.timeCategoryId = timeCategoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate == null ? 0.0 : hourlyRate;
    }
}