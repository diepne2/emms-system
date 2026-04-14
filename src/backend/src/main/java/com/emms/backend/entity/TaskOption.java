package com.emms.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(
        name = "task_options",
        indexes = {
                @Index(name = "idx_task_option_task_base", columnList = "task_base_id"),
                @Index(name = "idx_task_option_label", columnList = "label")
        }
)
public class TaskOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_option_id")
    private Long taskOptionId;

    @Column(name = "label", nullable = false, length = 255)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_base_id", nullable = false)
    @JsonBackReference
    private TaskBase taskBase;

    public TaskOption() {
    }

    public TaskOption(String label, TaskBase taskBase) {
        this.label = normalize(label);
        this.taskBase = taskBase;
    }

    @PrePersist
    @PreUpdate
    protected void validateAndNormalize() {
        this.label = normalize(this.label);

        if (this.label == null) {
            throw new IllegalArgumentException("Task option label không được để trống");
        }

        if (this.label.length() > 255) {
            throw new IllegalArgumentException("Task option label không được vượt quá 255 ký tự");
        }

        if (this.taskBase == null) {
            throw new IllegalArgumentException("Task option phải thuộc một taskBase");
        }
    }

    protected String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public Long getTaskOptionId() {
        return taskOptionId;
    }

    public void setTaskOptionId(Long taskOptionId) {
        this.taskOptionId = taskOptionId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = normalize(label);
    }

    public TaskBase getTaskBase() {
        return taskBase;
    }

    public void setTaskBase(TaskBase taskBase) {
        this.taskBase = taskBase;
    }
}