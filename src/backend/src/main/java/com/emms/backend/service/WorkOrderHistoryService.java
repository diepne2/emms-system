package com.emms.backend.service;

import com.emms.backend.dto.wo_history.WorkOrderHistoryShowDTO;
import com.emms.backend.entity.User;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.WorkOrderHistory;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.WorkOrderHistoryMapper;
import com.emms.backend.repository.WorkOrderHistoryRepository;
import com.emms.backend.repository.WorkOrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WorkOrderHistoryService {

    private final WorkOrderHistoryRepository workOrderHistoryRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderHistoryMapper workOrderHistoryMapper;
    private final ObjectMapper objectMapper;

    public WorkOrderHistoryService(
            WorkOrderHistoryRepository workOrderHistoryRepository,
            WorkOrderRepository workOrderRepository,
            WorkOrderHistoryMapper workOrderHistoryMapper,
            ObjectMapper objectMapper
    ) {
        this.workOrderHistoryRepository = workOrderHistoryRepository;
        this.workOrderRepository = workOrderRepository;
        this.workOrderHistoryMapper = workOrderHistoryMapper;
        this.objectMapper = objectMapper;
    }

    public WorkOrderHistory create(WorkOrderHistory workOrderHistory) {
        if (workOrderHistory == null) {
            throw new CustomException("Lịch sử đơn công việc không được để trống", HttpStatus.BAD_REQUEST);
        }

        validateWorkOrderHistory(workOrderHistory);
        normalize(workOrderHistory);

        return workOrderHistoryRepository.save(workOrderHistory);
    }

    public WorkOrderHistory update(WorkOrderHistory workOrderHistory) {
        if (workOrderHistory == null || workOrderHistory.getId() == null) {
            throw new CustomException("ID của lịch sử đơn công việc là bắt buộc", HttpStatus.BAD_REQUEST);
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

    @Transactional(readOnly = true)
    public List<WorkOrderHistoryShowDTO> getHistoryByWorkOrder(Long workOrderId) {
        WorkOrder workOrder = findWorkOrderById(workOrderId);

        return workOrderHistoryRepository.findByWorkOrderOrderByVersionNoDescCreatedAtDesc(workOrder)
                .stream()
                .map(workOrderHistoryMapper::toShowDto)
                .toList();
    }

    public void delete(Long id) {
        WorkOrderHistory existing = findEntityById(id);
        workOrderHistoryRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public Optional<WorkOrderHistory> findById(Long id) {
        if (id == null) {
            throw new CustomException("ID của lịch sử đơn công việc là bắt buộc", HttpStatus.BAD_REQUEST);
        }
        return workOrderHistoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public WorkOrderHistory findEntityById(Long id) {
        if (id == null) {
            throw new CustomException("ID của lịch sử đơn công việc là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        return workOrderHistoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("Lịch sử đơn công việc không tìm thấy", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Collection<WorkOrderHistory> findByWorkOrder(Long workOrderId) {
        WorkOrder workOrder = findWorkOrderById(workOrderId);
        return workOrderHistoryRepository.findByWorkOrderOrderByVersionNoDescCreatedAtDesc(workOrder);
    }

    public WorkOrderHistory saveSnapshot(WorkOrder workOrder, User savedBy, String versionName, String note) {
        if (workOrder == null || workOrder.getId() == null) {
            throw new CustomException("Đơn công việc là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        if (savedBy == null || savedBy.getUserId() == null) {
            throw new CustomException("Người lưu là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        Integer nextVersionNo = workOrderHistoryRepository
                .findTopByWorkOrderOrderByVersionNoDesc(workOrder)
                .map(WorkOrderHistory::getVersionNo)
                .map(v -> v + 1)
                .orElse(1);

        WorkOrderHistory history = new WorkOrderHistory();
        history.setWorkOrder(workOrder);
        history.setSavedBy(savedBy);
        history.setVersionNo(nextVersionNo);
        history.setVersionName(buildVersionName(workOrder, versionName, nextVersionNo));
        history.setNote(note);
        history.setSnapshotJson(buildSnapshotJson(workOrder));

        validateWorkOrderHistory(history);
        normalize(history);

        return workOrderHistoryRepository.save(history);
    }

    public void saveSystemHistory(Long workOrderId, String actionName, String note) {
        if (workOrderId == null) {
            throw new CustomException("ID của đơn công việc là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        WorkOrder workOrder = findWorkOrderById(workOrderId);

        User savedBy = null;
        try {
            savedBy = workOrder.getAssignedTo();
        } catch (Exception ignored) {
        }

        if (savedBy == null || savedBy.getUserId() == null) {
            return;
        }

        saveSnapshot(workOrder, savedBy, actionName, note);
    }

    private WorkOrder findWorkOrderById(Long workOrderId) {
        if (workOrderId == null) {
            throw new CustomException("ID của đơn công việc là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        return workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new CustomException("Đơn công việc không tìm thấy", HttpStatus.NOT_FOUND));
    }

    private String buildVersionName(WorkOrder workOrder, String versionName, Integer versionNo) {
        String code = workOrder.getId() == null ? "WO-UNKNOWN" : "WO-" + workOrder.getId();
        String action = versionName == null || versionName.isBlank()
                ? "STATUS_CHANGED"
                : versionName.trim();

        return code + " - v" + versionNo + " - " + action;
    }

    private String buildSnapshotJson(WorkOrder workOrder) {
        try {
            Snapshot snapshot = new Snapshot(
                    workOrder.getId(),
                    workOrder.getTitle(),
                    workOrder.getDescription(),
                    workOrder.getStatus() == null ? null : workOrder.getStatus().name(),
                    workOrder.getPriority() == null ? null : workOrder.getPriority().name(),
                    workOrder.getArchived(),
                    workOrder.getDueDate(),
                    workOrder.getCompletedOn(),
                    workOrder.getCompletedBy(),
                    workOrder.getFeedback(),
                    workOrder.getAsset() != null ? workOrder.getAsset().getId() : null,
                    workOrder.getAssetName(),
                    workOrder.getAssignedTo() != null ? workOrder.getAssignedTo().getUserId() : null,
                    workOrder.getAssignedTo() != null ? workOrder.getAssignedTo().getFullName() : null
            );
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new CustomException("Không thể tạo snapshot work order history", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateWorkOrderHistory(WorkOrderHistory workOrderHistory) {
        if (workOrderHistory.getVersionName() == null || workOrderHistory.getVersionName().trim().isEmpty()) {
            throw new CustomException("Tên phiên bản là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        if (workOrderHistory.getSavedBy() == null || workOrderHistory.getSavedBy().getUserId() == null) {
            throw new CustomException("Người lưu là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        if (workOrderHistory.getWorkOrder() == null || workOrderHistory.getWorkOrder().getId() == null) {
            throw new CustomException("Đơn công việc là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        if (workOrderHistory.getVersionNo() != null && workOrderHistory.getVersionNo() < 1) {
            throw new CustomException("Số phiên bản phải lớn hơn 0", HttpStatus.BAD_REQUEST);
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

    @Transactional(readOnly = true)
    public List<WorkOrderHistoryShowDTO> getDoneAndCancelledHistories() {
        List<WorkOrder.WorkOrderStatus> statuses = List.of(
                WorkOrder.WorkOrderStatus.DONE,
                WorkOrder.WorkOrderStatus.CANCELLED
        );

        return workOrderHistoryRepository
                .findByWorkOrder_StatusInOrderByCreatedAtDesc(statuses)
                .stream()
                .map(workOrderHistoryMapper::toShowDto)
                .toList();
    }

    private static class Snapshot {
        private final Long id;
        private final String title;
        private final String description;
        private final String status;
        private final String priority;
        private final Boolean archived;
        private final Object dueDate;
        private final Object completedOn;
        private final String completedBy;
        private final String feedback;
        private final Long assetId;
        private final String assetName;
        private final Long assignedToId;
        private final String assignedToName;

        public Snapshot(Long id,
                        String title,
                        String description,
                        String status,
                        String priority,
                        Boolean archived,
                        Object dueDate,
                        Object completedOn,
                        String completedBy,
                        String feedback,
                        Long assetId,
                        String assetName,
                        Long assignedToId,
                        String assignedToName) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.status = status;
            this.priority = priority;
            this.archived = archived;
            this.dueDate = dueDate;
            this.completedOn = completedOn;
            this.completedBy = completedBy;
            this.feedback = feedback;
            this.assetId = assetId;
            this.assetName = assetName;
            this.assignedToId = assignedToId;
            this.assignedToName = assignedToName;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
        public String getPriority() { return priority; }
        public Boolean getArchived() { return archived; }
        public Object getDueDate() { return dueDate; }
        public Object getCompletedOn() { return completedOn; }
        public String getCompletedBy() { return completedBy; }
        public String getFeedback() { return feedback; }
        public Long getAssetId() { return assetId; }
        public String getAssetName() { return assetName; }
        public Long getAssignedToId() { return assignedToId; }
        public String getAssignedToName() { return assignedToName; }
    }
}