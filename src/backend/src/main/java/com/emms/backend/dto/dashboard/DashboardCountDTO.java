package com.emms.backend.dto.dashboard;

public class DashboardCountDTO {

    private String name;
    private Long count;

    public DashboardCountDTO() {}

    public DashboardCountDTO(String name, Long count) {
        this.name = name;
        this.count = count;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
}