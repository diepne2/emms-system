package com.emms.backend.dto.location;

public class LocationDTO {

    private String name;
    private String address;
    private String parentLocation;
    private String vendors;
    private String contractors;

    public LocationDTO() {
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