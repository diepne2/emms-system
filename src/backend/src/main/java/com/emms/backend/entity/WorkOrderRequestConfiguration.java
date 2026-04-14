package com.emms.backend.entity;

import com.emms.backend.entity.enums.WorkOrderFieldKey;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.emms.backend.entity.FieldConfiguration.createFieldConfigurations;

@Entity
@Schema(description = "Work order request configuration for customizing request fields")
public class WorkOrderRequestConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @OneToMany(
            mappedBy = "workOrderRequestConfiguration",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("displayOrder ASC")
    private Set<FieldConfiguration> fieldConfigurations = new LinkedHashSet<>();

    public WorkOrderRequestConfiguration() {
        initDefaultFieldConfigurations();
    }

    public void initDefaultFieldConfigurations() {
        this.fieldConfigurations.clear();

        Set<WorkOrderFieldKey> defaultFields = new LinkedHashSet<>();
        defaultFields.add(WorkOrderFieldKey.ASSET);
        defaultFields.add(WorkOrderFieldKey.LOCATION);
        defaultFields.add(WorkOrderFieldKey.PRIMARY_USER);
        defaultFields.add(WorkOrderFieldKey.DUE_DATE);
        defaultFields.add(WorkOrderFieldKey.CATEGORY);
        defaultFields.add(WorkOrderFieldKey.TEAM);

        Set<WorkOrderFieldKey> requiredFields = new HashSet<>();
        requiredFields.add(WorkOrderFieldKey.ASSET);

        Set<FieldConfiguration> defaults =
                createFieldConfigurations(defaultFields, this, requiredFields);

        for (FieldConfiguration fieldConfiguration : defaults) {
            addFieldConfiguration(fieldConfiguration);
        }
    }

    public void addFieldConfiguration(FieldConfiguration fieldConfiguration) {
        if (fieldConfiguration == null) {
            return;
        }
        fieldConfiguration.setWorkOrderRequestConfiguration(this);
        this.fieldConfigurations.add(fieldConfiguration);
    }

    public void removeFieldConfiguration(FieldConfiguration fieldConfiguration) {
        if (fieldConfiguration == null) {
            return;
        }
        this.fieldConfigurations.remove(fieldConfiguration);
        fieldConfiguration.setWorkOrderRequestConfiguration(null);
    }

    public void setFieldConfigurations(Set<FieldConfiguration> fieldConfigurations) {
        this.fieldConfigurations.clear();

        if (fieldConfigurations == null) {
            return;
        }

        for (FieldConfiguration fieldConfiguration : fieldConfigurations) {
            addFieldConfiguration(fieldConfiguration);
        }
    }

    public FieldConfiguration findByFieldKey(WorkOrderFieldKey fieldKey) {
        if (fieldKey == null || this.fieldConfigurations == null) {
            return null;
        }

        for (FieldConfiguration item : this.fieldConfigurations) {
            if (fieldKey.equals(item.getFieldKey())) {
                return item;
            }
        }
        return null;
    }

    public void updateField(WorkOrderFieldKey fieldKey, Boolean enabled, Boolean required, Integer displayOrder) {
        if (fieldKey == null) {
            return;
        }

        FieldConfiguration fieldConfiguration = findByFieldKey(fieldKey);
        if (fieldConfiguration == null) {
            fieldConfiguration = new FieldConfiguration();
            fieldConfiguration.setFieldKey(fieldKey);
            fieldConfiguration.setEnabled(Boolean.TRUE);
            fieldConfiguration.setRequired(Boolean.FALSE);
            addFieldConfiguration(fieldConfiguration);
        }

        if (enabled != null) {
            fieldConfiguration.setEnabled(enabled);
        }
        if (required != null) {
            fieldConfiguration.setRequired(required);
        }
        if (displayOrder != null) {
            fieldConfiguration.setDisplayOrder(displayOrder);
        }
    }

    public Long getId() {
        return id;
    }

    public Set<FieldConfiguration> getFieldConfigurations() {
        return fieldConfigurations;
    }
}