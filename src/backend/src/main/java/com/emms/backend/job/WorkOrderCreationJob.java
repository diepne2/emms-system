package com.emms.backend.job;

import com.emms.backend.entity.PreventiveMaintenance;
import com.emms.backend.entity.Schedule;
import com.emms.backend.entity.Task;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.repository.ScheduleRepository;
import com.emms.backend.service.ScheduleService;
import com.emms.backend.service.TaskService;
import com.emms.backend.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkOrderCreationJob extends QuartzJobBean {

    private final ScheduleRepository scheduleRepository;
    private final WorkOrderService workOrderService;
    private final TaskService taskService;
    private final ScheduleService scheduleService;

    @Override
    @Transactional
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Long scheduleId = getScheduleId(context);

        if (scheduleId == null) {
            log.warn("WorkOrderCreationJob skipped: missing scheduleId");
            return;
        }

        try {
            Schedule schedule = scheduleRepository.findById(scheduleId).orElse(null);

            if (schedule == null) {
                log.warn("WorkOrderCreationJob skipped: schedule not found, id={}", scheduleId);
                return;
            }

            if (schedule.isDisabled()) {
                log.info("WorkOrderCreationJob skipped: schedule disabled, id={}", scheduleId);
                return;
            }

            LocalDate today = LocalDate.now();

            boolean shouldGenerate = scheduleService.shouldGenerateOnDate(schedule, today);
            if (!shouldGenerate) {
                log.info("WorkOrderCreationJob skipped: schedule does not match today, id={}", scheduleId);
                return;
            }

            PreventiveMaintenance preventiveMaintenance = schedule.getPreventiveMaintenance();

            if (preventiveMaintenance == null) {
                log.warn("WorkOrderCreationJob skipped: preventiveMaintenance is null, scheduleId={}", scheduleId);
                return;
            }

            if (!preventiveMaintenance.isActive()) {
                log.info(
                        "WorkOrderCreationJob skipped: preventive maintenance inactive, scheduleId={}, pmId={}",
                        scheduleId,
                        preventiveMaintenance.getId()
                );
                return;
            }

            WorkOrder savedWorkOrder = workOrderService.createFromPreventiveMaintenance(
                    preventiveMaintenance,
                    schedule,
                    today
            );

            List<Task> pmTasks = taskService.findByPreventiveMaintenance(preventiveMaintenance.getId());

            for (Task sourceTask : pmTasks) {
                Task copiedTask = new Task();

                copiedTask.setTaskBase(sourceTask.getTaskBase());
                copiedTask.setWorkOrder(savedWorkOrder);
                copiedTask.setPreventiveMaintenance(null);
                copiedTask.setLabel(sourceTask.getLabel());
                copiedTask.setNotes(sourceTask.getNotes());
                copiedTask.setValue(sourceTask.getValue());
                copiedTask.setStatus(Task.TaskStatus.OPEN);
                copiedTask.setSortOrder(sourceTask.getSortOrder());
                copiedTask.setRequiredTask(sourceTask.isRequiredTask());

                taskService.create(copiedTask);
            }

            log.info(
                    "Generated work order successfully: scheduleId={}, pmId={}, workOrderId={}",
                    scheduleId,
                    preventiveMaintenance.getId(),
                    savedWorkOrder.getId()
            );

        } catch (Exception ex) {
            log.error("Failed to execute WorkOrderCreationJob for scheduleId={}", scheduleId, ex);
            throw new JobExecutionException("Failed to generate work order from preventive maintenance", ex);
        }
    }

    private Long getScheduleId(JobExecutionContext context) {
        if (context == null || context.getMergedJobDataMap() == null) {
            return null;
        }

        Object value = context.getMergedJobDataMap().get("scheduleId");

        if (value instanceof Long longValue) {
            return longValue;
        }

        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }

        if (value instanceof String stringValue) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException ex) {
                log.warn("Invalid scheduleId format: {}", stringValue);
            }
        }

        return null;
    }
}