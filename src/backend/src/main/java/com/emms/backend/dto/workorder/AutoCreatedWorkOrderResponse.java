package com.emms.backend.dto.workorder;
public class AutoCreatedWorkOrderResponse {

    private Long id;

    public AutoCreatedWorkOrderResponse() {
    }

    public AutoCreatedWorkOrderResponse(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
