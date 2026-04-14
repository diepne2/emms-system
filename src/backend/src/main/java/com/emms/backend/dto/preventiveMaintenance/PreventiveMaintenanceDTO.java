package com.emms.backend.dto.preventiveMaintenance;

import com.emms.backend.dto.workorderBase.WorkOrderBaseDTO;
import com.emms.backend.entity.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO dùng để cập nhật kế hoạch bảo trì định kỳ")
public class PreventiveMaintenanceDTO extends WorkOrderBaseDTO {

    @Schema(description = "Mã kế hoạch bảo trì")
    private String code;

    @Schema(description = "Trạng thái hoạt động")
    private Boolean active;

    @Schema(description = "Số giờ ước tính")
    private Double estimatedHours;

    @Schema(description = "ID thiết bị")
    private Long assetId;

    @Schema(description = "ID người được phân công")
    private Long assignedToId;

    @Schema(description = "Mức độ ưu tiên")
    private Priority priority;

    public PreventiveMaintenanceDTO() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = trim(code);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    @Override
    public Priority getPriority() {
        return priority;
    }

    @Override
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}