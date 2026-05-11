package com.emms.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory_count_items")
public class InventoryCountItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long inventoryCountId;

    private Long partId;

    private Integer systemQuantity;

    private Integer actualQuantity;

    private Integer differenceQuantity;

    private String note;

    public Long getId() {
        return id;
    }

    public Long getInventoryCountId() {
        return inventoryCountId;
    }

    public void setInventoryCountId(Long inventoryCountId) {
        this.inventoryCountId = inventoryCountId;
    }

    public Long getPartId() {
        return partId;
    }

    public void setPartId(Long partId) {
        this.partId = partId;
    }

    public Integer getSystemQuantity() {
        return systemQuantity;
    }

    public void setSystemQuantity(Integer systemQuantity) {
        this.systemQuantity = systemQuantity;
    }

    public Integer getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(Integer actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public Integer getDifferenceQuantity() {
        return differenceQuantity;
    }

    public void setDifferenceQuantity(Integer differenceQuantity) {
        this.differenceQuantity = differenceQuantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note == null ? null : note.trim();
    }
}