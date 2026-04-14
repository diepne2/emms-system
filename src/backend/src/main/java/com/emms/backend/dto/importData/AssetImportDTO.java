package com.emms.backend.dto.importData;

import com.emms.backend.entity.enums.AssetStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for importing assets from external data sources")
public class AssetImportDTO {

    private Long id;

    private Boolean archived;

    private String description;

    private String locationName;

    private String parentAssetName;

    private String area;

    private String barCode;

    private String category;

    private String name;

    private String primaryUserEmail;

    private LocalDate warrantyExpirationDate;

    @Builder.Default
    private Map<String, Object> additionalInfos = new LinkedHashMap<>();

    private String serialNumber;

    @Builder.Default
    private List<String> assignedToEmails = new ArrayList<>();

    @Builder.Default
    private List<String> teamsNames = new ArrayList<>();

    private AssetStatus status;

    private BigDecimal acquisitionCost;

    @Builder.Default
    private List<String> customersNames = new ArrayList<>();

    @Builder.Default
    private List<String> vendorsNames = new ArrayList<>();

    @Builder.Default
    private List<String> partsNames = new ArrayList<>();

    private String model;

    private String manufacturer;

    private String power;

    // ===== normalize =====

    public void setDescription(String description) {
        this.description = trim(description);
    }

    public void setLocationName(String locationName) {
        this.locationName = trim(locationName);
    }

    public void setParentAssetName(String parentAssetName) {
        this.parentAssetName = trim(parentAssetName);
    }

    public void setArea(String area) {
        this.area = trim(area);
    }

    public void setBarCode(String barCode) {
        this.barCode = trim(barCode);
    }

    public void setCategory(String category) {
        this.category = trim(category);
    }

    public void setName(String name) {
        this.name = trim(name);
    }

    public void setPrimaryUserEmail(String primaryUserEmail) {
        this.primaryUserEmail = normalizeEmail(primaryUserEmail);
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = trim(serialNumber);
    }

    public void setModel(String model) {
        this.model = trim(model);
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = trim(manufacturer);
    }

    public void setPower(String power) {
        this.power = trim(power);
    }

    public void setAssignedToEmails(List<String> assignedToEmails) {
        this.assignedToEmails = normalizeList(assignedToEmails, true);
    }

    public void setTeamsNames(List<String> teamsNames) {
        this.teamsNames = normalizeList(teamsNames, false);
    }

    public void setCustomersNames(List<String> customersNames) {
        this.customersNames = normalizeList(customersNames, false);
    }

    public void setVendorsNames(List<String> vendorsNames) {
        this.vendorsNames = normalizeList(vendorsNames, false);
    }

    public void setPartsNames(List<String> partsNames) {
        this.partsNames = normalizeList(partsNames, false);
    }

    public void setAdditionalInfos(Map<String, Object> additionalInfos) {
        if (additionalInfos == null) {
            this.additionalInfos = new LinkedHashMap<>();
            return;
        }

        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : additionalInfos.entrySet()) {
            String key = trim(entry.getKey());
            Object value = entry.getValue();
            if (key != null && value != null) {
                normalized.put(key, value);
            }
        }
        this.additionalInfos = normalized;
    }

    // ===== validate =====

    public void validate() {
        if (name == null) {
            throw new IllegalArgumentException("Asset name must not be blank");
        }

        if (acquisitionCost != null && acquisitionCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Acquisition cost must be greater than or equal to 0");
        }
    }

    // ===== helper =====

    private List<String> normalizeList(List<String> values, boolean lowercase) {
        List<String> result = new ArrayList<>();
        if (values == null) {
            return result;
        }

        for (String value : values) {
            String normalized = lowercase ? normalizeEmail(value) : trim(value);
            if (normalized != null) {
                result.add(normalized);
            }
        }
        return result;
    }

    private String normalizeEmail(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}