package com.emms.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UserInvitationRequestDTO {

    @NotEmpty(message = "Danh sách email không được để trống")
    private List<@Email(message = "Email không hợp lệ") String> emails = new ArrayList<>();

    @NotNull(message = "roleId không được null")
    private Long roleId;

    public UserInvitationRequestDTO() {
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails == null ? new ArrayList<>() : emails;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}