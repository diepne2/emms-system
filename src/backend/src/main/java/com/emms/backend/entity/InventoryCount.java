package com.emms.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import com.emms.backend.entity.enums.InventoryCountStatus;

@Entity
@Table(
    name = "inventory_counts",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_inventory_count_period",
            columnNames = {"year", "month"}
        )
    }
)
public class InventoryCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private Integer year;

    private Integer month;

    @Enumerated(EnumType.STRING)
    private InventoryCountStatus status;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime confirmedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by")
    private User confirmedBy;

    @OneToMany(mappedBy = "inventoryCount")
    private List<InventoryCountItem> items;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = InventoryCountStatus.DRAFT;
        }
    }
}