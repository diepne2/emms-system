package com.emms.backend.dto.meter;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO rút gọn cho đồng hồ (Meter)")
public class MeterSummaryDTO {

    @Schema(description = "ID", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Tên đồng hồ")
    private String name;

    @Schema(description = "Đơn vị đo")
    private String unit;

    @Schema(description = "Tần suất cập nhật")
    private Integer updateFrequency;

    public MeterSummaryDTO() {
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = trim(unit);
    }

    public Integer getUpdateFrequency() {
        return updateFrequency;
    }

    public void setUpdateFrequency(Integer updateFrequency) {
        this.updateFrequency = updateFrequency;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}