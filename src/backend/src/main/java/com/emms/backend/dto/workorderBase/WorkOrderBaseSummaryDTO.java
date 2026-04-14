package com.emms.backend.dto.workorderBase;
import com.emms.backend.entity.WorkOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkOrderBaseSummaryDTO {

    private Long workOrderId;
    private String title;
    private LocalDate dueDate;
    private LocalDateTime dateCreated;
    private WorkOrder.WorkOrderPriority priority;
    private WorkOrder.WorkOrderStatus status;

    public WorkOrderBaseSummaryDTO() {
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
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

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public WorkOrder.WorkOrderPriority getPriority() {
        return priority;
    }

    public void setPriority(WorkOrder.WorkOrderPriority priority) {
        this.priority = priority;
    }

    public WorkOrder.WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrder.WorkOrderStatus status) {
        this.status = status;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}