package com.emms.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_work_order", columnList = "work_order_id"),
        @Index(name = "idx_comment_user", columnList = "user_id"),
        @Index(name = "idx_comment_created_at", columnList = "created_at")
})
public class Comment {

    private static final Pattern MENTION_PATTERN =
            Pattern.compile("@\\[.*?\\]\\(user:(\\d+)\\)");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        normalize();
    }

    @PreUpdate
    protected void onUpdate() {
        normalize();
    }

    private void normalize() {
        this.content = trim(this.content);
        if (this.content == null || this.content.isBlank()) {
            throw new IllegalArgumentException("content không được để trống");
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    public Set<Long> extractTaggedUserIds() {
        Set<Long> ids = new HashSet<>();

        if (content == null || content.isBlank()) {
            return ids;
        }

        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            ids.add(Long.parseLong(matcher.group(1)));
        }

        return ids;
    }

    public Long getId() {
        return id;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = trim(content);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}