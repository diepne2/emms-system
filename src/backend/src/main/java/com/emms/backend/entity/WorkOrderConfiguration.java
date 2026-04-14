package com.emms.backend.entity;

import com.emms.backend.entity.enums.WorkOrderFieldKey;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "work_order_configurations")
@Schema(description = "Configuration for work order fields")
public class WorkOrderConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_code", nullable = false, unique = true, length = 100)
    private String configCode;

    @Column(name = "config_name", nullable = false, length = 255)
    private String configName;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @OneToMany(
            mappedBy = "workOrderConfiguration",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("displayOrder ASC")
    private Set<FieldConfiguration> workOrderFieldConfigurations = new LinkedHashSet<>();

    public WorkOrderConfiguration() {
        initDefaultFieldConfigurations();
    }

    public WorkOrderConfiguration(String configCode, String configName) {
        this.configCode = trim(configCode);
        this.configName = trim(configName);
        initDefaultFieldConfigurations();
    }

    @PrePersist
    @PreUpdate
    public void preSave() {
        this.configCode = trim(this.configCode);
        this.configName = trim(this.configName);

        if (this.configCode == null || this.configCode.isBlank()) {
            this.configCode = "DEFAULT";
        }
        if (this.configName == null || this.configName.isBlank()) {
            this.configName = "Cấu hình mặc định Work Order";
        }
        if (this.active == null) {
            this.active = Boolean.TRUE;
        }
        if (this.workOrderFieldConfigurations == null) {
            this.workOrderFieldConfigurations = new LinkedHashSet<>();
        }
    }

    public void initializeDefaultsIfNeeded() {
        if (this.workOrderFieldConfigurations == null) {
            this.workOrderFieldConfigurations = new LinkedHashSet<>();
        }
        if (this.workOrderFieldConfigurations.isEmpty()) {
            initDefaultFieldConfigurations();
        }
    }

    public void initDefaultFieldConfigurations() {
        if (this.workOrderFieldConfigurations == null) {
            this.workOrderFieldConfigurations = new LinkedHashSet<>();
        } else {
            this.workOrderFieldConfigurations.clear();
        }

        int order = 1;
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.DESCRIPTION, true, false, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.ASSET, true, true, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.PRIORITY, true, true, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.IMAGES, true, false, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.PRIMARY_USER, true, false, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.ASSIGNED_TO, true, false, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.TEAM, true, false, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.LOCATION, true, false, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.DUE_DATE, true, false, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.CATEGORY, true, false, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.PURCHASE_ORDER, false, false, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.FILES, true, false, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.SIGNATURE, false, false, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.COMPLETE_FILES, true, false, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.COMPLETE_TASKS, true, false, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.COMPLETE_TIME, true, false, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.COMPLETE_PARTS, true, false, order++));
        addFieldConfiguration(new FieldConfiguration(WorkOrderFieldKey.COMPLETE_COST, true, false, order++));
    }

    public void addFieldConfiguration(FieldConfiguration fieldConfiguration) {
        if (fieldConfiguration == null) {
            return;
        }
        fieldConfiguration.setWorkOrderConfiguration(this);
        this.workOrderFieldConfigurations.add(fieldConfiguration);
    }

    public void removeFieldConfiguration(FieldConfiguration fieldConfiguration) {
        if (fieldConfiguration == null) {
            return;
        }
        this.workOrderFieldConfigurations.remove(fieldConfiguration);
        fieldConfiguration.setWorkOrderConfiguration(null);
    }

    public FieldConfiguration findByFieldKey(WorkOrderFieldKey fieldKey) {
        if (fieldKey == null || this.workOrderFieldConfigurations == null) {
            return null;
        }

        for (FieldConfiguration item : this.workOrderFieldConfigurations) {
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

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    public Long getId() {
        return id;
    }

    public String getConfigCode() {
        return configCode;
    }

    public void setConfigCode(String configCode) {
        this.configCode = trim(configCode);
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = trim(configName);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<FieldConfiguration> getWorkOrderFieldConfigurations() {
        return workOrderFieldConfigurations;
    }

    public void setWorkOrderFieldConfigurations(Set<FieldConfiguration> workOrderFieldConfigurations) {
        this.workOrderFieldConfigurations.clear();
        if (workOrderFieldConfigurations != null) {
            for (FieldConfiguration item : workOrderFieldConfigurations) {
                addFieldConfiguration(item);
            }
        }
    }
}