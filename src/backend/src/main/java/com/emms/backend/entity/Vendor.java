package com.emms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendors")
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_code", unique = true, length = 50)
    private String vendorCode;

    @NotBlank
    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "contact_person", length = 150)
    private String contactPerson;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "vendor_type", length = 100)
    private String vendorType;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinTable(
            name = "vendor_assets",
            joinColumns = @JoinColumn(name = "vendor_id"),
            inverseJoinColumns = @JoinColumn(name = "asset_id")
    )
    private List<Asset> assets = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinTable(
            name = "vendor_parts",
            joinColumns = @JoinColumn(name = "vendor_id"),
            inverseJoinColumns = @JoinColumn(name = "part_id")
    )
    private List<Part> parts = new ArrayList<>();

    public Vendor() {
    }

    public Vendor(String companyName) {
        this.companyName = trim(companyName);
    }

    public Vendor(String vendorCode,
                  String companyName,
                  String contactPerson,
                  String phone,
                  String email,
                  String address,
                  String vendorType,
                  String description,
                  Integer rating,
                  boolean active) {
        this.vendorCode = trim(vendorCode);
        this.companyName = trim(companyName);
        this.contactPerson = trim(contactPerson);
        this.phone = trim(phone);
        this.email = trim(email);
        this.address = trim(address);
        this.vendorType = trim(vendorType);
        this.description = trim(description);
        this.rating = rating;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = trim(vendorCode);
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = trim(companyName);
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = trim(contactPerson);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = trim(phone);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = trim(email);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = trim(address);
    }

    public String getVendorType() {
        return vendorType;
    }

    public void setVendorType(String vendorType) {
        this.vendorType = trim(vendorType);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = trim(description);
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets != null ? assets : new ArrayList<>();
    }

    public List<Part> getParts() {
        return parts;
    }

    public void setParts(List<Part> parts) {
        this.parts = parts != null ? parts : new ArrayList<>();
    }

    public void addAsset(Asset asset) {
        if (asset != null && !this.assets.contains(asset)) {
            this.assets.add(asset);
        }
    }

    public void removeAsset(Asset asset) {
        this.assets.remove(asset);
    }

    public void addPart(Part part) {
        if (part != null && !this.parts.contains(part)) {
            this.parts.add(part);
        }
    }

    public void removePart(Part part) {
        this.parts.remove(part);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    @Override
    public String toString() {
        return "Vendor{" +
                "id=" + id +
                ", vendorCode='" + vendorCode + '\'' +
                ", companyName='" + companyName + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", vendorType='" + vendorType + '\'' +
                ", rating=" + rating +
                ", active=" + active +
                '}';
    }
}