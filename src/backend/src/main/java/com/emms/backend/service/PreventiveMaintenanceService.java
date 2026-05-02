package com.emms.backend.service;

import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenanceDTO;
import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenancePostDTO;
import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenanceSummaryDTO;
import com.emms.backend.dto.preventiveMaintenance.RecurrenceRule;
import com.emms.backend.entity.PreventiveMaintenance;
import com.emms.backend.entity.Schedule;
import com.emms.backend.entity.enums.Priority;
import com.emms.backend.entity.enums.RecurrenceType;
import com.emms.backend.exception.CustomException;
import com.emms.backend.repository.PreventiveMaintenanceRepository;
import lombok.RequiredArgsConstructor;
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

    public PreventiveMaintenanceSummaryDTO create(PreventiveMaintenancePostDTO dto) {
        validateCreate(dto);

        PreventiveMaintenance pm = new PreventiveMaintenance();
        applyCreate(pm, dto);
        validateUniqueCode(pm.getCode());

        PreventiveMaintenance saved = repository.save(pm);

        if (saved.getSchedule() != null) {
            saved.getSchedule().setPreventiveMaintenance(saved);
        }

        return toSummaryDto(saved);
    }

    public PreventiveMaintenanceSummaryDTO update(Long id, PreventiveMaintenanceDTO dto) {
        if (id == null) {
            throw new CustomException("ID bắt buộc", HttpStatus.BAD_REQUEST);
        }

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
        repository.delete(findEntityById(id));
    }

    private PreventiveMaintenanceSummaryDTO toSummaryDto(PreventiveMaintenance pm) {
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

                pm.getPriority()
        );
    }

    private void applyCreate(PreventiveMaintenance pm, PreventiveMaintenancePostDTO dto) {
        pm.setTitle(dto.getTitle());
        pm.setDescription(dto.getDescription());
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

        if (dto.getEndsOn() != null) {
            schedule.setEndsOn(dto.getEndsOn().toLocalDate());
        }

        schedule.setFrequency(rule.getFrequency() != null ? rule.getFrequency() : 1);
        schedule.setDueDateDelay(rule.getDueDateDelay());
        schedule.setRecurrenceType(rule.getType() != null ? rule.getType() : RecurrenceType.DAILY);
        schedule.setRecurrenceBasedOn(rule.getBasedOn());
        schedule.setDaysOfWeek(rule.getDaysOfWeek());

        pm.setSchedule(schedule);
    }

    private void applyUpdate(PreventiveMaintenance pm, PreventiveMaintenanceDTO dto) {
        if (dto.getTitle() != null) pm.setTitle(dto.getTitle());
        if (dto.getDescription() != null) pm.setDescription(dto.getDescription());
        if (dto.getEstimatedHours() != null) pm.setEstimatedHours(dto.getEstimatedHours());
        if (dto.getPriority() != null) pm.setPriority(dto.getPriority());

        if (dto.getAssetId() != null) {
            pm.setAsset(assetService.getById(dto.getAssetId()));
        }

        if (dto.getAssignedToId() != null) {
            pm.setAssignedTo(userService.findEntityById(dto.getAssignedToId()));
        }

        if (pm.getSchedule() != null) {
            if (dto.getStartsOn() != null) {
                pm.getSchedule().setStartsOn(dto.getStartsOn().toLocalDate());
            }

            if (dto.getEndsOn() != null) {
                pm.getSchedule().setEndsOn(dto.getEndsOn().toLocalDate());
            }

            RecurrenceRule rule = dto.getRecurrenceRule();

            if (rule != null) {
                if (rule.getFrequency() != null) {
                    pm.getSchedule().setFrequency(rule.getFrequency());
                }

                if (rule.getDueDateDelay() != null) {
                    pm.getSchedule().setDueDateDelay(rule.getDueDateDelay());
                }

                if (rule.getType() != null) {
                    pm.getSchedule().setRecurrenceType(rule.getType());
                }

                if (rule.getBasedOn() != null) {
                    pm.getSchedule().setRecurrenceBasedOn(rule.getBasedOn());
                }

                if (rule.getDaysOfWeek() != null) {
                    pm.getSchedule().setDaysOfWeek(rule.getDaysOfWeek());
                }
            }
        }

        if (dto.getActive() != null) {
            pm.setActive(dto.getActive());

            if (pm.getSchedule() != null) {
                pm.getSchedule().setDisabled(!dto.getActive());
            }
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

        if (dto.getEndsOn() != null &&
                dto.getEndsOn().toLocalDate().isBefore(dto.getStartsOn().toLocalDate())) {
            throw new CustomException(
                    "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (dto.getRecurrenceRule() == null) {
            throw new CustomException("Quy tắc lặp lại là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        RecurrenceRule rule = dto.getRecurrenceRule();

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
}