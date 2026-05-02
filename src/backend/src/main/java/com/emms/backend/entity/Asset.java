package com.emms.backend.entity;

import com.emms.backend.entity.enums.AssetStatus;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private AssetStatus status;

    @Column(name = "Name")
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "Area")
    private String area;

    @Column(name = "parent_asset")
    private String parentAssetName;

    

    @Column(name = "location_name",nullable = false, unique = true)
    private String locationName;

    @Column(name = "Barcode",nullable = false, unique = true)
    private String barcode;

    @Column(name = "Category")
    private String category;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "warranty_expiry_date")
    private LocalDate warrantyExpiryDate;

    @Column(name = "additional_info")
    private String additionalInfo;

    @Column(name = "serial_number",nullable = false, unique = true)
    private String serialNumber;

    @Column(name = "teams_names")
    private String teamNames;

    @Column(name = "associated_parts")
    private String associatedParts;

    @Column(name = "Vendor")
    private String vendor;

    @Column(name = "Contractor")
    private String contractor;

    public Asset() {}


    private String trim(String v) { return v == null ? null : v.trim(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AssetStatus getStatus() { return status; }
    public void setStatus(AssetStatus status) { this.status = status; }

    public String getName() { return name; }
    public void setName(String name) { this.name = trim(name); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = trim(description); }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = trim(area); }

    public String getParentAssetName() { return parentAssetName; }
    public void setParentAssetName(String parentAssetName) { this.parentAssetName = trim(parentAssetName); }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = trim(locationName); }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = trim(barcode); }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = trim(category); }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = trim(assignedTo); }

    public LocalDate getWarrantyExpiryDate() { return warrantyExpiryDate; }
    public void setWarrantyExpiryDate(LocalDate warrantyExpiryDate) { this.warrantyExpiryDate = warrantyExpiryDate; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = trim(additionalInfo); }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = trim(serialNumber); }

    public String getTeamNames() { return teamNames; }
    public void setTeamNames(String teamNames) { this.teamNames = trim(teamNames); }

    public String getAssociatedParts() { return associatedParts; }
    public void setAssociatedParts(String associatedParts) { this.associatedParts = trim(associatedParts); }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = trim(vendor); }

    public String getContractor() { return contractor; }
    public void setContractor(String contractor) { this.contractor = trim(contractor); }
}