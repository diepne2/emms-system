package com.emms.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_task_work_order", columnList = "work_order_id"),
        @Index(name = "idx_task_pm", columnList = "preventive_maintenance_id"),
        @Index(name = "idx_task_task_base", columnList = "task_base_id"),
        @Index(name = "idx_task_status", columnList = "status")
})
public class Task {

    public enum TaskStatus {
        OPEN,
        ON_HOLD,
        IN_PROGRESS,
        COMPLETE,
        PASS,
        FLAG,
        FAIL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_base_id")
    private TaskBase taskBase;

    @Column(name = "label", nullable = false, length = 255)
    private String label;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "value", length = 255)
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TaskStatus status = TaskStatus.OPEN;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 1;

    @Column(name = "required_task", nullable = false)
    private boolean requiredTask = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preventive_maintenance_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private PreventiveMaintenance preventiveMaintenance;

    @Column(name = "completed_by", length = 100)
    private String completedBy;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("uploadedAt ASC")
    private List<File> files = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Task() {
    }

    public Task(TaskBase taskBase, WorkOrder workOrder, PreventiveMaintenance preventiveMaintenance, String value) {
        this.taskBase = taskBase;
        this.workOrder = workOrder;
        this.preventiveMaintenance = preventiveMaintenance;
        this.value = trim(value);
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        normalize();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        normalize();
    }

    private void normalize() {
        label = trim(label);
        notes = trim(notes);
        value = trim(value);
        completedBy = trim(completedBy);

        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("label không được để trống");
        }

        if (status == null) {
            status = TaskStatus.OPEN;
        }

        if (sortOrder == null || sortOrder < 1) {
            sortOrder = 1;
        }

        if (files == null) {
            files = new ArrayList<>();
        }

        if (workOrder == null && preventiveMaintenance == null) {
            throw new IllegalArgumentException("Task phải thuộc workOrder hoặc preventiveMaintenance");
        }
    }

    public void markInProgress() {
        this.status = TaskStatus.IN_PROGRESS;
    }

    public void markOnHold() {
        this.status = TaskStatus.ON_HOLD;
    }

    public void markComplete(String completedBy, String value, String notes) {
        this.status = TaskStatus.COMPLETE;
        this.completedBy = trim(completedBy);
        this.value = trim(value);
        this.notes = trim(notes);
        this.completedAt = LocalDateTime.now();
    }

    public void markPass(String completedBy, String notes) {
        this.status = TaskStatus.PASS;
        this.completedBy = trim(completedBy);
        this.notes = trim(notes);
        this.completedAt = LocalDateTime.now();
    }

    public void markFail(String completedBy, String notes) {
        this.status = TaskStatus.FAIL;
        this.completedBy = trim(completedBy);
        this.notes = trim(notes);
        this.completedAt = LocalDateTime.now();
    }

    public void markFlag(String completedBy, String notes) {
        this.status = TaskStatus.FLAG;
        this.completedBy = trim(completedBy);
        this.notes = trim(notes);
        this.completedAt = LocalDateTime.now();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public TaskBase getTaskBase() {
        return taskBase;
    }

    public void setTaskBase(TaskBase taskBase) {
        this.taskBase = taskBase;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = trim(label);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = trim(notes);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = trim(value);
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isRequiredTask() {
        return requiredTask;
    }

    public void setRequiredTask(boolean requiredTask) {
        this.requiredTask = requiredTask;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public PreventiveMaintenance getPreventiveMaintenance() {
        return preventiveMaintenance;
    }

    public void setPreventiveMaintenance(PreventiveMaintenance preventiveMaintenance) {
        this.preventiveMaintenance = preventiveMaintenance;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files != null ? files : new ArrayList<>();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}