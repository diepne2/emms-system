package com.emms.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "work_order_categories")
public class WorkOrderCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    public WorkOrderCategory() {
    }

    public WorkOrderCategory(String name) {
        this.name = name == null ? null : name.trim();
    }

    public WorkOrderCategory(String name, String description) {
        this.name = name == null ? null : name.trim();
        this.description = description == null ? null : description.trim();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    @Override
    public String toString() {
        return "WorkOrderCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}