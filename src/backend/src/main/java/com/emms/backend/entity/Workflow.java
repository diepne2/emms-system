package com.emms.backend.entity;

import com.emms.backend.entity.enums.WFMainCondition;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workflows", indexes = {
        @Index(name = "idx_workflow_title", columnList = "title"),
        @Index(name = "idx_workflow_main_condition", columnList = "main_condition"),
        @Index(name = "idx_workflow_enabled", columnList = "enabled")
})
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "main_condition", nullable = false, length = 100)
    private WFMainCondition mainCondition;

    @OneToMany(
            mappedBy = "workflow",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id ASC")
    private List<WorkflowCondition> secondaryConditions = new ArrayList<>();

    @OneToOne(
            mappedBy = "workflow",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private WorkflowAction action;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Workflow() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        normalize();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        normalize();
    }

    private void normalize() {
        this.title = trim(this.title);

        if (this.title == null) {
            throw new IllegalArgumentException("title không được để trống");
        }

        if (this.mainCondition == null) {
            throw new IllegalArgumentException("mainCondition không được null");
        }

        if (this.secondaryConditions == null) {
            this.secondaryConditions = new ArrayList<>();
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public void addCondition(WorkflowCondition condition) {
        if (condition == null) {
            return;
        }
        this.secondaryConditions.add(condition);
        condition.setWorkflow(this);
    }

    public void removeCondition(WorkflowCondition condition) {
        if (condition == null) {
            return;
        }
        this.secondaryConditions.remove(condition);
        condition.setWorkflow(null);
    }

    public void setSecondaryConditions(List<WorkflowCondition> secondaryConditions) {
        this.secondaryConditions.clear();
        if (secondaryConditions != null) {
            for (WorkflowCondition condition : secondaryConditions) {
                addCondition(condition);
            }
        }
    }

    public void setAction(WorkflowAction action) {
        this.action = action;
        if (action != null) {
            action.setWorkflow(this);
        }
    }

    public boolean hasConditions() {
        return secondaryConditions != null && !secondaryConditions.isEmpty();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public WFMainCondition getMainCondition() {
        return mainCondition;
    }

    public List<WorkflowCondition> getSecondaryConditions() {
        return secondaryConditions;
    }

    public WorkflowAction getAction() {
        return action;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = trim(title);
    }

    public void setMainCondition(WFMainCondition mainCondition) {
        this.mainCondition = mainCondition;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}