package com.emms.backend.dto.uiconfig;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO dùng để cập nhật (PATCH) cấu hình giao diện")
public class UiConfigurationPatchDTO {

    @Schema(description = "Bật/tắt module yêu cầu (Requests)")
    private Boolean requests;

    @Schema(description = "Bật/tắt module vị trí (Locations)")
    private Boolean locations;

    @Schema(description = "Bật/tắt module đồng hồ đo (Meters)")
    private Boolean meters;

    @Schema(description = "Bật/tắt module nhà cung cấp & khách hàng")
    private Boolean vendorsAndCustomers;

    public UiConfigurationPatchDTO() {
    }

    public Boolean getRequests() {
        return requests;
    }

    public void setRequests(Boolean requests) {
        this.requests = requests;
    }

    public Boolean getLocations() {
        return locations;
    }

    public void setLocations(Boolean locations) {
        this.locations = locations;
    }

    public Boolean getMeters() {
        return meters;
    }

    public void setMeters(Boolean meters) {
        this.meters = meters;
    }

    public Boolean getVendorsAndCustomers() {
        return vendorsAndCustomers;
    }

    public void setVendorsAndCustomers(Boolean vendorsAndCustomers) {
        this.vendorsAndCustomers = vendorsAndCustomers;
    }
}