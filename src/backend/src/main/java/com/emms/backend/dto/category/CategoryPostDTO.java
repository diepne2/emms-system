package com.emms.backend.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO for creating a new category")
public class CategoryPostDTO {

    @Schema(description = "Name")
    @NotBlank(message = "Name must not be blank")
    @Size(max = 255, message = "Name must be <= 255 characters")
    private String name;

    @Schema(description = "Description")
    @Size(max = 1000, message = "Description must be <= 1000 characters")
    private String description;

    public CategoryPostDTO() {
    }

    public CategoryPostDTO(String name, String description) {
        this.name = trim(name);
        this.description = trim(description);
    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = trim(name);
    }

    public void setDescription(String description) {
        this.description = trim(description);
    }


    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}