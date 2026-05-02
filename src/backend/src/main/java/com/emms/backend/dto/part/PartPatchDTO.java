package com.emms.backend.dto.part;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO dùng để cập nhật một phần thông tin vật tư (Part)")
public class PartPatchDTO {

    private String name;


    private BigDecimal cost;

    private String category;

    @Schema(description = "Có phải vật tư tiêu hao hay không")
    private Boolean consumable;

    @Schema(description = "Mã part")
    private String partNumber;

    @Schema(description = "Mã barcode")
    private String barcode;

    @Schema(description = "Mô tả chi tiết")
    private String description;

    @Schema(description = "Giá nhập gần nhất")
    private BigDecimal lastPrice;

    private String assetName;

    private String locationName;

    @Schema(description = "Số lượng tồn kho")
    private Integer quantity;

    @Schema(description = "Người được gán")
    private String assignedTo;

    @Schema(description = "Nhà cung cấp")
    private String vendor;

    public PartPatchDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = trim(name);
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = trim(category);
    }

    public Boolean getConsumable() {
        return consumable;
    }

    public void setConsumable(Boolean consumable) {
        this.consumable = consumable;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = trim(partNumber);
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = trim(barcode);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = trim(description);
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = trim(assetName);
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = trim(locationName);
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = trim(assignedTo);
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = trim(vendor);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}