package com.emms.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lightweight DTO for user information")
public class UserSummaryDTO {

    @Schema(description = "User ID")
    private Long id;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "First name")
    private String firstName;

    @Schema(description = "Last name")
    private String lastName;

    @Schema(description = "Full name")
    private String fullName;

    public UserSummaryDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = trim(username);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = trim(firstName);
        rebuildFullNameIfNeeded();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = trim(lastName);
        rebuildFullNameIfNeeded();
    }

    public String getFullName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }

        String generated = buildFullName(firstName, lastName);
        return generated != null ? generated : username;
    }

    public void setFullName(String fullName) {
        this.fullName = trim(fullName);
    }

    private void rebuildFullNameIfNeeded() {
        if (this.fullName == null || this.fullName.isBlank()) {
            this.fullName = buildFullName(this.firstName, this.lastName);
        }
    }

    private String buildFullName(String firstName, String lastName) {
        String first = trim(firstName);
        String last = trim(lastName);

        if (first == null && last == null) {
            return null;
        }

        if (first == null) {
            return last;
        }

        if (last == null) {
            return first;
        }

        return first + " " + last;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}