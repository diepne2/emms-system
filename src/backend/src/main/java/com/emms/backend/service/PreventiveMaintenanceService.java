package com.emms.backend.service;

import com.emms.backend.dto.importData.PreventiveMaintenanceImportDTO;
import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenanceDTO;
import com.emms.backend.dto.preventiveMaintenance.PreventiveMaintenancePostDTO;
import com.emms.backend.entity.Asset;
import com.emms.backend.entity.PreventiveMaintenance;
import com.emms.backend.entity.Schedule;
import com.emms.backend.entity.User;
import com.emms.backend.entity.enums.RecurrenceType;
import com.emms.backend.exception.CustomException;
import com.emms.backend.repository.PreventiveMaintenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PreventiveMaintenanceService {

    private final PreventiveMaintenanceRepository preventiveMaintenanceRepository;
    private final UserService userService;
    private final AssetService assetService;
    private final ScheduleService scheduleService;
    private final LocationService locationService;
    private final WorkOrderCategoryService workOrderCategoryService;

    public PreventiveMaintenance create(PreventiveMaintenancePostDTO dto) {
        validateCreate(dto);

        PreventiveMaintenance pm = new PreventiveMaintenance();
        applyPostDto(pm, dto);

        validateUniqueCodeForCreate(pm.getCode());

        PreventiveMaintenance saved = preventiveMaintenanceRepository.save(pm);

        if (saved.getSchedule() != null) {
            saved.getSchedule().setPreventiveMaintenance(saved);
        }

        return saved;
    }

    public PreventiveMaintenance createEntity(PreventiveMaintenance entity) {
        if (entity == null) {
            throw new CustomException(
                    "Dữ liệu kế hoạch bảo trì không được để trống",
                    HttpStatus.BAD_REQUEST
            );
        }

        validateUniqueCodeForCreate(entity.getCode());
        return preventiveMaintenanceRepository.save(entity);
    }

    public PreventiveMaintenance update(Long id, PreventiveMaintenanceDTO dto) {
        validateUpdatePayload(id, dto);

        PreventiveMaintenance saved = findEntityById(id);

        applyUpdateDto(saved, dto);
        validateUniqueCodeForUpdate(saved.getCode(), saved.getId());

        return preventiveMaintenanceRepository.save(saved);
    }

    @Transactional(readOnly = true)
    public Collection<PreventiveMaintenance> getAll() {
        return preventiveMaintenanceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PreventiveMaintenance> getAllForExport() {
        return preventiveMaintenanceRepository.findAllForExport();
    }

    @Transactional(readOnly = true)
    public PreventiveMaintenance getById(Long id) {
        return findEntityById(id);
    }

    @Transactional(readOnly = true)
    public PreventiveMaintenance findEntityById(Long id) {
        if (id == null) {
            throw new CustomException("id không được để trống", HttpStatus.BAD_REQUEST);
        }

        return preventiveMaintenanceRepository.findById(id)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy kế hoạch bảo trì với id: " + id,
                        HttpStatus.NOT_FOUND
                ));
    }

    @Transactional(readOnly = true)
    public Optional<PreventiveMaintenance> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return preventiveMaintenanceRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<PreventiveMaintenance> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException("Danh sách id không được để trống", HttpStatus.BAD_REQUEST);
        }
        return preventiveMaintenanceRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public Collection<PreventiveMaintenance> findByActive(boolean active) {
        return preventiveMaintenanceRepository.findByActive(active);
    }

    @Transactional(readOnly = true)
    public Collection<PreventiveMaintenance> findByDemo(boolean demo) {
        return preventiveMaintenanceRepository.findByDemo(demo);
    }

    @Transactional(readOnly = true)
    public List<PreventiveMaintenance> findCreatedBefore(LocalDateTime before) {
        if (before == null) {
            throw new CustomException("before không được để trống", HttpStatus.BAD_REQUEST);
        }
        return preventiveMaintenanceRepository.findByCreatedAtBefore(before);
    }

    @Transactional(readOnly = true)
    public List<PreventiveMaintenance> findDueBefore(LocalDateTime before) {
        if (before == null) {
            throw new CustomException("before không được để trống", HttpStatus.BAD_REQUEST);
        }
        return preventiveMaintenanceRepository.findByDueDateBefore(before);
    }

    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        return preventiveMaintenanceRepository.existsByCodeIgnoreCase(code.trim());
    }

    @Transactional(readOnly = true)
    public boolean hasMoreThan(long threshold) {
        if (threshold < 0) {
            throw new CustomException("threshold không được âm", HttpStatus.BAD_REQUEST);
        }
        return preventiveMaintenanceRepository.hasMoreThan(threshold);
    }

    public void delete(Long id) {
        PreventiveMaintenance entity = findEntityById(id);
        preventiveMaintenanceRepository.delete(entity);
    }

    public void deleteDemoData() {
        preventiveMaintenanceRepository.deleteByDemoTrue();
    }

    public List<PreventiveMaintenance> saveAll(List<PreventiveMaintenance> items) {
        if (items == null || items.isEmpty()) {
            throw new CustomException(
                    "Danh sách preventive maintenance không được để trống",
                    HttpStatus.BAD_REQUEST
            );
        }
        return preventiveMaintenanceRepository.saveAll(items);
    }

    private void applyPostDto(PreventiveMaintenance pm, PreventiveMaintenancePostDTO dto) {
        pm.setTitle(dto.getTitle());
        pm.setDescription(dto.getDescription());
        pm.setEstimatedHours(dto.getEstimatedHours());
        pm.setActive(true);

        if (dto.getPriority() != null) {
            pm.setPriority(dto.getPriority());
        }

        if (dto.getAssetId() != null) {
            Asset asset = assetService.findEntityById(dto.getAssetId());
            pm.setAsset(asset);
        } else {
            pm.setAsset(null);
        }

        if (dto.getAssignedToId() != null) {
            User assignedTo = userService.findEntityById(dto.getAssignedToId());
            pm.setAssignedTo(assignedTo);
        } else {
            pm.setAssignedTo(null);
        }

        Schedule schedule = pm.getSchedule();
        if (schedule == null) {
            schedule = new Schedule();
        }

        schedule.setPreventiveMaintenance(pm);
        schedule.setDisabled(false);
        schedule.setStartsOn(toLocalDate(dto.getStartsOn()));
        schedule.setEndsOn(dto.getEndsOn() == null ? null : dto.getEndsOn().toLocalDate());
        schedule.setFrequency(dto.getFrequency());
        schedule.setDueDateDelay(dto.getDueDateDelay());
        schedule.setRecurrenceType(dto.getRecurrenceType());
        schedule.setRecurrenceBasedOn(dto.getRecurrenceBasedOn());
        schedule.setDaysOfWeek(dto.getDaysOfWeek());

        pm.setSchedule(schedule);
    }

    private void applyUpdateDto(PreventiveMaintenance pm, PreventiveMaintenanceDTO dto) {
        if (dto.getTitle() != null) {
            pm.setTitle(dto.getTitle());
        }

        if (dto.getDescription() != null) {
            pm.setDescription(dto.getDescription());
        }

        if (dto.getEstimatedHours() != null) {
            if (dto.getEstimatedHours() < 0) {
                throw new CustomException("estimatedHours không được âm", HttpStatus.BAD_REQUEST);
            }
            pm.setEstimatedHours(dto.getEstimatedHours());
        }

        if (dto.getCode() != null) {
            pm.setCode(dto.getCode());
        }

        if (dto.getPriority() != null) {
            pm.setPriority(dto.getPriority());
        }

        if (dto.getDueDate() != null) {
            pm.setDueDate(dto.getDueDate().atStartOfDay());
        }

        if (dto.getRequiresSignature() != null) {
            pm.setRequiredSignature(dto.getRequiresSignature());
        }

        if (dto.getAssignedToId() != null) {
            User assignedUser = userService.findEntityById(dto.getAssignedToId());
            pm.setAssignedTo(assignedUser);
        }

        if (dto.getAssetId() != null) {
            Asset asset = assetService.findEntityById(dto.getAssetId());
            pm.setAsset(asset);
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
            throw new CustomException(
                    "Dữ liệu kế hoạch bảo trì không được để trống",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new CustomException("title không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (dto.getEstimatedHours() != null && dto.getEstimatedHours() < 0) {
            throw new CustomException("estimatedHours không được âm", HttpStatus.BAD_REQUEST);
        }

        if (dto.getFrequency() == null || dto.getFrequency() < 1) {
            throw new CustomException("frequency phải >= 1", HttpStatus.BAD_REQUEST);
        }

        if (dto.getDueDateDelay() != null && dto.getDueDateDelay() < 0) {
            throw new CustomException("dueDateDelay không được âm", HttpStatus.BAD_REQUEST);
        }

        LocalDate startsOn = toLocalDate(dto.getStartsOn());
        LocalDate endsOn = dto.getEndsOn() == null ? null : dto.getEndsOn().toLocalDate();

        if (endsOn != null && endsOn.isBefore(startsOn)) {
            throw new CustomException("endsOn phải >= startsOn", HttpStatus.BAD_REQUEST);
        }

        if (dto.getRecurrenceType() == RecurrenceType.WEEKLY
                && (dto.getDaysOfWeek() == null || dto.getDaysOfWeek().isEmpty())) {
            throw new CustomException(
                    "daysOfWeek không được để trống khi recurrenceType = WEEKLY",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validateUpdatePayload(Long id, PreventiveMaintenanceDTO dto) {
        if (id == null) {
            throw new CustomException("id không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (dto == null) {
            throw new CustomException("Dữ liệu cập nhật không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (dto.getEstimatedHours() != null && dto.getEstimatedHours() < 0) {
            throw new CustomException("estimatedHours không được âm", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateUniqueCodeForCreate(String code) {
        if (code == null || code.trim().isEmpty()) {
            return;
        }

        if (preventiveMaintenanceRepository.existsByCodeIgnoreCase(code.trim())) {
            throw new CustomException("Mã kế hoạch bảo trì đã tồn tại", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateUniqueCodeForUpdate(String code, Long currentId) {
        if (code == null || code.trim().isEmpty()) {
            return;
        }

        Optional<PreventiveMaintenance> existed =
                preventiveMaintenanceRepository.findByCodeIgnoreCase(code.trim());

        if (existed.isPresent() && !existed.get().getId().equals(currentId)) {
            throw new CustomException("Mã kế hoạch bảo trì đã tồn tại", HttpStatus.BAD_REQUEST);
        }
    }

    private LocalDate toLocalDate(LocalDateTime value) {
        return value == null ? LocalDate.now() : value.toLocalDate();
    }

    public void importPreventiveMaintenance(
            PreventiveMaintenance entity,
            PreventiveMaintenanceImportDTO dto
    ) {
        if (entity == null || dto == null) {
            throw new CustomException("Dữ liệu import không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        preventiveMaintenanceRepository.save(entity);
    }
}