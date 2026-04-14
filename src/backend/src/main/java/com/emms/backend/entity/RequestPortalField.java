package com.emms.backend.entity;

import com.emms.backend.entity.enums.PortalFieldType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "request_portal_fields")
public class RequestPortalField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PortalFieldType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Column(nullable = false)
    private boolean required = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_portal_id")
    @JsonIgnore
    private RequestPortal requestPortal;

    public RequestPortalField() {
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (type == null) {
            throw new IllegalArgumentException("type không được null");
        }
        if (requestPortal == null) {
            throw new IllegalArgumentException("requestPortal không được null");
        }
    }

    // ===== getter/setter =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PortalFieldType getType() {
        return type;
    }

    public void setType(PortalFieldType type) {
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public RequestPortal getRequestPortal() {
        return requestPortal;
    }

    public void setRequestPortal(RequestPortal requestPortal) {
        this.requestPortal = requestPortal;
    }
}