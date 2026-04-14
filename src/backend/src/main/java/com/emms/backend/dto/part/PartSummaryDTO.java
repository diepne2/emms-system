package com.emms.backend.dto.part;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO rút gọn cho vật tư (Part)")
public class PartSummaryDTO {

    @Schema(description = "ID duy nhất", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Tên vật tư")
    private String name;

    @Schema(description = "Mô tả")
    private String description;

    @Schema(description = "Giá tiền")
    private BigDecimal cost;

    @Schema(description = "Có phải vật tư tiêu hao hay không")
    private Boolean consumable;

    public PartSummaryDTO() {
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

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public Boolean getConsumable() {
        return consumable;
    }

    public void setConsumable(Boolean consumable) {
        this.consumable = consumable;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}