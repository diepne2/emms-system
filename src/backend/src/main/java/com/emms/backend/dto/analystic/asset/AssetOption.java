package com.emms.backend.dto.analystic.asset;

public class AssetOption {
    private Long id;
    private String name;
    private String status;

    public AssetOption(Long id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
}