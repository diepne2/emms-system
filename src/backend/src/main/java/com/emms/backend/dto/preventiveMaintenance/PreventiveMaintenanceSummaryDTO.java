package com.emms.backend.dto.preventiveMaintenance;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary DTO for preventive maintenance")
public class PreventiveMaintenanceSummaryDTO {

    @Schema(description = "ID", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Mã kế hoạch bảo trì")
    private String code;

    @Schema(description = "Tiêu đề kế hoạch")
    private String title;

    @Schema(description = "Trạng thái hoạt động")
    private Boolean active;

    public PreventiveMaintenanceSummaryDTO() {
    }

    public PreventiveMaintenanceSummaryDTO(Long id, String code, String title, Boolean active) {
        this.id = id;
        this.code = trim(code);
        this.title = trim(title);
        this.active = active;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = trim(title);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}