package com.emms.backend.service;

import com.emms.backend.dto.labor.LaborPatchDTO;
import com.emms.backend.entity.Labor;
import com.emms.backend.entity.TimeCategory;
import com.emms.backend.entity.User;
import com.emms.backend.entity.enums.TimeStatus;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.LaborMapper;
import com.emms.backend.repository.LaborRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LaborService {

    private final LaborRepository laborRepository;
    private final TimeCategoryService timeCategoryService;
    private final UserService userService;
    private final LaborMapper laborMapper;

    @Transactional
    public Labor create(Labor labor) {
        if (labor == null) {
            throw new CustomException("Labor must not be null", HttpStatus.BAD_REQUEST);
        }

        updateHourlyRateIfNeeded(labor);
        return laborRepository.saveAndFlush(labor);
    }

    @Transactional
    public Labor update(Long id, LaborPatchDTO dto) {
        if (id == null) {
            throw new CustomException("Labor id must not be null", HttpStatus.BAD_REQUEST);
        }
        if (dto == null) {
            throw new CustomException("Labor data must not be null", HttpStatus.BAD_REQUEST);
        }

        Labor savedLabor = laborRepository.findById(id)
                .orElseThrow(() -> new CustomException("Labor not found", HttpStatus.NOT_FOUND));

        validatePatch(dto);
        laborMapper.updateLabor(savedLabor, dto);
        applyRelations(savedLabor, dto);
        updateHourlyRateIfNeeded(savedLabor);

        return laborRepository.saveAndFlush(savedLabor);
    }

    private void applyRelations(Labor labor, LaborPatchDTO dto) {
        if (dto.getAssignedToId() != null) {
            User assignedTo = userService.findEntityById(dto.getAssignedToId());
            labor.setAssignedTo(assignedTo);
        }

        if (dto.getTimeCategoryId() != null) {
            TimeCategory timeCategory = timeCategoryService.findEntityById(dto.getTimeCategoryId());
            labor.setTimeCategory(timeCategory);
        }
    }

    private void validatePatch(LaborPatchDTO dto) {
        if (dto.getHourlyRate() != null && dto.getHourlyRate() < 0L) {
            throw new CustomException("Hourly rate must not be negative", HttpStatus.BAD_REQUEST);
        }

        if (dto.getDuration() != null && dto.getDuration() < 0L) {
            throw new CustomException("Duration must not be negative", HttpStatus.BAD_REQUEST);
        }
    }

    private void updateHourlyRateIfNeeded(Labor labor) {
        if (labor == null) {
            return;
        }

        if (labor.getHourlyRate() == null) {
            labor.setHourlyRate(0L);
        }

        if (labor.getHourlyRate() <= 0L && labor.getTimeCategory() != null) {
            Long categoryRate = labor.getTimeCategory().getHourlyRate();
            if (categoryRate != null && categoryRate > 0L) {
                labor.setHourlyRate(categoryRate);
            }
        }
    }

    public Labor save(Labor labor) {
        if (labor == null) {
            throw new CustomException("Labor must not be null", HttpStatus.BAD_REQUEST);
        }
        return laborRepository.save(labor);
    }

    public Collection<Labor> getAll() {
        return laborRepository.findAll();
    }

    public void delete(Long id) {
        Labor labor = findEntityById(id);
        laborRepository.delete(labor);
    }

    public Optional<Labor> findById(Long id) {
        if (id == null) {
            throw new CustomException("Labor id must not be null", HttpStatus.BAD_REQUEST);
        }
        return laborRepository.findById(id);
    }

    public Labor findEntityById(Long id) {
        if (id == null) {
            throw new CustomException("Labor id must not be null", HttpStatus.BAD_REQUEST);
        }

        return laborRepository.findById(id)
                .orElseThrow(() -> new CustomException("Labor not found", HttpStatus.NOT_FOUND));
    }

    public Collection<Labor> findByWorkOrder(Long id) {
        if (id == null) {
            throw new CustomException("Work order id must not be null", HttpStatus.BAD_REQUEST);
        }
        return laborRepository.findByWorkOrder_WorkOrderId(id);
    }

    public Labor stop(Labor labor) {
        if (labor == null) {
            throw new CustomException("Labor must not be null", HttpStatus.BAD_REQUEST);
        }
        if (labor.getStartedAt() == null) {
            throw new CustomException("Labor startedAt must not be null", HttpStatus.BAD_REQUEST);
        }
        if (labor.getDuration() < 0L) {
            throw new CustomException("Labor duration must not be negative", HttpStatus.BAD_REQUEST);
        }
        if (labor.getStatus() == TimeStatus.STOPPED) {
            return labor;
        }

        long diffMillis = new Date().getTime() - labor.getStartedAt().getTime();
        long diffSeconds = TimeUnit.SECONDS.convert(diffMillis, TimeUnit.MILLISECONDS);

        labor.setStatus(TimeStatus.STOPPED);
        labor.setDuration(labor.getDuration() + Math.max(diffSeconds, 0L));

        return save(labor);
    }
}