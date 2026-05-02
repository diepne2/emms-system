package com.emms.backend.dto.part;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WorkOrderPartShowDTO {

    private Long id;
    private Long workOrderId;
    private Long partId;

    private String partName;
    private String partNumber;
    private String barcode;
    private String category;
    private Integer currentStock;

    private Integer quantityUsed;
    private BigDecimal unitCost;
    private BigDecimal lineTotal;

    private LocalDateTime usedAt;

    public WorkOrderPartShowDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public Long getPartId() {
        return partId;
    }

    public void setPartId(Long partId) {
        this.partId = partId;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = trim(partName);
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = trim(category);
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getQuantityUsed() {
        return quantityUsed;
    }

    public void setQuantityUsed(Integer quantityUsed) {
        this.quantityUsed = quantityUsed;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}