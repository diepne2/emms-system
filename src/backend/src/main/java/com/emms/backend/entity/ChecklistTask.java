package com.emms.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "checklist_tasks", indexes = {
        @Index(name = "idx_checklist_task_checklist", columnList = "checklist_id"),
        @Index(name = "idx_checklist_task_required", columnList = "required"),
        @Index(name = "idx_checklist_task_order", columnList = "display_order")
})
public class ChecklistTask {

    public enum ChecklistTaskType {
        PASS_FAIL,
        YES_NO,
        TEXT,
        NUMBER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checklist_task_id")
    private Long checklistTaskId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 30)
    private ChecklistTaskType taskType = ChecklistTaskType.PASS_FAIL;

    @Column(name = "required", nullable = false)
    private boolean required = true;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 1;

    @Column(name = "expected_value", length = 255)
    private String expectedValue;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ChecklistTask() {
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
        title = trim(title);
        description = trim(description);
        expectedValue = trim(expectedValue);

        if (checklist == null) {
            throw new IllegalArgumentException("checklist không được null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title không được để trống");
        }
        if (taskType == null) {
            taskType = ChecklistTaskType.PASS_FAIL;
        }
        if (displayOrder == null || displayOrder < 1) {
            displayOrder = 1;
        }
        if (minValue != null && maxValue != null && minValue > maxValue) {
            throw new IllegalArgumentException("minValue phải <= maxValue");
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    public Long getChecklistTaskId() {
        return checklistTaskId;
    }

    public void setChecklistTaskId(Long checklistTaskId) {
        this.checklistTaskId = checklistTaskId;
    }

    public Checklist getChecklist() {
        return checklist;
    }

    public void setChecklist(Checklist checklist) {
        this.checklist = checklist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = trim(title);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = trim(description);
    }

    public ChecklistTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(ChecklistTaskType taskType) {
        this.taskType = taskType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = trim(expectedValue);
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}