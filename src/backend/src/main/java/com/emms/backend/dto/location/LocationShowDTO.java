package com.emms.backend.dto.location;

import com.emms.backend.dto.audit.AuditShowDTO;

public class LocationShowDTO extends AuditShowDTO {

    private Long id;
    private String name;
    private String address;
    private String parentLocation;

    private String vendors;
    private String contractors;

    public LocationShowDTO() {
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = trim(address);
    }

    public String getParentLocation() {
        return parentLocation;
    }

    public void setParentLocation(String parentLocation) {
        this.parentLocation = trim(parentLocation);
    }


    public String getVendors() {
        return vendors;
    }

    public void setVendors(String vendors) {
        this.vendors = trim(vendors);
    }

    public String getContractors() {
        return contractors;
    }

    public void setContractors(String contractors) {
        this.contractors = trim(contractors);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}