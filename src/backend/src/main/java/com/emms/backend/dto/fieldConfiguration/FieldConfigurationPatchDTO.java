package com.emms.backend.dto.fieldConfiguration;

import com.emms.backend.entity.enums.WorkOrderFieldKey;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for patching field configuration")
public class FieldConfigurationPatchDTO {

    @Schema(description = "Field key")
    private WorkOrderFieldKey fieldKey;

    @Schema(description = "Is field enabled")
    private Boolean enabled;

    @Schema(description = "Is field required")
    private Boolean required;

    @Schema(description = "Display order")
    private Integer displayOrder;

    public FieldConfigurationPatchDTO() {
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
        this.enabled = enabled;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}