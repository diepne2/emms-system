package com.emms.backend.service;

import com.emms.backend.dto.woMeterTrigger.WorkOrderMeterTriggerDTO;
import com.emms.backend.entity.Meter;
import com.emms.backend.entity.WorkOrderMeterTrigger;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.WorkOrderMeterTriggerMapper;
import com.emms.backend.repository.WorkOrderMeterTriggerRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderMeterTriggerService {

    private final WorkOrderMeterTriggerRepository repo;
    private final WorkOrderMeterTriggerMapper mapper;
    private final MeterService meterService;
    private final EntityManager em;

    public WorkOrderMeterTrigger create(WorkOrderMeterTriggerDTO dto) {
        if (dto == null) {
            throw new CustomException("DTO is required", HttpStatus.BAD_REQUEST);
        }

        if (dto.getMeterId() == null) {
            throw new CustomException("Meter is required", HttpStatus.BAD_REQUEST);
        }

        WorkOrderMeterTrigger entity = new WorkOrderMeterTrigger();
        mapper.update(entity, dto);

        Meter meter = meterService.findEntityById(dto.getMeterId());
        entity.setMeter(meter);

        normalize(entity);
        validate(entity);

        WorkOrderMeterTrigger saved = repo.saveAndFlush(entity);
        em.refresh(saved);
        return saved;
    }

    public WorkOrderMeterTrigger update(Long id, WorkOrderMeterTriggerDTO dto) {
        if (id == null) {
            throw new CustomException("Id is required", HttpStatus.BAD_REQUEST);
        }

        if (dto == null) {
            throw new CustomException("DTO is required", HttpStatus.BAD_REQUEST);
        }

        WorkOrderMeterTrigger entity = findEntityById(id);
        mapper.update(entity, dto);

        if (dto.getMeterId() != null) {
            Meter meter = meterService.findEntityById(dto.getMeterId());
            entity.setMeter(meter);
        }

        normalize(entity);
        validate(entity);

        WorkOrderMeterTrigger saved = repo.saveAndFlush(entity);
        em.refresh(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public Collection<WorkOrderMeterTrigger> getAll() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public WorkOrderMeterTrigger findEntityById(Long id) {
        if (id == null) {
            throw new CustomException("Id is required", HttpStatus.BAD_REQUEST);
        }

        return repo.findById(id)
                .orElseThrow(() -> new CustomException("Not found", HttpStatus.NOT_FOUND));
    }

    public void delete(Long id) {
        if (id == null) {
            throw new CustomException("Id is required", HttpStatus.BAD_REQUEST);
        }

        repo.delete(findEntityById(id));
    }

    private void validate(WorkOrderMeterTrigger e) {
        if (e.getName() == null || e.getName().isBlank()) {
            throw new CustomException("Name required", HttpStatus.BAD_REQUEST);
        }

        if (e.getTriggerCondition() == null) {
            throw new CustomException("Condition required", HttpStatus.BAD_REQUEST);
        }

        if (e.getTriggerValue() == null) {
            throw new CustomException("Trigger value required", HttpStatus.BAD_REQUEST);
        }

        if (e.getTriggerValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("Trigger value must be >= 0", HttpStatus.BAD_REQUEST);
        }

        if (e.getCooldownMinutes() == null || e.getCooldownMinutes() < 0) {
            throw new CustomException("Cooldown must be >= 0", HttpStatus.BAD_REQUEST);
        }

        if (e.getMeter() == null || e.getMeter().getId() == null) {
            throw new CustomException("Meter required", HttpStatus.BAD_REQUEST);
        }
    }

    private void normalize(WorkOrderMeterTrigger e) {
        if (e.getName() != null) {
            e.setName(e.getName().trim());
        }

        if (e.getTriggerValue() == null) {
            e.setTriggerValue(BigDecimal.ZERO);
        }

        if (e.getCooldownMinutes() == null || e.getCooldownMinutes() < 0) {
            e.setCooldownMinutes(0);
        }
    }
}