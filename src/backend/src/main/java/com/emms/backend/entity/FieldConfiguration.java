package com.emms.backend.entity;

import com.emms.backend.entity.enums.WorkOrderFieldKey;
import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(
        name = "field_configurations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_field_config_wo_key",
                        columnNames = {"work_order_configuration_id", "field_key"}
                ),
                @UniqueConstraint(
                        name = "uk_field_config_wor_key",
                        columnNames = {"work_order_request_configuration_id", "field_key"}
                )
        }
)
public class FieldConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_key", nullable = false, length = 50)
    private WorkOrderFieldKey fieldKey;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "required_field", nullable = false)
    private Boolean required = false;

    @Column(name = "display_order")
    private Integer displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_configuration_id")
    private WorkOrderConfiguration workOrderConfiguration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_request_configuration_id")
    private WorkOrderRequestConfiguration workOrderRequestConfiguration;

    public FieldConfiguration() {
    }

    public FieldConfiguration(
            WorkOrderFieldKey fieldKey,
            Boolean enabled,
            Boolean required,
            Integer displayOrder
    ) {
        this.fieldKey = fieldKey;
        this.enabled = enabled != null ? enabled : Boolean.TRUE;
        this.required = required != null ? required : Boolean.FALSE;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public WorkOrderFieldKey getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(WorkOrderFieldKey fieldKey) {
        this.fieldKey = fieldKey;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled != null ? enabled : Boolean.TRUE;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required != null ? required : Boolean.FALSE;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public WorkOrderConfiguration getWorkOrderConfiguration() {
        return workOrderConfiguration;
    }

    public void setWorkOrderConfiguration(WorkOrderConfiguration workOrderConfiguration) {
        this.workOrderConfiguration = workOrderConfiguration;
        if (workOrderConfiguration != null) {
            this.workOrderRequestConfiguration = null;
        }
    }

    public WorkOrderRequestConfiguration getWorkOrderRequestConfiguration() {
        return workOrderRequestConfiguration;
    }

    public void setWorkOrderRequestConfiguration(WorkOrderRequestConfiguration workOrderRequestConfiguration) {
        this.workOrderRequestConfiguration = workOrderRequestConfiguration;
        if (workOrderRequestConfiguration != null) {
            this.workOrderConfiguration = null;
        }
    }

    @PrePersist
    @PreUpdate
    public void preSave() {
        if (this.enabled == null) {
            this.enabled = Boolean.TRUE;
        }
        if (this.required == null) {
            this.required = Boolean.FALSE;
        }

        boolean hasWorkOrderConfig = this.workOrderConfiguration != null;
        boolean hasWorkOrderRequestConfig = this.workOrderRequestConfiguration != null;

        if (hasWorkOrderConfig == hasWorkOrderRequestConfig) {
            throw new IllegalStateException(
                    "FieldConfiguration must belong to exactly one parent: WorkOrderConfiguration or WorkOrderRequestConfiguration"
            );
        }

        if (this.fieldKey == null) {
            throw new IllegalStateException("fieldKey must not be null");
        }
    }

    public static Set<FieldConfiguration> createFieldConfigurations(
            Set<WorkOrderFieldKey> fieldKeys,
            WorkOrderConfiguration workOrderConfiguration,
            Set<WorkOrderFieldKey> requiredFields
    ) {
        Set<FieldConfiguration> result = new LinkedHashSet<>();
        if (fieldKeys == null || fieldKeys.isEmpty()) {
            return result;
        }

        int order = 1;
        for (WorkOrderFieldKey key : fieldKeys) {
            FieldConfiguration item = new FieldConfiguration();
            item.setFieldKey(key);
            item.setEnabled(Boolean.TRUE);
            item.setRequired(requiredFields != null && requiredFields.contains(key));
            item.setDisplayOrder(order++);
            item.setWorkOrderConfiguration(workOrderConfiguration);
            result.add(item);
        }
        return result;
    }

    public static Set<FieldConfiguration> createFieldConfigurations(
            Set<WorkOrderFieldKey> fieldKeys,
            WorkOrderRequestConfiguration workOrderRequestConfiguration,
            Set<WorkOrderFieldKey> requiredFields
    ) {
        Set<FieldConfiguration> result = new LinkedHashSet<>();
        if (fieldKeys == null || fieldKeys.isEmpty()) {
            return result;
        }

        int order = 1;
        for (WorkOrderFieldKey key : fieldKeys) {
            FieldConfiguration item = new FieldConfiguration();
            item.setFieldKey(key);
            item.setEnabled(Boolean.TRUE);
            item.setRequired(requiredFields != null && requiredFields.contains(key));
            item.setDisplayOrder(order++);
            item.setWorkOrderRequestConfiguration(workOrderRequestConfiguration);
            result.add(item);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldConfiguration that)) return false;

        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }

        return fieldKey == that.fieldKey
                && Objects.equals(
                        workOrderConfiguration != null ? workOrderConfiguration.getId() : null,
                        that.workOrderConfiguration != null ? that.workOrderConfiguration.getId() : null
                )
                && Objects.equals(
                        workOrderRequestConfiguration != null ? workOrderRequestConfiguration.getId() : null,
                        that.workOrderRequestConfiguration != null ? that.workOrderRequestConfiguration.getId() : null
                );
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(
                fieldKey,
                workOrderConfiguration != null ? workOrderConfiguration.getId() : null,
                workOrderRequestConfiguration != null ? workOrderRequestConfiguration.getId() : null
        );
    }
}