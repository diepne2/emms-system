package com.emms.backend.dto.importData;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for importing locations from external data sources")
public class LocationImportDTO {

    private Long id;

    @Schema(description = "Name")
    private String name;

    @Schema(description = "Address")
    private String address;

    @Schema(description = "Longitude coordinate (-180 to 180)")
    private Double longitude;

    @Schema(description = "Latitude coordinate (-90 to 90)")
    private Double latitude;

    @Schema(description = "Parent location name")
    private String parentLocationName;

    @Builder.Default
    private List<String> workersEmails = new ArrayList<>();

    @Builder.Default
    private List<String> teamsNames = new ArrayList<>();

    @Builder.Default
    private List<String> customersNames = new ArrayList<>();

    @Builder.Default
    private List<String> vendorsNames = new ArrayList<>();

    // ===== normalize =====
    public void setName(String name) {
        this.name = trim(name);
    }

    public void setAddress(String address) {
        this.address = trim(address);
    }

    public void setParentLocationName(String parentLocationName) {
        this.parentLocationName = trim(parentLocationName);
    }

    // ===== validate =====
    public void validate() {
        if (name == null) {
            throw new IllegalArgumentException("Location name must not be null");
        }

        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }

        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }

    private String trim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}