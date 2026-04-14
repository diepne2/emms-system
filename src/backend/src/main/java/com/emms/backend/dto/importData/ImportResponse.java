package com.emms.backend.dto.importData;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing import operation results")
public class ImportResponse {

    @Schema(description = "Total records processed")
    private int total;

    @Schema(description = "Number of records created")
    private int created;

    @Schema(description = "Number of records updated")
    private int updated;

    @Schema(description = "Number of records failed")
    private int failed;

    @Schema(description = "Error details for failed records")
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @Schema(description = "Optional message")
    private String message;
}
