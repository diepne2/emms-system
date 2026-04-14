package com.emms.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "locations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_location_name", columnNames = "name")
        },
        indexes = {
                @Index(name = "idx_location_name", columnList = "name"),
                @Index(name = "idx_location_parent", columnList = "parent_location")
        }
)
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "parent_location", length = 255)
    private String parentLocation;

    @Column(name = "vendors", columnDefinition = "TEXT")
    private String vendors;

    @Column(name = "contractors", columnDefinition = "TEXT")
    private String contractors;

    public Location() {
    }

    @PrePersist
    @PreUpdate
    private void validateAndNormalize() {
        name = trimToNull(name);
        address = trimToNull(address);
        parentLocation = trimToNull(parentLocation);
        vendors = trimToNull(vendors);
        contractors = trimToNull(contractors);

        if (name == null) {
            throw new IllegalArgumentException("name không được để trống");
        }
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
        this.name = trimToNull(name);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = trimToNull(address);
    }

    public String getParentLocation() {
        return parentLocation;
    }

    public void setParentLocation(String parentLocation) {
        this.parentLocation = trimToNull(parentLocation);
    }

    public String getVendors() {
        return vendors;
    }

    public void setVendors(String vendors) {
        this.vendors = trimToNull(vendors);
    }

    public String getContractors() {
        return contractors;
    }

    public void setContractors(String contractors) {
        this.contractors = trimToNull(contractors);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}