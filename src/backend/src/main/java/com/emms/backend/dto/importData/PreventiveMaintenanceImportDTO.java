package com.emms.backend.dto.importData;

import com.emms.backend.entity.enums.RecurrenceBasedOn;
import com.emms.backend.entity.enums.RecurrenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "DTO for importing preventive maintenance schedules from external data sources")
public class PreventiveMaintenanceImportDTO extends WorkOrderImportDTO {

    @Schema(description = "Start date as epoch milliseconds")
    private Long startsOn;

    @Schema(description = "Name")
    @NotBlank
    private String name;

    @Schema(description = "Frequency of recurrence")
    @NotNull
    private Integer frequency;

    @Schema(description = "Due date delay")
    private Integer dueDateDelay;

    @Schema(description = "End date as epoch milliseconds")
    private Long endsOn;

    @Schema(description = "Recurrence type")
    @NotNull
    private RecurrenceType recurrenceType;

    @Schema(description = "What the recurrence is based on")
    @NotNull
    private RecurrenceBasedOn recurrenceBasedOn;

    @lombok.Builder.Default
    @Schema(description = "Days of week for recurrence")
    private List<String> daysOfWeek = new ArrayList<>();

    public void setName(String name) {
        this.name = trim(name);
    }

    public void setDaysOfWeek(List<String> daysOfWeek) {
        if (daysOfWeek == null) {
            this.daysOfWeek = new ArrayList<>();
            return;
        }

        List<String> normalized = new ArrayList<>();
        for (String day : daysOfWeek) {
            String value = trim(day);
            if (value != null) {
                normalized.add(value.toUpperCase());
            }
        }
        this.daysOfWeek = normalized;
    }

    public void validate() {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be blank");
        }

        if (frequency == null || frequency <= 0) {
            throw new IllegalArgumentException("Frequency must be greater than 0");
        }

        if (dueDateDelay != null && dueDateDelay < 0) {
            throw new IllegalArgumentException("Due date delay must be greater than or equal to 0");
        }

        if (startsOn != null && endsOn != null && startsOn > endsOn) {
            throw new IllegalArgumentException("Start date must not be after end date");
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