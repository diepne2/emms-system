package com.emms.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "checklists", indexes = {
        @Index(name = "idx_checklist_name", columnList = "name"),
        @Index(name = "idx_checklist_active", columnList = "active"),
        @Index(name = "idx_checklist_created_at", columnList = "created_at")
})
public class Checklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Nếu muốn dùng checklist mẫu cho nhiều WO / kế hoạch bảo trì
     * thì để true = active, false = ngừng dùng
     */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /**
     * Có thể dùng cho:
     * - WORK_ORDER
     * - MAINTENANCE_PLAN
     * - ASSET
     * - GENERAL
     */
    @Column(name = "applies_to", length = 50)
    private String appliesTo;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "checklist",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("displayOrder ASC, checklistTaskId ASC")
    private List<ChecklistTask> tasks = new ArrayList<>();

    public Checklist() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        normalize();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        normalize();
    }

    private void normalize() {
        name = trim(name);
        description = trim(description);
        appliesTo = trim(appliesTo);
        createdBy = trim(createdBy);

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name không được để trống");
        }
    }

    public void addTask(ChecklistTask task) {
        if (task == null) {
            return;
        }
        task.setChecklist(this);
        this.tasks.add(task);
    }

    public void removeTask(ChecklistTask task) {
        if (task == null) {
            return;
        }
        task.setChecklist(null);
        this.tasks.remove(task);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(String appliesTo) {
        this.appliesTo = trim(appliesTo);
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = trim(createdBy);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<ChecklistTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<ChecklistTask> tasks) {
        this.tasks.clear();
        if (tasks != null) {
            for (ChecklistTask task : tasks) {
                addTask(task);
            }
        }
    }
}