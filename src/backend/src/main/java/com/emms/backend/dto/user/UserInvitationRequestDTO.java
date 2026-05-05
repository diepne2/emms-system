package com.emms.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

public class UserInvitationRequestDTO {

    @NotEmpty(message = "Danh sách email không được để trống")
    private List<
        @Email(message = "Email không hợp lệ")
        @NotBlank(message = "Email không được để trống")
        String
    > emails = new ArrayList<>();


    @NotBlank(message = "Vai trò không được để trống")
    private String roleName;

    public UserInvitationRequestDTO() {
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails == null ? new ArrayList<>() : emails;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}