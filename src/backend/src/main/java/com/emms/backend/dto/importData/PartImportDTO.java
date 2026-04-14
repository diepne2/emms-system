package com.emms.backend.dto.importData;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for importing parts from external data sources")
public class PartImportDTO {

    @Schema(description = "Unique identifier")
    private Long id;

    @Schema(description = "Name")
    private String name;

    @Schema(description = "Cost")
    private BigDecimal cost;

    @Schema(description = "Category name")
    private String category;

    @Schema(description = "Whether this is a non-stock part")
    private Boolean nonStock;

    @Schema(description = "Barcode identifier")
    private String barcode;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Quantity in stock")
    private BigDecimal quantity;

    @Schema(description = "Additional information")
    private String additionalInfos;

    @Schema(description = "Storage area")
    private String area;

    @Schema(description = "Minimum quantity threshold")
    private BigDecimal minQuantity;

    @Schema(description = "Location name")
    private String locationName;

    @Schema(description = "List of assigned user emails")
    @Builder.Default
    private List<String> assignedToEmails = new ArrayList<>();

    @Schema(description = "List of team names")
    @Builder.Default
    private List<String> teamsNames = new ArrayList<>();

    @Schema(description = "List of customer names")
    @Builder.Default
    private List<String> customersNames = new ArrayList<>();

    @Schema(description = "List of vendor names")
    @Builder.Default
    private List<String> vendorsNames = new ArrayList<>();

    public void setName(String name) {
        this.name = trim(name);
    }

    public void setCategory(String category) {
        this.category = trim(category);
    }

    public void setBarcode(String barcode) {
        this.barcode = trim(barcode);
    }

    public void setDescription(String description) {
        this.description = trim(description);
    }

    public void setAdditionalInfos(String additionalInfos) {
        this.additionalInfos = trim(additionalInfos);
    }

    public void setArea(String area) {
        this.area = trim(area);
    }

    public void setLocationName(String locationName) {
        this.locationName = trim(locationName);
    }

    public void setAssignedToEmails(List<String> assignedToEmails) {
        this.assignedToEmails = normalizeStringList(assignedToEmails, true);
    }

    public void setTeamsNames(List<String> teamsNames) {
        this.teamsNames = normalizeStringList(teamsNames, false);
    }

    public void setCustomersNames(List<String> customersNames) {
        this.customersNames = normalizeStringList(customersNames, false);
    }

    public void setVendorsNames(List<String> vendorsNames) {
        this.vendorsNames = normalizeStringList(vendorsNames, false);
    }

    public void validate() {
        if (name == null) {
            throw new IllegalArgumentException("Part name must not be null");
        }

        if (cost != null && cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cost must be greater than or equal to 0");
        }

        if (quantity != null && quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantity must be greater than or equal to 0");
        }

        if (minQuantity != null && minQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Min quantity must be greater than or equal to 0");
        }
    }

    private List<String> normalizeStringList(List<String> values, boolean lowercase) {
        List<String> result = new ArrayList<>();
        if (values == null) {
            return result;
        }

        for (String value : values) {
            String trimmed = trim(value);
            if (trimmed != null) {
                result.add(lowercase ? trimmed.toLowerCase() : trimmed);
            }
        }
        return result;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}