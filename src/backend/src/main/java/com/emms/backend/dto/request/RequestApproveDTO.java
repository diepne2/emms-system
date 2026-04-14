package com.emms.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class RequestApproveDTO {

    @NotNull(message = "Trạng thái request không được null")
    private Boolean approved;

    public RequestApproveDTO() {
    }

    public RequestApproveDTO(Boolean approved) {
        this.approved = approved;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
}