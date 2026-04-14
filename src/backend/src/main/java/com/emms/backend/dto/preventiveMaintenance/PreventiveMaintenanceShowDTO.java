package com.emms.backend.dto.preventiveMaintenance;

import com.emms.backend.dto.workorderBase.WorkOrderBaseShowDTO;
import com.emms.backend.entity.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO hiển thị kế hoạch bảo trì định kỳ")
public class PreventiveMaintenanceShowDTO extends WorkOrderBaseShowDTO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "Mã kế hoạch bảo trì")
    private String code;

    @Schema(description = "Trạng thái hoạt động")
    private boolean active;

    @Schema(description = "Thông tin lịch bảo trì")
    private Schedule schedule;

    public PreventiveMaintenanceShowDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = trim(code);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}