package com.emms.backend.dto.category;

public class CategorySummaryDTO {

    private Long id;
    private String name;


    public CategorySummaryDTO() {}

    public CategorySummaryDTO(Long id, String name) {
        this.id = id;
        this.name = trim(name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = trim(name);
    }


    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
