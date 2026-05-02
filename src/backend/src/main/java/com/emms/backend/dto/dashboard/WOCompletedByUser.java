package com.emms.backend.dto.dashboard;

public class WOCompletedByUser {
    private Long id;
    private String username;
    private String fullName;
    private Integer completedCount;

    public WOCompletedByUser(Long id, String username, String fullName, Integer completedCount) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.completedCount = completedCount;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public Integer getCompletedCount() { return completedCount; }
}