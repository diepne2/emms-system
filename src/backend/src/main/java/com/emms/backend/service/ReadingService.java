package com.emms.backend.service;

import com.emms.backend.dto.reading.ReadingDTO;
import com.emms.backend.dto.reading.ReadingShowDTO;
import com.emms.backend.entity.Meter;
import com.emms.backend.entity.Reading;
import com.emms.backend.entity.User;
import com.emms.backend.entity.WorkOrderMeterTrigger;
import com.emms.backend.exception.CustomException;
import com.emms.backend.mapper.ReadingMapper;
import com.emms.backend.repository.ReadingRepository;
import com.emms.backend.repository.WorkOrderMeterTriggerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReadingService {

    private final ReadingRepository readingRepository;
    private final ReadingMapper readingMapper;
    private final MeterService meterService;
    private final WorkOrderMeterTriggerRepository triggerRepository;
    private final WorkOrderAutomationService workOrderAutomationService;
    private final NotificationService notificationService;
    private final WorkOrderHistoryService workOrderHistoryService;

    public ReadingService(
            ReadingRepository readingRepository,
            ReadingMapper readingMapper,
            MeterService meterService,
            WorkOrderMeterTriggerRepository triggerRepository,
            WorkOrderAutomationService workOrderAutomationService,
            NotificationService notificationService,
            WorkOrderHistoryService workOrderHistoryService
    ) {
        this.readingRepository = readingRepository;
        this.readingMapper = readingMapper;
        this.meterService = meterService;
        this.triggerRepository = triggerRepository;
        this.workOrderAutomationService = workOrderAutomationService;
        this.notificationService = notificationService;
        this.workOrderHistoryService = workOrderHistoryService;
    }


    public ReadingShowDTO create(ReadingDTO dto) {
        validateCreate(dto);

        Meter meter = meterService.findEntityById(dto.getMeterId());

        Reading entity = readingMapper.toEntity(dto);
        entity.setMeter(meter);

        if (entity.getRecordedAt() == null) {
            entity.setRecordedAt(LocalDateTime.now());
        }

        validateMonotonicReading(meter.getId(), entity.getValue(), entity.getRecordedAt());
        fillDeltaValue(entity);

        Reading saved = readingRepository.save(entity);

        evaluateTriggersAndCreateWorkOrderIfNeeded(saved);

        return readingMapper.toShowDto(saved);
    }


    @Transactional(readOnly = true)
    public Optional<Reading> findLatestByMeter(Long meterId) {
        if (meterId == null) return Optional.empty();
        return readingRepository.findTopByMeterIdOrderByRecordedAtDescIdDesc(meterId);
    }

    @Transactional(readOnly = true)
    public List<ReadingShowDTO> getByMeter(Long meterId) {
        if (meterId == null) {
            throw new CustomException("ID máy đo là bắt buộc", HttpStatus.BAD_REQUEST);
        }

        return readingRepository.findByMeterIdOrderByRecordedAtDescIdDesc(meterId)
                .stream()
                .map(readingMapper::toShowDto)
                .collect(Collectors.toList());
    }

    // ================= VALIDATION =================

    private void validateCreate(ReadingDTO dto) {
        if (dto == null) throw new CustomException("Reading data must not be null", HttpStatus.BAD_REQUEST);
        if (dto.getMeterId() == null) throw new CustomException("ID máy đo là bắt buộc", HttpStatus.BAD_REQUEST);
        if (dto.getValue() == null) throw new CustomException("Giá trị đọc là bắt buộc", HttpStatus.BAD_REQUEST);
        if (dto.getValue().compareTo(BigDecimal.ZERO) < 0)
            throw new CustomException("Giá trị đọc phải lớn hơn hoặc bằng 0", HttpStatus.BAD_REQUEST);
    }

    private void validateMonotonicReading(Long meterId, BigDecimal currentValue, LocalDateTime recordedAt) {
        Optional<Reading> latestBefore = readingRepository
                .findTopByMeterIdAndRecordedAtBeforeOrderByRecordedAtDescIdDesc(
                        meterId,
                        recordedAt.plusNanos(1)
                );

        if (latestBefore.isPresent()) {
            BigDecimal previous = latestBefore.get().getValue();
            if (previous != null && currentValue.compareTo(previous) < 0) {
                throw new CustomException(
                        "Giá trị đọc phải lớn hơn hoặc bằng giá trị đọc trước (" + previous + ")",
                        HttpStatus.BAD_REQUEST
                );
            }
        }
    }


    private void fillDeltaValue(Reading entity) {
        Optional<Reading> latestBefore = readingRepository
                .findTopByMeterIdAndRecordedAtBeforeOrderByRecordedAtDescIdDesc(
                        entity.getMeter().getId(),
                        entity.getRecordedAt().plusNanos(1)
                );

        if (latestBefore.isPresent() && latestBefore.get().getValue() != null) {
            entity.setDeltaValue(entity.getValue().subtract(latestBefore.get().getValue()));
        } else {
            entity.setDeltaValue(BigDecimal.ZERO);
        }
    }



    private void evaluateTriggersAndCreateWorkOrderIfNeeded(Reading reading) {

        List<WorkOrderMeterTrigger> triggers =
                triggerRepository.findByMeterIdAndActiveTrue(reading.getMeter().getId());

        if (triggers == null || triggers.isEmpty()) return;

        for (WorkOrderMeterTrigger trigger : triggers) {

            if (!isCooldownSatisfied(trigger, reading.getRecordedAt())) continue;
            if (!isConditionMatched(trigger, reading)) continue;

            Long workOrderId = workOrderAutomationService.createFromMeterTrigger(trigger, reading);

            if (workOrderId == null) continue;

            // update trigger
            trigger.setLastTriggeredAt(reading.getRecordedAt());
            if (!trigger.isRecurrent()) {
                trigger.setActive(false);
            }
            triggerRepository.save(trigger); 

            // update reading
            reading.setTriggered(true);
            reading.setTriggeredWorkOrderId(workOrderId);
            readingRepository.save(reading);

            saveHistory(trigger, reading, workOrderId);
            sendNotification(trigger, reading, workOrderId);

            break;
        }
    }

    private boolean isCooldownSatisfied(WorkOrderMeterTrigger trigger, LocalDateTime time) {
        if (trigger.getCooldownMinutes() == null || trigger.getCooldownMinutes() <= 0) return true;
        if (trigger.getLastTriggeredAt() == null) return true;

        return !time.isBefore(trigger.getLastTriggeredAt().plusMinutes(trigger.getCooldownMinutes()));
    }

    private boolean isConditionMatched(WorkOrderMeterTrigger trigger, Reading reading) {

        if (trigger.getTriggerCondition() == null || trigger.getTriggerValue() == null) return false;

        BigDecimal value = reading.getValue();
        BigDecimal triggerValue = trigger.getTriggerValue();

        if (value == null) return false;

        return switch (trigger.getTriggerCondition()) {

            case GREATER_THAN -> value.compareTo(triggerValue) > 0;
            case GREATER_THAN_OR_EQUAL -> value.compareTo(triggerValue) >= 0;
            case LESS_THAN -> value.compareTo(triggerValue) < 0;
            case LESS_THAN_OR_EQUAL -> value.compareTo(triggerValue) <= 0;
            case EQUAL -> value.compareTo(triggerValue) == 0;

            case DELTA_GREATER_THAN ->
                    reading.getDeltaValue() != null &&
                            reading.getDeltaValue().compareTo(triggerValue) > 0;

            case DELTA_GREATER_THAN_OR_EQUAL ->
                    reading.getDeltaValue() != null &&
                            reading.getDeltaValue().compareTo(triggerValue) >= 0;

            default -> false;
        };
    }



    private void saveHistory(WorkOrderMeterTrigger trigger, Reading reading, Long workOrderId) {
        try {
            workOrderHistoryService.saveSystemHistory(
                    workOrderId,
                    "AUTO_CREATED_FROM_METER_TRIGGER",
                    "Trigger [" + trigger.getName() + "] | value=" + reading.getValue()
                            + " | delta=" + reading.getDeltaValue()
            );
        } catch (Exception ignored) {}
    }


    private void sendNotification(WorkOrderMeterTrigger trigger, Reading reading, Long workOrderId) {
        try {
            Long userId = resolveNotificationUserId(reading);
            if (userId == null) return;

            notificationService.createNotificationIfUserExists(
                    userId,
                    "Auto Work Order Created",
                    "WO #" + workOrderId + " created from trigger [" + trigger.getName() + "]"
            );
        } catch (Exception ignored) {}
    }

    private Long resolveNotificationUserId(Reading reading) {
        if (reading == null || reading.getMeter() == null) return null;

        if (reading.getMeter().getUsers() != null && !reading.getMeter().getUsers().isEmpty()) {
            User u = reading.getMeter().getUsers().get(0);
            if (u != null) return u.getUserId();
        }
        return null;
    }
}