package com.emms.backend.service;

import com.emms.backend.dto.importData.WorkOrderImportDTO;
import com.emms.backend.dto.workorder.WorkOrderDTO;
import com.emms.backend.dto.workorder.WorkOrderPostDTO;
import com.emms.backend.dto.workorder.WorkOrderShowDTO;
import com.emms.backend.entity.Asset;
import com.emms.backend.entity.PreventiveMaintenance;
import com.emms.backend.entity.Schedule;
import com.emms.backend.entity.User;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.WorkOrderMapper;
import com.emms.backend.repository.WorkOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final AssetService assetService;
    private final UserService userService;
    private final WorkOrderMapper workOrderMapper;

    public WorkOrderService(
            WorkOrderRepository workOrderRepository,
            AssetService assetService,
            UserService userService,
            WorkOrderMapper workOrderMapper
    ) {
        this.workOrderRepository = workOrderRepository;
        this.assetService = assetService;
        this.userService = userService;
        this.workOrderMapper = workOrderMapper;
    }

    public WorkOrderShowDTO create(WorkOrderPostDTO dto) {
        validateCreate(dto);

        WorkOrder entity = workOrderMapper.fromPostDto(dto);

        if (dto.getAssignedToId() != null) {
            User assignedUser = userService.findEntityById(dto.getAssignedToId());
            entity.setAssignedTo(assignedUser);
        }

        if (dto.getAssetId() != null) {
            Asset asset = assetService.findEntityById(dto.getAssetId());
            entity.setAsset(asset);
            entity.setAssetName(asset.getName());
        }

        if (entity.getStatus() == null) {
            entity.setStatus(WorkOrder.WorkOrderStatus.OPEN);
        }
        if (entity.getArchived() == null) {
            entity.setArchived(false);
        }
        if (entity.getTotalCost() == null) {
            entity.setTotalCost(BigDecimal.ZERO);
        }

        WorkOrder saved = workOrderRepository.save(entity);
        return workOrderMapper.toShowDto(saved);
    }

    public WorkOrderShowDTO update(Long id, WorkOrderDTO dto) {
        validateUpdate(id, dto);

        WorkOrder existing = findEntityById(id);
        workOrderMapper.updateWorkOrder(existing, dto);

        if (dto.getAssignedToId() != null) {
            User assignedUser = userService.findEntityById(dto.getAssignedToId());
            existing.setAssignedTo(assignedUser);
        }

        if (dto.getCompletedBy() != null && !dto.getCompletedBy().isBlank()) {
            existing.setCompletedBy(dto.getCompletedBy().trim());
        }

        if (dto.getCompletedOn() != null) {
            existing.setCompletedOn(dto.getCompletedOn());
        }

        if (dto.getArchived() != null) {
            existing.setArchived(dto.getArchived());
        }

        WorkOrder saved = workOrderRepository.save(existing);
        return workOrderMapper.toShowDto(saved);
    }

    @Transactional(readOnly = true)
    public WorkOrderShowDTO getById(Long id) {
        return workOrderMapper.toShowDto(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<WorkOrderShowDTO> getAll() {
        return workOrderRepository.findAll()
                .stream()
                .map(workOrderMapper::toShowDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkOrder findEntityById(Long id) {
        if (id == null) {
            throw new CustomException("Work order id must not be null", HttpStatus.BAD_REQUEST);
        }

        return workOrderRepository.findById(id)
                .orElseThrow(() -> new CustomException("Work order not found", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<WorkOrder> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return workOrderRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public WorkOrder checkAccessToWorkOrderId(Long id, User currentUser) {
        if (id == null) {
            throw new CustomException("Work order id must not be null", HttpStatus.BAD_REQUEST);
        }

        if (currentUser == null) {
            throw new CustomException("Current user must not be null", HttpStatus.UNAUTHORIZED);
        }

        WorkOrder workOrder = findEntityById(id);

        if (hasAdminOrManagerRole(currentUser)) {
            return workOrder;
        }

        if (workOrder.getAssignedTo() != null
                && currentUser.getUserId() != null
                && workOrder.getAssignedTo().getUserId() != null
                && workOrder.getAssignedTo().getUserId().equals(currentUser.getUserId())) {
            return workOrder;
        }

        if (workOrder.getCompletedBy() != null
                && currentUser.getFullName() != null
                && workOrder.getCompletedBy().trim().equalsIgnoreCase(currentUser.getFullName().trim())) {
            return workOrder;
        }

        throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
    }

    public void delete(Long id) {
        WorkOrder existing = findEntityById(id);
        workOrderRepository.delete(existing);
    }

    public WorkOrderShowDTO markCompleted(Long id, Long completedByUserId, String feedback) {
        if (completedByUserId == null) {
            throw new CustomException("Completed by user id must not be null", HttpStatus.BAD_REQUEST);
        }

        WorkOrder existing = findEntityById(id);
        User completedBy = userService.findEntityById(completedByUserId);

        existing.setCompletedBy(completedBy.getFullName());
        existing.setCompletedOn(LocalDateTime.now());
        existing.setStatus(WorkOrder.WorkOrderStatus.DONE);

        if (feedback != null && !feedback.isBlank()) {
            existing.setFeedback(feedback.trim());
        }

        WorkOrder saved = workOrderRepository.save(existing);
        return workOrderMapper.toShowDto(saved);
    }

    public WorkOrderShowDTO changeStatus(Long id, WorkOrder.WorkOrderStatus status, String feedback) {
        if (status == null) {
            throw new CustomException("Status must not be null", HttpStatus.BAD_REQUEST);
        }

        WorkOrder existing = findEntityById(id);
        existing.setStatus(status);

        if (feedback != null && !feedback.isBlank()) {
            existing.setFeedback(feedback.trim());
        }

        WorkOrder saved = workOrderRepository.save(existing);
        return workOrderMapper.toShowDto(saved);
    }

    public WorkOrderShowDTO archive(Long id, boolean archived) {
        WorkOrder existing = findEntityById(id);
        existing.setArchived(archived);

        WorkOrder saved = workOrderRepository.save(existing);
        return workOrderMapper.toShowDto(saved);
    }

    public List<WorkOrder> saveAll(List<WorkOrder> items) {
        if (items == null || items.isEmpty()) {
            throw new CustomException("Work order list must not be empty", HttpStatus.BAD_REQUEST);
        }
        return workOrderRepository.saveAll(items);
    }

    public void importWorkOrder(WorkOrder entity, WorkOrderImportDTO dto) {
        if (entity == null) {
            throw new CustomException("Work order entity must not be null", HttpStatus.BAD_REQUEST);
        }
        if (dto == null) {
            throw new CustomException("Work order import dto must not be null", HttpStatus.BAD_REQUEST);
        }

        dto.validate();

        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setFeedback(dto.getFeedback());
        entity.setArchived(dto.getArchived() != null ? dto.getArchived() : false);

        if (dto.getEstimatedDuration() != null) {
            entity.setEstimatedDuration(dto.getEstimatedDuration());
        }

        if (dto.getDueDate() != null) {
            entity.setDueDate(
                    Instant.ofEpochMilli(dto.getDueDate())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
            );
        } else {
            entity.setDueDate(null);
        }

        if (dto.getCompletedOn() != null) {
            entity.setCompletedOn(
                    Instant.ofEpochMilli(dto.getCompletedOn())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
            );
        } else {
            entity.setCompletedOn(null);
        }

        if (dto.getAssignedToEmails() != null && !dto.getAssignedToEmails().isEmpty()) {
            String firstEmail = dto.getAssignedToEmails().get(0);

            User assignedUser = userService.findAll().stream()
                    .filter(u -> u.getEmail() != null
                            && u.getEmail().trim().equalsIgnoreCase(firstEmail.trim()))
                    .findFirst()
                    .orElse(null);

            entity.setAssignedTo(assignedUser);
        } else {
            entity.setAssignedTo(null);
        }

        if (dto.getCompletedByEmail() != null && !dto.getCompletedByEmail().isBlank()) {
            User completedBy = userService.findAll().stream()
                    .filter(u -> u.getEmail() != null
                            && u.getEmail().trim().equalsIgnoreCase(dto.getCompletedByEmail().trim()))
                    .findFirst()
                    .orElse(null);

            if (completedBy != null && completedBy.getFullName() != null) {
                entity.setCompletedBy(completedBy.getFullName().trim());
            } else {
                entity.setCompletedBy(null);
            }
        } else {
            entity.setCompletedBy(null);
        }

        if (dto.getAssetName() != null && !dto.getAssetName().isBlank()) {
            entity.setAssetName(dto.getAssetName().trim());
        } else {
            entity.setAssetName(null);
        }

        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                entity.setStatus(WorkOrder.WorkOrderStatus.valueOf(dto.getStatus().trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new CustomException("Invalid work order status: " + dto.getStatus(), HttpStatus.BAD_REQUEST);
            }
        } else if (entity.getStatus() == null) {
            entity.setStatus(WorkOrder.WorkOrderStatus.OPEN);
        }
    }

    @Transactional
    public WorkOrder createFromPreventiveMaintenance(PreventiveMaintenance pm, Schedule schedule) {
        if (pm == null) {
            throw new CustomException("Preventive maintenance must not be null", HttpStatus.BAD_REQUEST);
        }

        if (pm.getTitle() == null || pm.getTitle().isBlank()) {
            throw new CustomException("Preventive maintenance title must not be blank", HttpStatus.BAD_REQUEST);
        }

        WorkOrder wo = new WorkOrder();
        wo.setTitle(pm.getTitle());
        wo.setDescription(pm.getDescription());
        wo.setStatus(WorkOrder.WorkOrderStatus.OPEN);
        wo.setArchived(false);

        if (pm.getEstimatedHours() != null) {
            if (pm.getEstimatedHours() < 0) {
                throw new CustomException("Estimated hours must be >= 0", HttpStatus.BAD_REQUEST);
            }
            wo.setEstimatedDuration(pm.getEstimatedHours());
        }

        if (pm.getAssignedTo() != null) {
            wo.setAssignedTo(pm.getAssignedTo());
        }

        if (pm.getAsset() != null) {
            wo.setAsset(pm.getAsset());
            if (pm.getAsset().getName() != null) {
                wo.setAssetName(pm.getAsset().getName().trim());
            }
        }

        if (schedule != null && schedule.getDueDateDelay() != null) {
            if (schedule.getDueDateDelay() < 0) {
                throw new CustomException("Due date delay must be >= 0", HttpStatus.BAD_REQUEST);
            }
            wo.setDueDate(LocalDate.now().plusDays(schedule.getDueDateDelay()));
        } else {
            wo.setDueDate(LocalDate.now());
        }

        wo.setDateCreated(LocalDateTime.now());
        wo.setCreatedAt(LocalDateTime.now());

        return workOrderRepository.save(wo);
    }

    @Transactional(readOnly = true)
    public Collection<WorkOrder> findByCreatedAtBetween(Date start, Date end) {
        if (start == null || end == null) {
            throw new CustomException("Start date and end date must not be null", HttpStatus.BAD_REQUEST);
        }
        if (end.before(start)) {
            throw new CustomException("End date must be after or equal to start date", HttpStatus.BAD_REQUEST);
        }

        LocalDateTime startDateTime = Instant.ofEpochMilli(start.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        LocalDateTime endDateTime = Instant.ofEpochMilli(end.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return workOrderRepository.findByDateCreatedBetween(startDateTime, endDateTime);
    }

    @Transactional(readOnly = true)
    public Collection<WorkOrder> findByCompletedOnBetween(Date start, Date end) {
        if (start == null || end == null) {
            throw new CustomException("Start date and end date must not be null", HttpStatus.BAD_REQUEST);
        }
        if (end.before(start)) {
            throw new CustomException("End date must be after or equal to start date", HttpStatus.BAD_REQUEST);
        }

        LocalDateTime startDateTime = Instant.ofEpochMilli(start.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        LocalDateTime endDateTime = Instant.ofEpochMilli(end.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return workOrderRepository.findByCompletedOnBetween(startDateTime, endDateTime);
    }

    public long getAllCost(Collection<WorkOrder> workOrders, boolean returnZeroIfNull) {
        if (workOrders == null || workOrders.isEmpty()) {
            return 0L;
        }

        BigDecimal total = workOrders.stream()
                .filter(Objects::nonNull)
                .map(WorkOrder::getTotalCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.longValue();
    }

    private void validateCreate(WorkOrderPostDTO dto) {
        if (dto == null) {
            throw new CustomException("Work order data must not be null", HttpStatus.BAD_REQUEST);
        }

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new CustomException("Work order title must not be blank", HttpStatus.BAD_REQUEST);
        }

        if (dto.getEstimatedDuration() != null && dto.getEstimatedDuration() < 0) {
            throw new CustomException("Estimated duration must be >= 0", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateUpdate(Long id, WorkOrderDTO dto) {
        if (id == null) {
            throw new CustomException("Work order id must not be null", HttpStatus.BAD_REQUEST);
        }

        if (dto == null) {
            throw new CustomException("Work order update data must not be null", HttpStatus.BAD_REQUEST);
        }

        if (dto.getEstimatedDuration() != null && dto.getEstimatedDuration() < 0) {
            throw new CustomException("Estimated duration must be >= 0", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean hasAdminOrManagerRole(User user) {
        if (user == null || user.getRole() == null || user.getRole().getCode() == null) {
            return false;
        }

        String roleCode = user.getRole().getCode().name();
        return "ADMIN".equalsIgnoreCase(roleCode) || "TECHNICAL_MANAGER".equalsIgnoreCase(roleCode);
    }
}