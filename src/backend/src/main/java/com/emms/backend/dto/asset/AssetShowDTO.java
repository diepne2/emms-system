package com.emms.backend.dto.asset;

import com.emms.backend.entity.enums.AssetStatus;

import java.time.LocalDate;

public class AssetShowDTO {

    private Long id;
    private String name;
    private String description;
    private String area;
    private AssetStatus status;
    private String parentAssetName;
    private String locationName;
    private String barcode;
    private String category;
    private String assignedTo;
    private LocalDate warrantyExpiryDate;
    private String additionalInfo;
    private String serialNumber;
    private String teamNames;
    private String associatedParts;
    private String vendor;
    private String contractor;

    public AssetShowDTO() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = trim(description);
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = trim(area);
    }

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
    }

    public String getParentAssetName() {
        return parentAssetName;
    }

    public void setParentAssetName(String parentAssetName) {
        this.parentAssetName = trim(parentAssetName);
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = trim(locationName);
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = trim(barcode);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = trim(category);
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = trim(assignedTo);
    }

    public LocalDate getWarrantyExpiryDate() {
        return warrantyExpiryDate;
    }

    public void setWarrantyExpiryDate(LocalDate warrantyExpiryDate) {
        this.warrantyExpiryDate = warrantyExpiryDate;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = trim(additionalInfo);
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = trim(serialNumber);
    }

    public String getTeamNames() {
        return teamNames;
    }

    public void setTeamNames(String teamNames) {
        this.teamNames = trim(teamNames);
    }

    public String getAssociatedParts() {
        return associatedParts;
    }

    public void setAssociatedParts(String associatedParts) {
        this.associatedParts = trim(associatedParts);
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = trim(vendor);
    }

    public String getContractor() {
        return contractor;
    }

    public void setContractor(String contractor) {
        this.contractor = trim(contractor);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}