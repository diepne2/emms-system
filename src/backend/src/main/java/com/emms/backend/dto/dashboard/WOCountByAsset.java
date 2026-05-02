package com.emms.backend.dto.dashboard;


public class WOCountByAsset {
    private Long id;
    private String name;
    private Integer totalCount;

    public WOCountByAsset(Long id, String name, Integer totalCount) {
        this.id = id;
        this.name = name;
        this.totalCount = totalCount;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getTotalCount() { return totalCount; }
}
