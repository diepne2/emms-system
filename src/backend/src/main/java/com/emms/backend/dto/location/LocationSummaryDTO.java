package com.emms.backend.dto.location;

public class LocationSummaryDTO {

    private Long id;
    private String name;
    private String address;
    private String parentLocation;
    private String description;

    public LocationSummaryDTO() {
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = trimToNull(description);
    }
}