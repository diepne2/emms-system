package com.emms.backend.service;

import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenanceDTO;
import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenancePostDTO;
import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenanceSummaryDTO;
import com.emms.backend.dto.preventiveMaintenance.RecurrenceRule;
import com.emms.backend.entity.PreventiveMaintenance;
import com.emms.backend.entity.Schedule;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.enums.Priority;
import com.emms.backend.entity.enums.RecurrenceType;
import com.emms.backend.exception.CustomException;
import com.emms.backend.repository.PreventiveMaintenanceRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PreventiveMaintenanceService {

    private final PreventiveMaintenanceRepository repository;
    private final UserService userService;
    private final AssetService assetService;
    private final WorkOrderService workOrderService;
    private final NotificationService notificationService;

    public PreventiveMaintenanceSummaryDTO create(PreventiveMaintenancePostDTO dto) {
        validateCreate(dto);

        PreventiveMaintenance pm = new PreventiveMaintenance();
        applyCreate(pm, dto);
        validateUniqueCode(pm.getCode());

        PreventiveMaintenance saved = repository.save(pm);
        return toSummaryDto(saved);
    }

    public PreventiveMaintenanceSummaryDTO update(Long id, PreventiveMaintenanceDTO dto) {
        if (id == null) {
            throw new CustomException("ID bắt buộc", HttpStatus.BAD_REQUEST);
        }
        validateUpdate(dto);

        PreventiveMaintenance pm = findEntityById(id);
        applyUpdate(pm, dto);

        PreventiveMaintenance saved = repository.save(pm);
        return toSummaryDto(saved);
    }

    @Transactional(readOnly = true)
    public List<PreventiveMaintenanceSummaryDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PreventiveMaintenanceSummaryDTO getById(Long id) {
        return toSummaryDto(findEntityById(id));
    }

    public PreventiveMaintenance findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new CustomException("PM không tìm thấy : " + id, HttpStatus.NOT_FOUND));
    }

    public void delete(Long id) {
        PreventiveMaintenance pm = findEntityById(id);
        try {
            repository.delete(pm);
        } catch (DataIntegrityViolationException ex) {
            pm.setActive(false);

            Schedule schedule = pm.getSchedule();
            if (schedule != null) {
                schedule.setDisabled(true);
            }
            repository.save(pm);
            throw new CustomException(
                "Kế hoạch bảo trì đã phát sinh Work Order nên không thể xóa. Hệ thống đã tắt lịch và chuyển sang ngưng hoạt động.",
                HttpStatus.CONFLICT
          );
        }
    }

    public void disableByWorkOrder(WorkOrder workOrder) {
        if (workOrder == null || workOrder.getPreventiveMaintenance() == null) {
            return;
        }
        
        PreventiveMaintenance pm = workOrder.getPreventiveMaintenance();
        pm.setActive(false);
        
        Schedule schedule = pm.getSchedule();
        if (schedule != null) {
            schedule.setDisabled(true);
        }
        
        repository.save(pm);
    }

    public void forceDelete(Long id) {
        PreventiveMaintenance pm = findEntityById(id);
        
        pm.setActive(false);
        Schedule schedule = pm.getSchedule();
        if (schedule != null) {
            schedule.setDisabled(true);
        }
        
        repository.save(pm);
        repository.delete(pm);
    }

    private PreventiveMaintenanceSummaryDTO toSummaryDto(PreventiveMaintenance pm) {
        Schedule schedule = pm.getSchedule();
        RecurrenceRule rule = toRecurrenceRule(schedule, pm.getPriority());

        return new PreventiveMaintenanceSummaryDTO(
                pm.getId(),
                pm.getCode(),
                pm.getTitle(),
                pm.getDescription(),
                pm.isActive(),

                pm.getAsset() != null ? pm.getAsset().getId() : null,
                pm.getAsset() != null ? pm.getAsset().getName() : null,

                pm.getAssignedTo() != null ? pm.getAssignedTo().getUserId() : null,
                pm.getAssignedTo() != null ? pm.getAssignedTo().getUsername() : null,

                pm.getPriority(),
                schedule != null ? schedule.getStartsOn() : null,
                schedule != null ? schedule.getEndsOn() : null,
                rule
        );
    }

    private RecurrenceRule toRecurrenceRule(Schedule schedule, Priority priority) {
        if (schedule == null) {
            return null;
        }

        RecurrenceRule rule = new RecurrenceRule();
        rule.setType(schedule.getRecurrenceType());
        rule.setBasedOn(schedule.getRecurrenceBasedOn());
        rule.setFrequency(schedule.getFrequency());
        rule.setDueDateDelay(schedule.getDueDateDelay());
        rule.setDaysOfWeek(schedule.getDaysOfWeek());
        rule.setPriority(priority);
        return rule;
    }

    private void applyCreate(PreventiveMaintenance pm, PreventiveMaintenancePostDTO dto) {
        pm.setTitle(trim(dto.getTitle()));
        pm.setDescription(trim(dto.getDescription()));
        pm.setEstimatedHours(dto.getEstimatedHours());
        pm.setActive(true);

        if (dto.getAssetId() != null) {
            pm.setAsset(assetService.getById(dto.getAssetId()));
        }

        if (dto.getAssignedToId() != null) {
            pm.setAssignedTo(userService.findEntityById(dto.getAssignedToId()));
        }

        RecurrenceRule rule = dto.getRecurrenceRule();
        pm.setPriority(rule.getPriority() != null ? rule.getPriority() : Priority.MEDIUM);

        Schedule schedule = new Schedule();
        schedule.setPreventiveMaintenance(pm);
        schedule.setDisabled(false);
        schedule.setStartsOn(dto.getStartsOn().toLocalDate());
        schedule.setEndsOn(dto.getEndsOn() != null ? dto.getEndsOn().toLocalDate() : null);
        schedule.setFrequency(rule.getFrequency() != null ? rule.getFrequency() : 1);
        schedule.setDueDateDelay(rule.getDueDateDelay() != null ? rule.getDueDateDelay() : 0);
        schedule.setRecurrenceType(rule.getType() != null ? rule.getType() : RecurrenceType.DAILY);
        schedule.setRecurrenceBasedOn(rule.getBasedOn());
        schedule.setDaysOfWeek(rule.getType() == RecurrenceType.WEEKLY ? rule.getDaysOfWeek() : null);

        pm.setSchedule(schedule);
    }

    private void applyUpdate(PreventiveMaintenance pm, PreventiveMaintenanceDTO dto) {
        if (dto.getTitle() != null) pm.setTitle(trim(dto.getTitle()));
        if (dto.getDescription() != null) pm.setDescription(trim(dto.getDescription()));
        if (dto.getEstimatedHours() != null) pm.setEstimatedHours(dto.getEstimatedHours());
        if (dto.getPriority() != null) pm.setPriority(dto.getPriority());

        if (dto.getAssetId() != null) {
            pm.setAsset(assetService.getById(dto.getAssetId()));
        }

        if (dto.getAssignedToId() != null) {
            pm.setAssignedTo(userService.findEntityById(dto.getAssignedToId()));
        }

        Schedule schedule = pm.getSchedule();
        if (schedule == null) {
            schedule = new Schedule();
            schedule.setPreventiveMaintenance(pm);
            schedule.setDisabled(false);
            pm.setSchedule(schedule);
        }

        if (dto.getStartsOn() != null) {
            schedule.setStartsOn(dto.getStartsOn().toLocalDate());
        }

        // Cho phép xóa ngày kết thúc khi FE gửi endsOn = null.
        schedule.setEndsOn(dto.getEndsOn() != null ? dto.getEndsOn().toLocalDate() : null);

        RecurrenceRule rule = dto.getRecurrenceRule();
        if (rule != null) {
            if (rule.getPriority() != null) {
                pm.setPriority(rule.getPriority());
            }
            schedule.setFrequency(rule.getFrequency() != null ? rule.getFrequency() : 1);
            schedule.setDueDateDelay(rule.getDueDateDelay() != null ? rule.getDueDateDelay() : 0);
            schedule.setRecurrenceType(rule.getType() != null ? rule.getType() : RecurrenceType.DAILY);
            schedule.setRecurrenceBasedOn(rule.getBasedOn());
            schedule.setDaysOfWeek(rule.getType() == RecurrenceType.WEEKLY ? rule.getDaysOfWeek() : null);
        }

        if (dto.getActive() != null) {
            pm.setActive(dto.getActive());
            schedule.setDisabled(!dto.getActive());
        }
    }

    private void validateCreate(PreventiveMaintenancePostDTO dto) {
        if (dto == null) {
            throw new CustomException("DTO null", HttpStatus.BAD_REQUEST);
        }

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new CustomException("Tiêu đề là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        if (dto.getStartsOn() == null) {
            throw new CustomException("Ngày bắt đầu là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        validateDateRange(dto.getStartsOn(), dto.getEndsOn());
        validateRecurrenceRule(dto.getRecurrenceRule());
    }

    private void validateUpdate(PreventiveMaintenanceDTO dto) {
        if (dto == null) {
            throw new CustomException("DTO null", HttpStatus.BAD_REQUEST);
        }

        validateDateRange(dto.getStartsOn(), dto.getEndsOn());

        if (dto.getRecurrenceRule() != null) {
            validateRecurrenceRule(dto.getRecurrenceRule());
        }
    }

    private void validateDateRange(java.time.LocalDateTime startsOn, java.time.LocalDateTime endsOn) {
        if (startsOn != null && endsOn != null && endsOn.toLocalDate().isBefore(startsOn.toLocalDate())) {
            throw new CustomException(
                    "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validateRecurrenceRule(RecurrenceRule rule) {
        if (rule == null) {
            throw new CustomException("Quy tắc lặp lại là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        if (rule.getType() == RecurrenceType.WEEKLY &&
                (rule.getDaysOfWeek() == null || rule.getDaysOfWeek().isEmpty())) {
            throw new CustomException(
                    "Ngày trong tuần là bắt buộc cho loại lặp lại hàng tuần",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (rule.getFrequency() != null && rule.getFrequency() <= 0) {
            throw new CustomException("Tần suất phải lớn hơn 0", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateUniqueCode(String code) {
        if (code == null) return;

        if (repository.existsByCodeIgnoreCase(code)) {
            throw new CustomException("Mã trùng lặp", HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public void generateWorkOrder(Long pmId) {
        if (pmId == null) {
            throw new CustomException("ID PM là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        PreventiveMaintenance pm = findEntityById(pmId);
        Schedule schedule = pm.getSchedule();

        workOrderService.createFromPreventiveMaintenance(pm, schedule);
    }

    private String trim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
