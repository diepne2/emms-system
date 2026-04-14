package com.emms.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "request_portals", indexes = {
        @Index(name = "idx_request_portal_title", columnList = "title"),
        @Index(name = "idx_request_portal_uuid", columnList = "uuid")
})
public class RequestPortal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_portal_id")
    private Long requestPortalId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "welcome_message", length = 2000)
    private String welcomeMessage;

    @Column(name = "uuid", nullable = false, unique = true, length = 100)
    private String uuid;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "requestPortal", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, requestPortalFieldId ASC")
    private List<RequestPortalField> fields = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public RequestPortal() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        normalize();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        normalize();
    }

    private void normalize() {
        title = trim(title);
        welcomeMessage = trim(welcomeMessage);
        uuid = trim(uuid);

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title không được để trống");
        }

        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("uuid không được để trống");
        }

        if (fields == null) {
            fields = new ArrayList<>();
        }
    }

    public void addField(RequestPortalField field) {
        if (field == null) {
            return;
        }
        field.setRequestPortal(this);
        this.fields.add(field);
    }

    public void removeField(RequestPortalField field) {
        if (field == null) {
            return;
        }
        field.setRequestPortal(null);
        this.fields.remove(field);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    public Long getRequestPortalId() {
        return requestPortalId;
    }

    public void setRequestPortalId(Long requestPortalId) {
        this.requestPortalId = requestPortalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = trim(title);
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = trim(welcomeMessage);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = trim(uuid);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<RequestPortalField> getFields() {
        return fields;
    }

    public void setFields(List<RequestPortalField> fields) {
        this.fields = fields == null ? new ArrayList<>() : fields;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}