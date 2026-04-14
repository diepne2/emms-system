package com.emms.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "floor_plans", indexes = {
        @Index(name = "idx_floor_plan_location", columnList = "location_id"),
        @Index(name = "idx_floor_plan_name", columnList = "name")
})
public class FloorPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "floor_plan_id")
    private Long floorPlanId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private File image;

    @Column(name = "area")
    private Long area;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    public FloorPlan() {
    }

    @PrePersist
    @PreUpdate
    protected void validateAndNormalize() {
        this.name = normalize(this.name);

        if (this.name == null) {
            throw new IllegalArgumentException("Tên sơ đồ mặt bằng không được để trống");
        }

        if (this.name.length() > 255) {
            throw new IllegalArgumentException("Tên sơ đồ mặt bằng không được vượt quá 255 ký tự");
        }

        if (this.area != null && this.area < 0) {
            throw new IllegalArgumentException("Diện tích không được âm");
        }

        if (this.location == null) {
            throw new IllegalArgumentException("FloorPlan phải thuộc một Location");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public Long getFloorPlanId() {
        return floorPlanId;
    }

    public void setFloorPlanId(Long floorPlanId) {
        this.floorPlanId = floorPlanId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalize(name);
    }

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }

    public Long getArea() {
        return area;
    }

    public void setArea(Long area) {
        this.area = area;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}