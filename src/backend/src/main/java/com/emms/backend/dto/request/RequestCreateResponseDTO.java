package com.emms.backend.dto.request;

public class RequestCreateResponseDTO {

    private Long requestId;
    private String status;
    private Long workOrderId;
    private String message;

    public RequestCreateResponseDTO() {
    }

    public RequestCreateResponseDTO(Long requestId, String status, Long workOrderId, String message) {
        this.requestId = requestId;
        this.status = status;
        this.workOrderId = workOrderId;
        this.message = message;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}