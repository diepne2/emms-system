package com.emms.backend.dto.user;

public class UserProfileUpdateDTO {

    private String firstName;
    private String lastName;
    private String phone;
    private String jobTitle;

    public UserProfileUpdateDTO() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = trim(firstName);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = trim(lastName);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = trim(phone);
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = trim(jobTitle);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}