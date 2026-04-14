package com.emms.backend.dto.workorder;

import com.emms.backend.entity.WorkOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkOrderSummaryDTO {

    private Long id;
    private String title;
    private LocalDate dueDate;
    private WorkOrder.WorkOrderStatus status;
    private LocalDateTime dateCreated;

    public WorkOrderSummaryDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = trim(title);
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public WorkOrder.WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrder.WorkOrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}