package com.emms.backend.dto.workorder;
import com.emms.backend.entity.WorkOrder;

public class WorkOrderChangeStatusDTO {

    private WorkOrder.WorkOrderStatus status;
    private String feedback;

    public WorkOrderChangeStatusDTO() {
    }

    public WorkOrder.WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrder.WorkOrderStatus status) {
        this.status = status;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = trim(feedback);
    }

    private String trim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}