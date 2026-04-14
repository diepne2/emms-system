package com.emms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meters", indexes = {
        @Index(name = "idx_meter_name", columnList = "name"),
        @Index(name = "idx_meter_asset", columnList = "asset_id"),
        @Index(name = "idx_meter_location", columnList = "location_id"),
        @Index(name = "idx_meter_category", columnList = "meter_category_id")
})
public class Meter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "update_frequency", nullable = false)
    private Integer updateFrequency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_category_id")
    private MeterCategory meterCategory;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_file_id")
    private AssetDocument image;

    @Column(name = "demo", nullable = false)
    private boolean demo = false;

    @ManyToMany
    @JoinTable(
            name = "meter_user_assignments",
            joinColumns = @JoinColumn(name = "meter_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            indexes = {
                    @Index(name = "idx_meter_user_meter_id", columnList = "meter_id"),
                    @Index(name = "idx_meter_user_user_id", columnList = "user_id")
            }
    )
    @JsonIgnore
    private List<User> users = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Meter() {
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
        name = trim(name);
        unit = trim(unit);

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name không được để trống");
        }

        if (asset == null) {
            throw new IllegalArgumentException("asset không được null");
        }

        if (updateFrequency == null || updateFrequency < 1) {
            throw new IllegalArgumentException("updateFrequency phải >= 1");
        }

        if (users == null) {
            users = new ArrayList<>();
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    public boolean hasUsers() {
        return users != null && !users.isEmpty();
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = trim(unit);
    }

    public Integer getUpdateFrequency() {
        return updateFrequency;
    }

    public void setUpdateFrequency(Integer updateFrequency) {
        this.updateFrequency = updateFrequency;
    }

    public MeterCategory getMeterCategory() {
        return meterCategory;
    }

    public void setMeterCategory(MeterCategory meterCategory) {
        this.meterCategory = meterCategory;
    }

    public AssetDocument getImage() {
        return image;
    }

    public void setImage(AssetDocument image) {
        this.image = image;
    }

    public boolean isDemo() {
        return demo;
    }

    public void setDemo(boolean demo) {
        this.demo = demo;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users == null ? new ArrayList<>() : users;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}