package com.emms.backend.service;

import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.Reading;
import com.emms.backend.entity.WorkOrderMeterTrigger;
import com.emms.backend.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkOrderAutomationService {

    private final WorkOrderService workOrderService;

    public WorkOrderAutomationService(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    public Long createFromMeterTrigger(WorkOrderMeterTrigger trigger, Reading reading) {
        if (trigger == null || trigger.getId() == null) {
            throw new CustomException("Ngưỡng kích hoạt của đồng hồ đo không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (reading == null || reading.getId() == null) {
            throw new CustomException("Dữ liệu chỉ số đồng hồ đo không được để trống", HttpStatus.BAD_REQUEST);
        }

        WorkOrder created = workOrderService.createAutoFromMeterTrigger(trigger, reading);

        if (created == null || created.getId() == null) {
            throw new CustomException("Thất bại khi tạo đơn công việc từ ngưỡng đồng hồ đo", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return created.getId();
    }
}