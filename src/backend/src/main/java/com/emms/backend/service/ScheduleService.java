package com.emms.backend.service;

import com.emms.backend.dto.schedule.ScheduleDTO;
import com.emms.backend.entity.PreventiveMaintenance;
import com.emms.backend.entity.Schedule;
import com.emms.backend.entity.enums.RecurrenceBasedOn;
import com.emms.backend.entity.enums.RecurrenceType;
import com.emms.backend.exception.CustomException;
import com.emms.backend.repository.PreventiveMaintenanceRepository;
import com.emms.backend.repository.ScheduleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final PreventiveMaintenanceRepository preventiveMaintenanceRepository;

    public ScheduleService(
            ScheduleRepository scheduleRepository,
            PreventiveMaintenanceRepository preventiveMaintenanceRepository
    ) {
        this.scheduleRepository = scheduleRepository;
        this.preventiveMaintenanceRepository = preventiveMaintenanceRepository;
    }

    public Schedule create(Long preventiveMaintenanceId, ScheduleDTO dto) {
        if (preventiveMaintenanceId == null) {
            throw new CustomException("preventiveMaintenanceId không được để trống", HttpStatus.BAD_REQUEST);
        }

        validateScheduleDTO(dto);

        PreventiveMaintenance preventiveMaintenance = preventiveMaintenanceRepository.findById(preventiveMaintenanceId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy preventive maintenance với id: " + preventiveMaintenanceId,
                        HttpStatus.NOT_FOUND
                ));


        if (scheduleRepository.findByPreventiveMaintenance_Id(preventiveMaintenanceId).isPresent()) {
            throw new CustomException(
                    "Preventive maintenance này đã có schedule",
                    HttpStatus.BAD_REQUEST
            );
        }

        Schedule schedule = new Schedule();
        schedule.setPreventiveMaintenance(preventiveMaintenance);
        applyDtoToEntity(schedule, dto);

        return scheduleRepository.save(schedule);
    }

    public Schedule update(Long scheduleId, ScheduleDTO dto) {
        if (scheduleId == null) {
            throw new CustomException("scheduleId không được để trống", HttpStatus.BAD_REQUEST);
        }

        validateScheduleDTO(dto);

        Schedule savedSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy schedule với id: " + scheduleId,
                        HttpStatus.NOT_FOUND
                ));

        applyDtoToEntity(savedSchedule, dto);

        return scheduleRepository.save(savedSchedule);
    }

    @Transactional(readOnly = true)
    public Collection<Schedule> getAll() {
        return scheduleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Schedule getById(Long scheduleId) {
        if (scheduleId == null) {
            throw new CustomException("scheduleId không được để trống", HttpStatus.BAD_REQUEST);
        }

        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy schedule với id: " + scheduleId,
                        HttpStatus.NOT_FOUND
                ));
    }

    @Transactional(readOnly = true)
    public Schedule getByPreventiveMaintenanceId(Long preventiveMaintenanceId) {
        if (preventiveMaintenanceId == null) {
            throw new CustomException("preventiveMaintenanceId không được để trống", HttpStatus.BAD_REQUEST);
        }


        return scheduleRepository.findByPreventiveMaintenance_Id(preventiveMaintenanceId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy schedule cho preventive maintenance id: " + preventiveMaintenanceId,
                        HttpStatus.NOT_FOUND
                ));
    }

    public void delete(Long scheduleId) {
        if (scheduleId == null) {
            throw new CustomException("scheduleId không được để trống", HttpStatus.BAD_REQUEST);
        }

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy schedule với id: " + scheduleId,
                        HttpStatus.NOT_FOUND
                ));

        scheduleRepository.delete(schedule);
    }

    public Schedule enable(Long scheduleId) {
        Schedule schedule = getById(scheduleId);
        schedule.setDisabled(false);
        return scheduleRepository.save(schedule);
    }

    public Schedule disable(Long scheduleId) {
        Schedule schedule = getById(scheduleId);
        schedule.setDisabled(true);
        return scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public boolean isActiveOnDate(Long scheduleId, LocalDate date) {
        if (date == null) {
            throw new CustomException("date không được để trống", HttpStatus.BAD_REQUEST);
        }

        Schedule schedule = getById(scheduleId);
        return schedule.isActiveOn(date);
    }

    @Transactional(readOnly = true)
    public boolean shouldGenerateOnDate(Schedule schedule, LocalDate date) {
        if (schedule == null) {
            throw new CustomException("schedule không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (date == null) {
            throw new CustomException("date không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (!schedule.isActiveOn(date)) {
            return false;
        }

        LocalDate startsOn = schedule.getStartsOn();
        int frequency = schedule.getFrequency() == null ? 1 : schedule.getFrequency();
        RecurrenceType recurrenceType = schedule.getRecurrenceType();
        RecurrenceBasedOn recurrenceBasedOn = schedule.getRecurrenceBasedOn();

        if (recurrenceBasedOn == RecurrenceBasedOn.COMPLETED_DATE) {
            return true;
        }

        if (recurrenceType == null) {
            recurrenceType = RecurrenceType.DAILY;
        }

        return switch (recurrenceType) {
            case DAILY -> isDailyMatch(startsOn, date, frequency);
            case WEEKLY -> isWeeklyMatch(schedule, startsOn, date, frequency);
            case MONTHLY -> isMonthlyMatch(startsOn, date, frequency);
            case YEARLY -> isYearlyMatch(startsOn, date, frequency);
        };
    }

    private void applyDtoToEntity(Schedule schedule, ScheduleDTO dto) {
        schedule.setDisabled(Boolean.TRUE.equals(dto.getDisabled()));
        schedule.setStartsOn(dto.getStartsOn() == null ? LocalDate.now() : dto.getStartsOn());
        schedule.setFrequency(dto.getFrequency() == null ? 1 : dto.getFrequency());
        schedule.setEndsOn(dto.getEndsOn());
        schedule.setDueDateDelay(dto.getDueDateDelay());
        schedule.setRecurrenceType(dto.getRecurrenceType() == null ? RecurrenceType.DAILY : dto.getRecurrenceType());
        schedule.setRecurrenceBasedOn(
                dto.getRecurrenceBasedOn() == null
                        ? RecurrenceBasedOn.SCHEDULED_DATE
                        : dto.getRecurrenceBasedOn()
        );
        schedule.setDaysOfWeek(normalizeDaysOfWeek(dto.getDaysOfWeek()));

        validateBusinessRules(schedule);
    }

    private void validateScheduleDTO(ScheduleDTO dto) {
        if (dto == null) {
            throw new CustomException("ScheduleDTO không được để trống", HttpStatus.BAD_REQUEST);
        }

        if (dto.getFrequency() != null && dto.getFrequency() < 1) {
            throw new CustomException("frequency phải >= 1", HttpStatus.BAD_REQUEST);
        }

        if (dto.getDueDateDelay() != null && dto.getDueDateDelay() < 0) {
            throw new CustomException("dueDateDelay không được âm", HttpStatus.BAD_REQUEST);
        }

        if (dto.getStartsOn() != null && dto.getEndsOn() != null && dto.getEndsOn().isBefore(dto.getStartsOn())) {
            throw new CustomException("endsOn phải >= startsOn", HttpStatus.BAD_REQUEST);
        }

        if (dto.getRecurrenceType() == RecurrenceType.WEEKLY) {
            List<Integer> days = dto.getDaysOfWeek();
            if (days == null || days.isEmpty()) {
                throw new CustomException(
                        "daysOfWeek không được để trống khi recurrenceType = WEEKLY",
                        HttpStatus.BAD_REQUEST
                );
            }
        }
    }

    private void validateBusinessRules(Schedule schedule) {
        if (schedule.getRecurrenceType() == RecurrenceType.WEEKLY) {
            if (schedule.getDaysOfWeek() == null || schedule.getDaysOfWeek().isEmpty()) {
                throw new CustomException(
                        "daysOfWeek không được để trống khi recurrenceType = WEEKLY",
                        HttpStatus.BAD_REQUEST
                );
            }
        } else {
            schedule.setDaysOfWeek(new ArrayList<>());
        }

        if (schedule.getEndsOn() != null && schedule.getEndsOn().isBefore(schedule.getStartsOn())) {
            throw new CustomException("endsOn phải >= startsOn", HttpStatus.BAD_REQUEST);
        }
    }

    private List<Integer> normalizeDaysOfWeek(List<Integer> daysOfWeek) {
        List<Integer> normalized = new ArrayList<>();

        if (daysOfWeek == null) {
            return normalized;
        }

        for (Integer day : daysOfWeek) {
            if (day == null) {
                continue;
            }
            if (day < 1 || day > 7) {
                throw new CustomException("daysOfWeek chỉ nhận giá trị từ 1 đến 7", HttpStatus.BAD_REQUEST);
            }
            if (!normalized.contains(day)) {
                normalized.add(day);
            }
        }

        return normalized;
    }

    private boolean isDailyMatch(LocalDate startsOn, LocalDate date, int frequency) {
        long daysBetween = ChronoUnit.DAYS.between(startsOn, date);
        return daysBetween >= 0 && daysBetween % frequency == 0;
    }

    private boolean isWeeklyMatch(Schedule schedule, LocalDate startsOn, LocalDate date, int frequency) {
        if (schedule.getDaysOfWeek() == null || schedule.getDaysOfWeek().isEmpty()) {
            return false;
        }

        long daysBetween = ChronoUnit.DAYS.between(startsOn, date);
        if (daysBetween < 0) {
            return false;
        }

        long weeksBetween = daysBetween / 7;
        if (weeksBetween % frequency != 0) {
            return false;
        }

        int dayValue = date.getDayOfWeek().getValue();
        return schedule.getDaysOfWeek().contains(dayValue);
    }

    private boolean isMonthlyMatch(LocalDate startsOn, LocalDate date, int frequency) {
        if (date.getDayOfMonth() != startsOn.getDayOfMonth()) {
            return false;
        }

        int monthsBetween = (date.getYear() - startsOn.getYear()) * 12
                + (date.getMonthValue() - startsOn.getMonthValue());

        return monthsBetween >= 0 && monthsBetween % frequency == 0;
    }

    private boolean isYearlyMatch(LocalDate startsOn, LocalDate date, int frequency) {
        if (date.getDayOfMonth() != startsOn.getDayOfMonth()
                || date.getMonthValue() != startsOn.getMonthValue()) {
            return false;
        }

        int yearsBetween = date.getYear() - startsOn.getYear();
        return yearsBetween >= 0 && yearsBetween % frequency == 0;
    }
}