package com.emms.backend.dto.importData;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for importing meters from external data sources")
public class MeterImportDTO {

    @Schema(description = "Unique identifier")
    private Long id;

    @Schema(description = "Name")
    private String name;

    @Schema(description = "Unit of measurement")
    private String unit;

    @Schema(description = "Update frequency")
    private Integer updateFrequency;

    @Schema(description = "Meter category name")
    private String meterCategory;

    @Schema(description = "Location name")
    private String locationName;

    @Schema(description = "Asset name")
    private String assetName;

    @Schema(description = "List of user emails")
    @Builder.Default
    private List<String> usersEmails = new ArrayList<>();

    public void setName(String name) {
        this.name = trim(name);
    }

    public void setUnit(String unit) {
        this.unit = trim(unit);
    }

    public void setMeterCategory(String meterCategory) {
        this.meterCategory = trim(meterCategory);
    }

    public void setLocationName(String locationName) {
        this.locationName = trim(locationName);
    }

    public void setAssetName(String assetName) {
        this.assetName = trim(assetName);
    }

    public void setUsersEmails(List<String> usersEmails) {
        if (usersEmails == null) {
            this.usersEmails = new ArrayList<>();
            return;
        }

        List<String> normalized = new ArrayList<>();
        for (String email : usersEmails) {
            String value = trim(email);
            if (value != null) {
                normalized.add(value.toLowerCase());
            }
        }
        this.usersEmails = normalized;
    }

    public void validate() {
        if (name == null) {
            throw new IllegalArgumentException("Meter name must not be null");
        }

        if (updateFrequency != null && updateFrequency < 0) {
            throw new IllegalArgumentException("Update frequency must be greater than or equal to 0");
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}