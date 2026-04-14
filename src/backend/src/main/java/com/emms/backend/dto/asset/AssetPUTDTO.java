package com.emms.backend.dto.asset;

import com.emms.backend.entity.enums.AssetStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "DTO dùng để tạo / cập nhật thiết bị (Asset)")
public class AssetPUTDTO {

    @Schema(description = "Tên tài sản", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Mô tả tài sản")
    private String description;

    @Schema(description = "Trạng thái tài sản")
    private AssetStatus status;

    @Schema(description = "Khu vực")
    private String area;

    @Schema(description = "Tên tài sản cha")
    private String parentAssetName;

    @Schema(description = "Tên vị trí")
    private String locationName;

    @Schema(description = "Mã barcode")
    private String barcode;

    @Schema(description = "Danh mục tài sản")
    private String category;

    @Schema(description = "Người phụ trách")
    private String assignedTo;

    @Schema(description = "Ngày hết hạn bảo hành")
    private LocalDate warrantyExpiryDate;

    @Schema(description = "Thông tin bổ sung")
    private String additionalInfo;

    @Schema(description = "Số serial")
    private String serialNumber;

    @Schema(description = "Danh sách team")
    private String teamNames;

    @Schema(description = "Các vật liệu liên quan")
    private String associatedParts;

    @Schema(description = "Nhà cung cấp")
    private String vendor;

    @Schema(description = "Nhà thầu")
    private String contractor;

    public AssetPUTDTO() {}


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

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = trim(area);
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

    private String trim(String v) {
        return v == null ? null : v.trim();
    }
}