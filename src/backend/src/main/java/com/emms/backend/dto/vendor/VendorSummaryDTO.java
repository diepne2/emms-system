package com.emms.backend.dto.vendor;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO rút gọn cho nhà cung cấp (bao gồm ID và tên công ty)")
public class VendorSummaryDTO {

    @Schema(description = "ID của nhà cung cấp")
    private Long id;

    @Schema(description = "Tên công ty của nhà cung cấp")
    private String companyName;

    // ===== Constructor =====
    public VendorSummaryDTO() {
    }

    // ===== Getter & Setter =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = trim(companyName);
    }

    // ===== Utils =====
    private String trim(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}