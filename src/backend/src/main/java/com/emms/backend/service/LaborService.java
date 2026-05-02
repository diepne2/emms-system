package com.emms.backend.service;

import com.emms.backend.dto.labor.LaborCreateDTO;
import com.emms.backend.dto.labor.LaborPatchDTO;
import com.emms.backend.dto.labor.LaborShowDTO;
import com.emms.backend.entity.Labor;
import com.emms.backend.entity.TimeCategory;
import com.emms.backend.entity.User;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.enums.TimeStatus;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.LaborMapper;
import com.emms.backend.repository.LaborRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class LaborService {

    private final LaborRepository laborRepository;
    private final TimeCategoryService timeCategoryService;
    private final UserService userService;
    private final WorkOrderService workOrderService;
    private final LaborMapper laborMapper;

    public LaborService(
            LaborRepository laborRepository,
            TimeCategoryService timeCategoryService,
            UserService userService,
            WorkOrderService workOrderService,
            LaborMapper laborMapper
    ) {
        this.laborRepository = laborRepository;
        this.timeCategoryService = timeCategoryService;
        this.userService = userService;
        this.workOrderService = workOrderService;
        this.laborMapper = laborMapper;
    }

    @Transactional
    public LaborShowDTO create(LaborCreateDTO dto) {
        if (dto == null) {
            throw new CustomException("Dữ liệu lao động không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (dto.getWorkOrderId() == null) {
            throw new CustomException("ID đơn hàng công việc không được để trống", HttpStatus.BAD_REQUEST);
        }

        WorkOrder workOrder = workOrderService.findEntityByIdForLabor(dto.getWorkOrderId());

        TimeCategory timeCategory = null;
        if (dto.getTimeCategoryId() != null) {
            timeCategory = timeCategoryService.findEntityById(dto.getTimeCategoryId());
        }

        User assignedUser = workOrder.getAssignedTo();

        Labor labor = laborMapper.toEntity(dto, workOrder, assignedUser, timeCategory);

        updateHourlyRateIfNeeded(labor);

        Labor saved = laborRepository.saveAndFlush(labor);
        return laborMapper.toShowDTO(saved);
    }

    public List<LaborShowDTO> getMyLabors() {
        User currentUser = userService.whoami();

        if (currentUser == null || currentUser.getUserId() == null) {
            throw new CustomException("Người dùng hiện tại không được để trống", HttpStatus.UNAUTHORIZED);
        }

        return laborRepository.findAll()
                .stream()
                .filter(labor ->
                        labor.getAssignedTo() != null
                                && labor.getAssignedTo().getUserId() != null
                                && labor.getAssignedTo().getUserId().equals(currentUser.getUserId())
                )
                .map(laborMapper::toShowDTO)
                .toList();
    }

    public List<LaborShowDTO> getAll() {
        return laborRepository.findAll()
                .stream()
                .map(laborMapper::toShowDTO)
                .toList();
    }

    public LaborShowDTO getById(Long id) {
        return laborMapper.toShowDTO(findEntityById(id));
    }

    public List<LaborShowDTO> findByWorkOrder(Long workOrderId) {
        if (workOrderId == null) {
            throw new CustomException("ID đơn hàng công việc không được để trống", HttpStatus.BAD_REQUEST);
        }

        return laborRepository.findByWorkOrder_Id(workOrderId)
                .stream()
                .map(laborMapper::toShowDTO)
                .toList();
    }

    @Transactional
    public LaborShowDTO update(Long id, LaborPatchDTO dto) {
        if (id == null) {
            throw new CustomException("ID lao động không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (dto == null) {
            throw new CustomException("Dữ liệu lao động không được để trống", HttpStatus.BAD_REQUEST);
        }

        Labor labor = findEntityById(id);

        User assignedTo = null;
        if (dto.getAssignedToId() != null) {
            assignedTo = userService.findEntityById(dto.getAssignedToId());
        }

        TimeCategory timeCategory = null;
        if (dto.getTimeCategoryId() != null) {
            timeCategory = timeCategoryService.findEntityById(dto.getTimeCategoryId());
        }

        validatePatch(dto);

        laborMapper.updateLabor(labor, dto, assignedTo, timeCategory);

        updateHourlyRateIfNeeded(labor);

        Labor saved = laborRepository.saveAndFlush(labor);
        return laborMapper.toShowDTO(saved);
    }

    @Transactional
    public LaborShowDTO stop(Long id) {
        Labor labor = findEntityById(id);

        if (labor.getStartedAt() == null) {
            throw new CustomException("Thời gian bắt đầu lao động không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (labor.getDuration() == null || labor.getDuration() < 0L) {
            throw new CustomException("Thời lượng lao động không được âm", HttpStatus.BAD_REQUEST);
        }

        if (labor.getStatus() == TimeStatus.STOPPED) {
            return laborMapper.toShowDTO(labor);
        }

        long diffMillis = new Date().getTime() - labor.getStartedAt().getTime();
        long diffSeconds = TimeUnit.SECONDS.convert(diffMillis, TimeUnit.MILLISECONDS);

        labor.setStatus(TimeStatus.STOPPED);
        labor.setDuration(labor.getDuration() + Math.max(diffSeconds, 0L));

        Labor saved = laborRepository.saveAndFlush(labor);
        return laborMapper.toShowDTO(saved);
    }

    public Labor findEntityById(Long id) {
        if (id == null) {
            throw new CustomException("ID lao động không được để trống", HttpStatus.BAD_REQUEST);
        }

        return laborRepository.findById(id)
                .orElseThrow(() -> new CustomException("Lao động không tìm thấy", HttpStatus.NOT_FOUND));
    }

    private void validatePatch(LaborPatchDTO dto) {
        if (dto.getHourlyRate() != null && dto.getHourlyRate() < 0.0) {
            throw new CustomException("Tiền công mỗi giờ không được âm", HttpStatus.BAD_REQUEST);
        }

        if (dto.getDuration() != null && dto.getDuration() < 0L) {
            throw new CustomException("Thời lượng không được âm", HttpStatus.BAD_REQUEST);
        }
    }

    private void updateHourlyRateIfNeeded(Labor labor) {
        if (labor == null) return;

        if (labor.getHourlyRate() == null) {
            labor.setHourlyRate(0.0);
        }

        if (labor.getHourlyRate() <= 0.0 && labor.getTimeCategory() != null) {
            Double categoryRate = labor.getTimeCategory().getHourlyRate();
            if (categoryRate != null && categoryRate > 0.0) {
                labor.setHourlyRate(categoryRate);
            }
        }
    }
}