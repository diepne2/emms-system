package com.emms.backend.dto.requestPortal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO for patching an existing request portal")
public class RequestPortalPatchDTO {

    @NotBlank(message = "Title must not be blank")
    @Schema(description = "Title of the request portal")
    private String title;

    @Schema(description = "Welcome message displayed on the portal")
    private String welcomeMessage;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = trim(title);
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = trim(welcomeMessage);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}