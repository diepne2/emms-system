package com.emms.backend.service;

import com.emms.backend.dto.workorder.WorkOrderDTO;
import com.emms.backend.dto.workorder.WorkOrderOptionDTO;
import com.emms.backend.dto.workorder.WorkOrderPostDTO;
import com.emms.backend.dto.workorder.WorkOrderShowDTO;
import com.emms.backend.entity.Asset;
import com.emms.backend.entity.Meter;
import com.emms.backend.entity.PreventiveMaintenance;
import com.emms.backend.entity.Reading;
import com.emms.backend.entity.Schedule;
import com.emms.backend.entity.User;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.WorkOrderMeterTrigger;
import com.emms.backend.entity.enums.AssetStatus;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.WorkOrderMapper;
import com.emms.backend.repository.WorkOrderRepository;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class WorkOrderService {

    private static final int DONE_VISIBLE_DAYS = 3;

    private final WorkOrderRepository workOrderRepository;
    private final AssetService assetService;
    private final UserService userService;
    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderHistoryService workOrderHistoryService;
    private final NotificationService notificationService;
    private final RequestService requestService;
    
    private final PreventiveMaintenanceService preventiveMaintenanceService;

    public WorkOrderService(
            WorkOrderRepository workOrderRepository,
            AssetService assetService,
            UserService userService,
            WorkOrderMapper workOrderMapper,
            WorkOrderHistoryService workOrderHistoryService,
            NotificationService notificationService,
            RequestService requestService,
            @Lazy PreventiveMaintenanceService preventiveMaintenanceService
    ) {
        this.workOrderRepository = workOrderRepository;
        this.assetService = assetService;
        this.userService = userService;
        this.workOrderMapper = workOrderMapper;
        this.workOrderHistoryService = workOrderHistoryService;
        this.notificationService = notificationService;
        this.requestService = requestService;
        this.preventiveMaintenanceService = preventiveMaintenanceService;
    }

    public WorkOrderShowDTO create(WorkOrderPostDTO dto) {
        validateCreate(dto);

        WorkOrder entity = workOrderMapper.fromPostDto(dto);

        User assignedUser = userService.findEntityById(dto.getAssignedToId());
        entity.setAssignedTo(assignedUser);

        Asset asset = assetService.getById(dto.getAssetId());
        entity.setAsset(asset);
        entity.setAssetName(asset.getName());

        if (dto.getLocationName() != null && !dto.getLocationName().isBlank()) {
            entity.setLocationName(dto.getLocationName().trim());
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

        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }

        if (entity.getDateCreated() == null) {
            entity.setDateCreated(LocalDateTime.now());
        }

        syncAssetStatusFromWorkOrder(entity);

        WorkOrder saved = workOrderRepository.save(entity);
        saveWorkOrderHistorySnapshot(saved, "CREATED", "Work order được tạo mới");

        notifyAssignedUser(
                saved,
                "Work Order mới",
                "Bạn được giao Work Order mới: " + safeTitle(saved.getTitle())
        );

        return workOrderMapper.toShowDto(saved);
    }

    public WorkOrderShowDTO update(Long id, WorkOrderDTO dto) {
    validateUpdate(id, dto);

    WorkOrder existing = findEntityById(id);

    Long oldAssignedUserId = existing.getAssignedTo() != null
            ? existing.getAssignedTo().getUserId()
            : null;

    workOrderMapper.updateWorkOrder(existing, dto);

    User assignedUser = userService.findEntityById(dto.getAssignedToId());
    existing.setAssignedTo(assignedUser);

    Asset asset = assetService.getById(dto.getAssetId());
    existing.setAsset(asset);
    existing.setAssetName(asset.getName());

    if (dto.getLocationName() != null && !dto.getLocationName().isBlank()) {
        existing.setLocationName(dto.getLocationName().trim());
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

    syncAssetStatusFromWorkOrder(existing);

    WorkOrder saved = workOrderRepository.save(existing);
    saveWorkOrderHistorySnapshot(saved, "UPDATED", "Cập nhật work order");

    Long newAssignedUserId = saved.getAssignedTo() != null
            ? saved.getAssignedTo().getUserId()
            : null;

    if (newAssignedUserId != null && !newAssignedUserId.equals(oldAssignedUserId)) {
        notificationService.createNotificationIfUserExists(
                newAssignedUserId,
                "Bạn được giao Work Order",
                "Bạn vừa được assign Work Order: " + safeTitle(saved.getTitle())
            );
        } 
        return workOrderMapper.toShowDto(saved); 
    }

    @Transactional(readOnly = true)
    public WorkOrderShowDTO getById(Long id) {
        return workOrderMapper.toShowDto(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<WorkOrderShowDTO> getAll() {
        return workOrderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(workOrderMapper::toShowDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkOrderShowDTO> getByPreventiveMaintenance(Long pmId) {
        if (pmId == null) {
            throw new CustomException("PM id must not be null", HttpStatus.BAD_REQUEST);
        }

        return workOrderRepository
                .findByPreventiveMaintenance_IdOrderByCreatedAtDesc(pmId)
                .stream()
                .map(workOrderMapper::toShowDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkOrderShowDTO> getWorkOrdersForUser(User user) {
        if (user == null) {
            throw new CustomException("Current user must not be null", HttpStatus.UNAUTHORIZED);
        }

        LocalDateTime doneVisibleFrom = LocalDateTime.now().minusDays(DONE_VISIBLE_DAYS);

        List<WorkOrder.WorkOrderStatus> activeStatuses = List.of(
                WorkOrder.WorkOrderStatus.OPEN,
                WorkOrder.WorkOrderStatus.IN_PROGRESS,
                WorkOrder.WorkOrderStatus.ON_HOLD,
                WorkOrder.WorkOrderStatus.PENDING
        );

        List<WorkOrder> result = new ArrayList<>();

        if (hasAdminOrManagerRole(user)) {
            result.addAll(
                    workOrderRepository.findByArchivedFalseAndStatusInOrderByCreatedAtDesc(activeStatuses)
            );
            result.addAll(
                    workOrderRepository.findByArchivedFalseAndStatusAndCompletedOnAfterOrderByCreatedAtDesc(
                            WorkOrder.WorkOrderStatus.DONE,
                            doneVisibleFrom
                    )
            );
        } else {
            if (user.getUserId() == null) {
                throw new CustomException("Current user id must not be null", HttpStatus.UNAUTHORIZED);
            }

            result.addAll(
                    workOrderRepository.findByAssignedTo_UserIdAndArchivedFalseAndStatusInOrderByCreatedAtDesc(
                            user.getUserId(),
                            activeStatuses
                    )
            );
            result.addAll(
                    workOrderRepository.findByAssignedTo_UserIdAndArchivedFalseAndStatusAndCompletedOnAfterOrderByCreatedAtDesc(
                            user.getUserId(),
                            WorkOrder.WorkOrderStatus.DONE,
                            doneVisibleFrom
                    )
            );
        }

        result.sort(buildDashboardComparator());

        return result.stream()
                .map(workOrderMapper::toShowDto)
                .toList();
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
    public WorkOrder findEntityByIdForLabor(Long id) {
        if (id == null) {
            throw new CustomException("Work order id must not be null", HttpStatus.BAD_REQUEST);
        }return workOrderRepository.findById(id)
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

        throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
    }

    @Transactional(readOnly = true)
    public WorkOrder checkCanChangeStatus(Long id, User currentUser, WorkOrder.WorkOrderStatus nextStatus) {
        if (currentUser == null) {
            throw new CustomException("Current user must not be null", HttpStatus.UNAUTHORIZED);
        }

        if (nextStatus == null) {
            throw new CustomException("Status must not be null", HttpStatus.BAD_REQUEST);
        }

        WorkOrder workOrder = findEntityById(id);
        WorkOrder.WorkOrderStatus currentStatus = workOrder.getStatus();

        if (hasAdminOrManagerRole(currentUser)) {
            validateManagerStatusTransition(currentStatus, nextStatus);
            return workOrder;
        }

        if (!isTechnician(currentUser)) {
            throw new CustomException("Bạn không có quyền cập nhật trạng thái", HttpStatus.FORBIDDEN);
        }

        if (workOrder.getAssignedTo() == null
                || workOrder.getAssignedTo().getUserId() == null
                || currentUser.getUserId() == null
                || !workOrder.getAssignedTo().getUserId().equals(currentUser.getUserId())) {
            throw new CustomException("Bạn không được assign work order này", HttpStatus.FORBIDDEN);
        }

        validateTechnicianStatusTransition(currentStatus, nextStatus);
        return workOrder;
    }

    public void delete(Long id) {
        WorkOrder existing = findEntityById(id);

        if (existing.getStatus() != WorkOrder.WorkOrderStatus.CANCELLED) {
            throw new CustomException( 
                "Chỉ được xóa vĩnh viễn Work Order đã hủy",
                HttpStatus.CONFLICT
            );
        }
        requestService.cancelAndDetachByWorkOrderId(id);
        preventiveMaintenanceService.disableByWorkOrder(existing);
        
        existing.setPreventiveMaintenance(null);
        workOrderRepository.save(existing);
        workOrderRepository.delete(existing);

    }

    public WorkOrderShowDTO markCompleted(Long id, Long completedByUserId, String feedback) {
        if (completedByUserId == null) {
            throw new CustomException("Completed by user id must not be null", HttpStatus.BAD_REQUEST);
        }

        WorkOrder existing = findEntityById(id);
        User completedBy = userService.findEntityById(completedByUserId);

        if (existing.getStatus() != WorkOrder.WorkOrderStatus.IN_PROGRESS) {
            throw new CustomException("Chỉ work order đang thực hiện mới có thể gửi chờ duyệt", HttpStatus.BAD_REQUEST);
        }

        WorkOrder.WorkOrderStatus oldStatus = existing.getStatus();

        existing.setCompletedBy(completedBy.getFullName());
        existing.setCompletedOn(LocalDateTime.now());
        existing.setStatus(WorkOrder.WorkOrderStatus.PENDING);
        existing.setArchived(false);

        if (feedback != null && !feedback.isBlank()) {
            existing.setFeedback(feedback.trim());
        }

        syncAssetStatusFromWorkOrder(existing);

        WorkOrder saved = workOrderRepository.save(existing);
        saveWorkOrderHistorySnapshot(
                saved,
                "SUBMIT_FOR_APPROVAL",
                buildStatusHistoryNote(oldStatus, WorkOrder.WorkOrderStatus.PENDING, feedback)
        );

        notifyAssignedUser(
                saved,
                "Work Order chờ duyệt",
                "Work Order \"" + safeTitle(saved.getTitle()) + "\" đã được gửi chờ duyệt."
        );

        return workOrderMapper.toShowDto(saved);
    }

    public WorkOrderShowDTO changeStatus(Long id, WorkOrder.WorkOrderStatus status, String feedback) {
        if (status == null) {
            throw new CustomException("Status must not be null", HttpStatus.BAD_REQUEST);
        }

        WorkOrder existing = findEntityById(id);
        WorkOrder.WorkOrderStatus oldStatus = existing.getStatus();

        existing.setStatus(status);

        if (status == WorkOrder.WorkOrderStatus.DONE) {
            if (existing.getCompletedOn() == null) {
                existing.setCompletedOn(LocalDateTime.now());
            }
            if (existing.getCompletedBy() == null || existing.getCompletedBy().isBlank()) {
                User currentUser = userService.whoami();
                existing.setCompletedBy(
                        currentUser.getFullName() != null && !currentUser.getFullName().isBlank()
                                ? currentUser.getFullName().trim()
                                : currentUser.getUsername()
                );
            }
            existing.setArchived(false);

        } else if (status == WorkOrder.WorkOrderStatus.CANCELLED) {
            if (existing.getCompletedOn() == null) {
                existing.setCompletedOn(LocalDateTime.now());
            }
            existing.setArchived(true);

        } else if (status == WorkOrder.WorkOrderStatus.PENDING) {
            existing.setArchived(false);
            if (existing.getCompletedOn() == null) {
                existing.setCompletedOn(LocalDateTime.now());
            }

        } else {
            existing.setArchived(false);
            existing.setCompletedOn(null);
            existing.setCompletedBy(null);
        }

        if (feedback != null && !feedback.isBlank()) {
            existing.setFeedback(feedback.trim());
        }

        syncAssetStatusFromWorkOrder(existing);

        WorkOrder saved = workOrderRepository.save(existing);

        if (oldStatus != status) {
            String actionName = switch (status) {
                case DONE -> "DONE";
                case CANCELLED -> "CANCELLED";
                default -> "STATUS_CHANGED";
            };

            saveWorkOrderHistorySnapshot(
                    saved,
                    actionName,
                    buildStatusHistoryNote(oldStatus, status, feedback)
            );

            notifyAssignedUser(
                    saved,
                    "Trạng thái Work Order thay đổi",
                    "Work Order \"" + safeTitle(saved.getTitle()) + "\" chuyển từ " + oldStatus + " sang " + status + "."
            );
        }

        return workOrderMapper.toShowDto(saved);
    }

    public WorkOrderShowDTO archive(Long id, boolean archived) {
        WorkOrder existing = findEntityById(id);
        existing.setArchived(archived);

        WorkOrder saved = workOrderRepository.save(existing);
        saveWorkOrderHistorySnapshot(
                saved,
                archived ? "ARCHIVED" : "UNARCHIVED",
                archived ? "Work order đã được archive" : "Work order đã được bỏ archive"
        );

        notifyAssignedUser(
                saved,
                archived ? "Work Order đã lưu trữ" : "Work Order bỏ lưu trữ",
                "Work Order \"" + safeTitle(saved.getTitle()) + "\" đã được " +
                        (archived ? "lưu trữ." : "bỏ lưu trữ.")
        );

        return workOrderMapper.toShowDto(saved);
    }

    public void archiveExpiredDoneWorkOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(DONE_VISIBLE_DAYS);

        List<WorkOrder> expiredDoneItems =
                workOrderRepository.findByArchivedFalseAndStatusAndCompletedOnBefore(
                        WorkOrder.WorkOrderStatus.DONE,
                        threshold
                );

        if (expiredDoneItems.isEmpty()) {
            return;
        }

        expiredDoneItems.forEach(item -> item.setArchived(true));
        workOrderRepository.saveAll(expiredDoneItems);
    }

    public List<WorkOrder> saveAll(List<WorkOrder> items) {
        if (items == null || items.isEmpty()) {
            throw new CustomException("Danh sách Work Order là bắt buộc", HttpStatus.BAD_REQUEST);
        }
        return workOrderRepository.saveAll(items);
    }

    @Transactional
    public WorkOrder createFromPreventiveMaintenance(PreventiveMaintenance pm, Schedule schedule,LocalDate scheduledDate) {
        if (pm == null || pm.getId() == null) {
            throw new CustomException("Bảo trì định kỳ là bắt buộc", HttpStatus.BAD_REQUEST);
        }
        
        if (!pm.isActive()) {
            throw new CustomException(
                "Kế hoạch bảo trì đã bị tắt, không thể tạo Work Order.",
                HttpStatus.BAD_REQUEST
            );
        }

        if (scheduledDate == null) {
            scheduledDate = LocalDate.now();
        }
        
        if (schedule == null) {
            throw new CustomException(
                "Kế hoạch bảo trì chưa có lịch, không thể tạo Work Order.",
                HttpStatus.BAD_REQUEST
            );
        }
        
        
        if (schedule.isDisabled()) {
            throw new CustomException(
                "Lịch bảo trì đã bị vô hiệu hóa, không thể tạo Work Order.",
                HttpStatus.BAD_REQUEST
            );
        }
        
        
        if (schedule.getStartsOn() == null) {
            throw new CustomException(
                "Ngày bắt đầu lịch bảo trì là bắt buộc.",
                HttpStatus.BAD_REQUEST
            );
        }
    
        if (schedule.getStartsOn().isAfter(scheduledDate)) {
            throw new CustomException(
                "Chưa tới lịch bảo trì, không thể tạo Work Order.",
                HttpStatus.BAD_REQUEST
            );
        }


        if (schedule.getEndsOn() != null && scheduledDate.isAfter(schedule.getEndsOn())) {
            throw new CustomException(
                "Kế hoạch bảo trì đã hết hạn, không thể tạo Work Order mới.",
                HttpStatus.BAD_REQUEST
            );
        }
        
        if (pm.getTitle() == null || pm.getTitle().isBlank()) {
            throw new CustomException("Tiêu đề bảo trì định kỳ là bắt buộc", HttpStatus.BAD_REQUEST);
        }
        
        Integer delay = schedule.getDueDateDelay();
        
        
        if (delay != null && delay < 0) {
            throw new CustomException(
                "Delay ngày đến hạn phải lớn hơn hoặc bằng 0",
                HttpStatus.BAD_REQUEST
            );
        }
        
    
        LocalDate dueDate = scheduledDate;

        
        if (delay != null) {
            dueDate = scheduledDate.plusDays(delay);
        }
        
        boolean existed = workOrderRepository.existsByPreventiveMaintenance_IdAndDueDate(
            pm.getId(),
            dueDate
        );
        
        
        if (existed) {
            throw new CustomException(
                "Đã tồn tại Work Order cho kế hoạch bảo trì này và ngày đến hạn.",
                HttpStatus.CONFLICT
            );
        }
        
        WorkOrder wo = new WorkOrder();
        
        wo.setTitle("WO - " + pm.getTitle());
        wo.setDescription(pm.getDescription());
        wo.setStatus(WorkOrder.WorkOrderStatus.OPEN);
        wo.setArchived(false);
        wo.setPreventiveMaintenance(pm);
        wo.setDueDate(dueDate);
        wo.setPriority(mapPmPriority(pm.getPriority()));
        
        if (pm.getEstimatedHours() != null) {
            if (pm.getEstimatedHours() < 0) {
                throw new CustomException(
                    "Giờ ước lượng phải lớn hơn hoặc bằng 0",
                    HttpStatus.BAD_REQUEST
                );
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
        
        wo.setDateCreated(LocalDateTime.now());
        wo.setCreatedAt(LocalDateTime.now());
        
        syncAssetStatusFromWorkOrder(wo);
        
        WorkOrder saved = workOrderRepository.save(wo);
        
        saveWorkOrderHistorySnapshot(
            saved,
            "CREATED_FROM_PM",
            "Tạo work order từ preventive maintenance"
        );
        
        notifyAssignedUser(
            saved,
            "Work Order PM mới",
            "Bạn được giao Work Order từ bảo trì định kỳ: " + safeTitle(saved.getTitle())
        );
        
        return saved;
    }

    @Transactional
    public WorkOrder createFromPreventiveMaintenance(PreventiveMaintenance pm, Schedule schedule) {
        return createFromPreventiveMaintenance(pm, schedule, LocalDate.now());
    }

    

    @Transactional(readOnly = true)
    public Collection<WorkOrder> findByCreatedAtBetween(Date start, Date end) {
        if (start == null || end == null) {
            throw new CustomException("Ngày bắt đầu và ngày kết thúc là bắt buộc", HttpStatus.BAD_REQUEST);
        }
        if (end.before(start)) {
            throw new CustomException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu", HttpStatus.BAD_REQUEST);
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
            throw new CustomException("Ngày bắt đầu và ngày kết thúc là bắt buộc", HttpStatus.BAD_REQUEST);
        }
        if (end.before(start)) {
            throw new CustomException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu", HttpStatus.BAD_REQUEST);
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

    public WorkOrder createAutoFromMeterTrigger(WorkOrderMeterTrigger trigger, Reading reading) {
        if (trigger == null || trigger.getId() == null) {
            throw new CustomException("Meter trigger là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        if (reading == null || reading.getId() == null) {
            throw new CustomException("Reading là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        Meter meter = reading.getMeter();
        if (meter == null) {
            throw new CustomException("Meter là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        Asset asset = meter.getAsset();
        if (asset == null || asset.getId() == null) {
            throw new CustomException("Asset là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        WorkOrder entity = new WorkOrder();

        entity.setTitle("Auto WO - " + trigger.getName() + " - " + meter.getName());
        entity.setDescription(
                "Automatically created from meter trigger.\n"
                        + "Meter: " + meter.getName() + "\n"
                        + "Reading value: " + reading.getValue() + "\n"
                        + "Delta: " + reading.getDeltaValue() + "\n"
                        + "Condition: " + trigger.getTriggerCondition() + "\n"
                        + "Trigger value: " + trigger.getTriggerValue()
        );

        entity.setAsset(asset);
        entity.setAssetName(asset.getName());

        if (meter.getLocation() != null && meter.getLocation().getName() != null) {
            entity.setLocationName(meter.getLocation().getName().trim());
        }

        entity.setStatus(WorkOrder.WorkOrderStatus.OPEN);
        entity.setArchived(false);
        entity.setDateCreated(LocalDateTime.now());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setDueDate(LocalDate.now());

        if (trigger.getPriority() != null) {
            entity.setPriority(mapTriggerPriority(trigger.getPriority()));
        } else {
            entity.setPriority(WorkOrder.WorkOrderPriority.NONE);
        }

        if (meter.getUsers() != null && !meter.getUsers().isEmpty()) {
            User assigned = meter.getUsers().get(0);
            if (assigned != null && assigned.getUserId() != null) {
                entity.setAssignedTo(assigned);
            }
        }

        if (entity.getTotalCost() == null) {
            entity.setTotalCost(BigDecimal.ZERO);
        }

        syncAssetStatusFromWorkOrder(entity);

        WorkOrder saved = workOrderRepository.save(entity);
        saveWorkOrderHistorySnapshot(
                saved,
                "AUTO_CREATED_FROM_METER_TRIGGER",
                "Tạo tự động từ meter trigger: " + trigger.getName()
        );

        notifyAssignedUser(
                saved,
                "Work Order tự động mới",
                "Hệ thống tự tạo Work Order từ meter trigger: " + safeTitle(saved.getTitle())
        );

        return saved;
    }

    private WorkOrder.WorkOrderPriority mapPmPriority(com.emms.backend.entity.enums.Priority priority) {
        if (priority == null) {
            return WorkOrder.WorkOrderPriority.NONE;
        }

        return switch (priority) {
            case LOW -> WorkOrder.WorkOrderPriority.LOW;
            case MEDIUM -> WorkOrder.WorkOrderPriority.MEDIUM;
            case HIGH -> WorkOrder.WorkOrderPriority.HIGH;
            case URGENT -> WorkOrder.WorkOrderPriority.URGENT;
            default -> WorkOrder.WorkOrderPriority.NONE;
        };
    }

    private WorkOrder.WorkOrderPriority mapTriggerPriority(Object priority) {
        if (priority == null) {
            return WorkOrder.WorkOrderPriority.NONE;
        }

        String name = priority.toString().trim().toUpperCase();

        return switch (name) {
            case "LOW" -> WorkOrder.WorkOrderPriority.LOW;
            case "MEDIUM" -> WorkOrder.WorkOrderPriority.MEDIUM;
            case "HIGH" -> WorkOrder.WorkOrderPriority.HIGH;
            case "URGENT" -> WorkOrder.WorkOrderPriority.URGENT;
            default -> WorkOrder.WorkOrderPriority.NONE;
        };
    }

    private void validateCreate(WorkOrderPostDTO dto) {
        if (dto == null) {
            throw new CustomException("Work order data là bắt buộc", HttpStatus.BAD_REQUEST);
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new CustomException("Tiêu đề là bắt buộc", HttpStatus.BAD_REQUEST);
        }
        if (dto.getAssetId() == null) {
            throw new CustomException("Thiết bị là bắt buộc", HttpStatus.BAD_REQUEST);
        }
        if (dto.getAssignedToId() == null) {
            throw new CustomException("Người thực hiện là bắt buộc", HttpStatus.BAD_REQUEST);
        }
        if (dto.getDueDate() == null) {
            throw new CustomException("Ngày không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto.getPriority() == null) {
            throw new CustomException("Ưu tiên không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto.getLocationName() == null || dto.getLocationName().isBlank()) {
            throw new CustomException("Location không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto.getEstimatedDuration() != null && dto.getEstimatedDuration() < 0) {
            throw new CustomException("Estimated duration phải lớn hơn hoặc bằng 0", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateUpdate(Long id, WorkOrderDTO dto) {
        if (id == null) {
            throw new CustomException("Work order id là bắt buộc", HttpStatus.BAD_REQUEST);
        }
        if (dto == null) {
            throw new CustomException("Work order update data là bắt buộc", HttpStatus.BAD_REQUEST);
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new CustomException("Tiêu đề không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto.getAssetId() == null) {
            throw new CustomException("Thiết bị không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto.getAssignedToId() == null) {
            throw new CustomException("Người thực hiện không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto.getDueDate() == null) {
            throw new CustomException("Ngày không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto.getPriority() == null) {
            throw new CustomException("Ưu tiên không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto.getLocationName() == null || dto.getLocationName().isBlank()) {
            throw new CustomException("Location không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (dto.getEstimatedDuration() != null && dto.getEstimatedDuration() < 0) {
            throw new CustomException("Estimated duration must be >= 0", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateTechnicianStatusTransition(
            WorkOrder.WorkOrderStatus currentStatus,
            WorkOrder.WorkOrderStatus nextStatus
    ) {
        boolean valid = switch (currentStatus) {
            case OPEN -> nextStatus == WorkOrder.WorkOrderStatus.IN_PROGRESS
                    || nextStatus == WorkOrder.WorkOrderStatus.ON_HOLD;
            case IN_PROGRESS -> nextStatus == WorkOrder.WorkOrderStatus.ON_HOLD
                    || nextStatus == WorkOrder.WorkOrderStatus.PENDING;
            case ON_HOLD -> nextStatus == WorkOrder.WorkOrderStatus.IN_PROGRESS
                    || nextStatus == WorkOrder.WorkOrderStatus.OPEN;
            case PENDING, DONE, CANCELLED -> false;
        };

        if (!valid) {
            throw new CustomException("Bạn không được phép chuyển trạng thái này", HttpStatus.FORBIDDEN);
        }
    }

    private void validateManagerStatusTransition(
            WorkOrder.WorkOrderStatus currentStatus,
            WorkOrder.WorkOrderStatus nextStatus
    ) {
        boolean valid = switch (currentStatus) {
            case OPEN -> nextStatus == WorkOrder.WorkOrderStatus.IN_PROGRESS
                    || nextStatus == WorkOrder.WorkOrderStatus.ON_HOLD
                    || nextStatus == WorkOrder.WorkOrderStatus.CANCELLED;
            case IN_PROGRESS -> nextStatus == WorkOrder.WorkOrderStatus.ON_HOLD
                    || nextStatus == WorkOrder.WorkOrderStatus.PENDING
                    || nextStatus == WorkOrder.WorkOrderStatus.CANCELLED;
            case ON_HOLD -> nextStatus == WorkOrder.WorkOrderStatus.IN_PROGRESS
                    || nextStatus == WorkOrder.WorkOrderStatus.OPEN
                    || nextStatus == WorkOrder.WorkOrderStatus.CANCELLED;
            case PENDING -> nextStatus == WorkOrder.WorkOrderStatus.DONE
                    || nextStatus == WorkOrder.WorkOrderStatus.IN_PROGRESS
                    || nextStatus == WorkOrder.WorkOrderStatus.ON_HOLD
                    || nextStatus == WorkOrder.WorkOrderStatus.CANCELLED;
            case DONE, CANCELLED -> false;
        };

        if (!valid) {
            throw new CustomException("Chuyển trạng thái không hợp lệ", HttpStatus.FORBIDDEN);
        }
    }

    private boolean hasAdminOrManagerRole(User user) {
        if (user == null || user.getRole() == null || user.getRole().getCode() == null) {
            return false;
        }
        
        String roleCode = user.getRole().getCode().name();
        
        return "ADMIN".equalsIgnoreCase(roleCode)
            || "MANAGER".equalsIgnoreCase(roleCode)
            || "TECHNICAL_MANAGER".equalsIgnoreCase(roleCode);
    }

    private boolean isTechnician(User user) {
        if (user == null || user.getRole() == null || user.getRole().getCode() == null) {
            return false;
        }
        String roleCode = user.getRole().getCode().name();
        return "TECHNICIAN".equalsIgnoreCase(roleCode);
    }

    private void syncAssetStatusFromWorkOrder(WorkOrder workOrder) {
        if (workOrder == null || workOrder.getAsset() == null || workOrder.getStatus() == null)  {
            return;
        }

        Asset asset = workOrder.getAsset();

        if (asset.getStatus() == AssetStatus.DECOMMISSIONED) {
            return;
        }


        AssetStatus targetStatus = mapAssetStatusFromWorkOrderStatus(workOrder.getStatus());
        if (targetStatus != null && asset.getStatus() != targetStatus) {
            asset.setStatus(targetStatus);
        }
    }

    private AssetStatus mapAssetStatusFromWorkOrderStatus(WorkOrder.WorkOrderStatus status) {
        if (status == null) {
            return AssetStatus.MAINTENANCE;
        }

        return switch (status) {
            case IN_PROGRESS, PENDING -> AssetStatus.MAINTENANCE;
            case DONE, CANCELLED -> AssetStatus.OPERATIONAL;
            case OPEN, ON_HOLD -> null;

        };
    }

    private Comparator<WorkOrder> buildDashboardComparator() {
        return (a, b) -> {
            LocalDateTime aTime = a.getCreatedAt() != null ? a.getCreatedAt() : a.getDateCreated();
            LocalDateTime bTime = b.getCreatedAt() != null ? b.getCreatedAt() : b.getDateCreated();

            if (aTime == null && bTime == null) {
                Long aId = a.getId() != null ? a.getId() : 0L;
                Long bId = b.getId() != null ? b.getId() : 0L;
                return bId.compareTo(aId);
            }

            if (aTime == null) return 1;
            if (bTime == null) return -1;

            return bTime.compareTo(aTime);
        };
    }

    @Transactional(readOnly = true)
    public List<WorkOrderOptionDTO> getOptions(String keyword) {
        List<WorkOrder> workOrders;

        if (keyword == null || keyword.trim().isBlank()) {
            workOrders = workOrderRepository.findTop20ByOrderByIdDesc();
        } else {
            String q = keyword.trim();
            workOrders = workOrderRepository.findTop20ByTitleContainingIgnoreCaseOrderByIdDesc(q);
        }

        return workOrders.stream()
                .map(wo -> new WorkOrderOptionDTO(
                        wo.getId(),
                        safeGetCode(wo),
                        wo.getTitle(),
                        wo.getStatus() == null ? null : wo.getStatus().name()
                ))
                .toList();
    }

    private String safeGetCode(WorkOrder wo) {
        if (wo == null || wo.getId() == null) {
            return null;
        }
        return "WO-" + wo.getId();
    }

    private void saveWorkOrderHistorySnapshot(WorkOrder workOrder, String actionName, String note) {
        if (workOrder == null || workOrder.getId() == null) {
            return;
        }

        try {
            User currentUser = userService.whoami();
            if (currentUser != null && currentUser.getUserId() != null) {
                workOrderHistoryService.saveSnapshot(workOrder, currentUser, actionName, note);
            }
        } catch (Exception ignored) {
        }
    }

    private String buildStatusHistoryNote(
            WorkOrder.WorkOrderStatus oldStatus,
            WorkOrder.WorkOrderStatus newStatus,
            String feedback
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Status changed: ")
                .append(oldStatus == null ? "NULL" : oldStatus.name())
                .append(" -> ")
                .append(newStatus == null ? "NULL" : newStatus.name());

        if (feedback != null && !feedback.isBlank()) {
            sb.append(" | Note: ").append(feedback.trim());
        }

        return sb.toString();
    }

    private void notifyAssignedUser(WorkOrder workOrder, String title, String message) {
        try {
            if (workOrder == null || workOrder.getAssignedTo() == null) {
                return;
            }

            Long userId = workOrder.getAssignedTo().getUserId();

            if (userId != null) {
                notificationService.createNotificationIfUserExists(userId, title, message);
            }
        } catch (Exception ignored) {
        }
    }

    private String safeTitle(String title) {
        return title == null || title.isBlank() ? "Không có tiêu đề" : title.trim();
    }

    public WorkOrder save(WorkOrder workOrder) {
        if (workOrder == null) {
            throw new CustomException("Work order là bắt buộc", HttpStatus.BAD_REQUEST);
        }
        return workOrderRepository.save(workOrder);
    }

    public WorkOrderShowDTO cancel(Long id) {
        WorkOrder existing = findEntityById(id);
        
        if (existing.getStatus() == WorkOrder.WorkOrderStatus.DONE) {
            throw new CustomException(
                "Không thể hủy Work Order đã hoàn thành",
                HttpStatus.CONFLICT
            );
        }
        
        if (existing.getStatus() == WorkOrder.WorkOrderStatus.CANCELLED) {
            existing.setArchived(true);
            WorkOrder saved = workOrderRepository.save(existing);
            return workOrderMapper.toShowDto(saved);
        }
        
        WorkOrder.WorkOrderStatus oldStatus = existing.getStatus();
        
        existing.setStatus(WorkOrder.WorkOrderStatus.CANCELLED);
        existing.setArchived(true);
        
        if (existing.getCompletedOn() == null) {
            existing.setCompletedOn(LocalDateTime.now());
        }
        
        syncAssetStatusFromWorkOrder(existing);
        WorkOrder saved = workOrderRepository.save(existing);
        
        saveWorkOrderHistorySnapshot(
            saved,
            "CANCELLED",
            buildStatusHistoryNote(oldStatus, WorkOrder.WorkOrderStatus.CANCELLED, "Work Order đã được hủy")
        );
        
        notifyAssignedUser(
            saved,
            "Work Order đã bị hủy",
            "Work Order \"" + safeTitle(saved.getTitle()) + "\" đã bị hủy."
        );
        return workOrderMapper.toShowDto(saved);
    }
}