package com.emms.backend.service;

import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.WorkOrderHistory;
import com.emms.backend.exception.CustomException;
import com.emms.backend.repository.WorkOrderHistoryRepository;
import com.emms.backend.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderHistoryService {

    private final WorkOrderHistoryRepository workOrderHistoryRepository;
    private final WorkOrderRepository workOrderRepository;

    public WorkOrderHistory create(WorkOrderHistory workOrderHistory) {
        if (workOrderHistory == null) {
            throw new CustomException("WorkOrderHistory is required", HttpStatus.BAD_REQUEST);
        }

        validateWorkOrderHistory(workOrderHistory);
        normalize(workOrderHistory);

        return workOrderHistoryRepository.save(workOrderHistory);
    }

    public WorkOrderHistory update(WorkOrderHistory workOrderHistory) {
        if (workOrderHistory == null || workOrderHistory.getId() == null) {
            throw new CustomException("WorkOrderHistory id is required", HttpStatus.BAD_REQUEST);
        }

        WorkOrderHistory existing = findEntityById(workOrderHistory.getId());

        if (workOrderHistory.getVersionName() != null) {
            existing.setVersionName(workOrderHistory.getVersionName());
        }

        if (workOrderHistory.getVersionNo() != null) {
            existing.setVersionNo(workOrderHistory.getVersionNo());
        }

        if (workOrderHistory.getNote() != null) {
            existing.setNote(workOrderHistory.getNote());
        }

        if (workOrderHistory.getSnapshotJson() != null) {
            existing.setSnapshotJson(workOrderHistory.getSnapshotJson());
        }

        if (workOrderHistory.getSavedBy() != null) {
            existing.setSavedBy(workOrderHistory.getSavedBy());
        }

        if (workOrderHistory.getWorkOrder() != null) {
            existing.setWorkOrder(workOrderHistory.getWorkOrder());
        }

        validateWorkOrderHistory(existing);
        normalize(existing);

        return workOrderHistoryRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public Collection<WorkOrderHistory> getAll() {
        return workOrderHistoryRepository.findAll();
    }

    public void delete(Long id) {
        WorkOrderHistory existing = findEntityById(id);
        workOrderHistoryRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public Optional<WorkOrderHistory> findById(Long id) {
        if (id == null) {
            throw new CustomException("WorkOrderHistory id is required", HttpStatus.BAD_REQUEST);
        }
        return workOrderHistoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public WorkOrderHistory findEntityById(Long id) {
        if (id == null) {
            throw new CustomException("WorkOrderHistory id is required", HttpStatus.BAD_REQUEST);
        }

        return workOrderHistoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("WorkOrderHistory not found", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Collection<WorkOrderHistory> findByWorkOrder(Long workOrderId) {
        if (workOrderId == null) {
            throw new CustomException("WorkOrder id is required", HttpStatus.BAD_REQUEST);
        }

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new CustomException("WorkOrder not found", HttpStatus.NOT_FOUND));

        return workOrderHistoryRepository.findByWorkOrder(workOrder);
    }

    private void validateWorkOrderHistory(WorkOrderHistory workOrderHistory) {
        if (workOrderHistory.getVersionName() == null || workOrderHistory.getVersionName().trim().isEmpty()) {
            throw new CustomException("Version name is required", HttpStatus.BAD_REQUEST);
        }

        if (workOrderHistory.getSavedBy() == null || workOrderHistory.getSavedBy().getUserId() == null) {
            throw new CustomException("SavedBy is required", HttpStatus.BAD_REQUEST);
        }

        if (workOrderHistory.getWorkOrder() == null || workOrderHistory.getWorkOrder().getId() == null) {
            throw new CustomException("WorkOrder is required", HttpStatus.BAD_REQUEST);
        }

        if (workOrderHistory.getVersionNo() != null && workOrderHistory.getVersionNo() < 1) {
            throw new CustomException("Version number must be greater than 0", HttpStatus.BAD_REQUEST);
        }
    }

    private void normalize(WorkOrderHistory workOrderHistory) {
        if (workOrderHistory.getVersionName() != null) {
            workOrderHistory.setVersionName(workOrderHistory.getVersionName().trim());
        }

        if (workOrderHistory.getNote() != null) {
            workOrderHistory.setNote(workOrderHistory.getNote().trim());
        }

        if (workOrderHistory.getSnapshotJson() != null) {
            workOrderHistory.setSnapshotJson(workOrderHistory.getSnapshotJson().trim());
        }

        if (workOrderHistory.getVersionNo() == null || workOrderHistory.getVersionNo() < 1) {
            workOrderHistory.setVersionNo(1);
        }
    }
}