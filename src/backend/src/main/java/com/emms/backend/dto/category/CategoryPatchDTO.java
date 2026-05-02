package com.emms.backend.dto.category;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for patching an existing category")
public class CategoryPatchDTO {

    @Schema(description = "Name")
    private String name;

    @Schema(description = "Description")
    private String description;

    public CategoryPatchDTO() {
    }

    public CategoryPatchDTO(String name, String description) {
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