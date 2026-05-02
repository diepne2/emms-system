package com.emms.backend.dto.workorder;

public class WorkOrderOptionDTO {

    private Long id;
    private String code;
    private String title;
    private String status;

    public WorkOrderOptionDTO() {
    }

    public WorkOrderOptionDTO(Long id, String code, String title, String status) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}