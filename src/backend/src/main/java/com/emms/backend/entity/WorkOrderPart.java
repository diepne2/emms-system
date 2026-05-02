package com.emms.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "work_order_parts",
    indexes = {
        @Index(name = "idx_wop_work_order", columnList = "work_order_id"),
        @Index(name = "idx_wop_part", columnList = "part_id")
    }
)
public class WorkOrderPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Column(name = "quantity_used", nullable = false)
    private Integer quantityUsed;

    @Column(name = "cost", precision = 15, scale = 2, nullable = false)
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    public WorkOrderPart() {
    }

    public WorkOrderPart(WorkOrder workOrder, Part part, Integer quantityUsed, BigDecimal cost) {
        this.workOrder = workOrder;
        this.part = part;
        this.quantityUsed = quantityUsed;
        this.cost = cost == null ? BigDecimal.ZERO : cost;
        this.usedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (usedAt == null) {
            usedAt = LocalDateTime.now();
        }
        if (cost == null) {
            cost = BigDecimal.ZERO;
        }
        validate();
    }

    @PreUpdate
    public void preUpdate() {
        if (cost == null) {
            cost = BigDecimal.ZERO;
        }
        validate();
    }

    private void validate() {
        if (workOrder == null) {
            throw new IllegalArgumentException("workOrder không được null");
        }
        if (part == null) {
            throw new IllegalArgumentException("part không được null");
        }
        if (quantityUsed == null || quantityUsed <= 0) {
            throw new IllegalArgumentException("quantityUsed phải > 0");
        }
        if (cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("cost không được âm");
        }
    }

    public Long getId() {
        return id;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public Integer getQuantityUsed() {
        return quantityUsed;
    }

    public void setQuantityUsed(Integer quantityUsed) {
        this.quantityUsed = quantityUsed;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost == null ? BigDecimal.ZERO : cost;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public BigDecimal getTotalCost() {
        return cost.multiply(BigDecimal.valueOf(quantityUsed));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkOrderPart)) return false;
        WorkOrderPart that = (WorkOrderPart) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "WorkOrderPart{" +
                "id=" + id +
                ", workOrderId=" + (workOrder != null ? workOrder.getId() : null) +
                ", partId=" + (part != null ? part.getId() : null) +
                ", quantityUsed=" + quantityUsed +
                ", cost=" + cost +
                ", usedAt=" + usedAt +
                '}';
    }
}