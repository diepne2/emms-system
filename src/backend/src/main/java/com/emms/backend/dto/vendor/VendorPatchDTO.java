package com.emms.backend.dto.vendor;

import com.emms.backend.entity.abstracts.BasicInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO dùng để cập nhật thông tin nhà cung cấp (PATCH)")
public class VendorPatchDTO extends BasicInfo {

    @Schema(description = "Loại nhà cung cấp")
    private String vendorType;

    @Schema(description = "Mô tả nhà cung cấp")
    private String description;

    @Schema(description = "Đánh giá nhà cung cấp (rating)")
    private Integer rating;

    // ===== Constructor =====
    public VendorPatchDTO() {
    }

    // ===== Getter & Setter =====
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

    // ===== Utils =====
    protected String trim(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}