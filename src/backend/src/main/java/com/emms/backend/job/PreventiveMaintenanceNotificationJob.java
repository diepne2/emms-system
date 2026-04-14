package com.emms.backend.job;

import com.emms.backend.entity.PreventiveMaintenance;
import com.emms.backend.entity.Schedule;
import com.emms.backend.entity.User;
import com.emms.backend.infrastructure.MailServiceFactory;
import com.emms.backend.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class PreventiveMaintenanceNotificationJob extends QuartzJobBean {

    private final ScheduleRepository scheduleRepository;
    private final MailServiceFactory mailServiceFactory;
    private final MessageSource messageSource;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Long scheduleId = getScheduleId(context);
        if (scheduleId == null) {
            log.warn("PreventiveMaintenanceNotificationJob skipped: missing scheduleId");
            return;
        }

        try {
            Schedule schedule = scheduleRepository.findById(scheduleId).orElse(null);
            if (schedule == null) {
                log.warn("PreventiveMaintenanceNotificationJob skipped: schedule not found, id={}", scheduleId);
                return;
            }

            if (schedule.isDisabled()) {
                log.info("PreventiveMaintenanceNotificationJob skipped: schedule disabled, id={}", scheduleId);
                return;
            }

            PreventiveMaintenance pm = schedule.getPreventiveMaintenance();
            if (pm == null) {
                log.warn(
                        "PreventiveMaintenanceNotificationJob skipped: no preventive maintenance linked, scheduleId={}",
                        scheduleId
                );
                return;
            }

            if (!pm.isActive()) {
                log.info(
                        "PreventiveMaintenanceNotificationJob skipped: preventive maintenance inactive, scheduleId={}, pmId={}",
                        scheduleId,
                        pm.getId()
                );
                return;
            }

            Locale locale = Locale.getDefault();
            String title = messageSource.getMessage(
                    "coming_wo",
                    null,
                    "Upcoming preventive maintenance",
                    locale
            );

            Collection<User> usersToMail = getRecipients(pm);

            String[] recipientEmails = usersToMail.stream()
                    .filter(Objects::nonNull)
                    .filter(this::canReceiveWorkOrderEmail)
                    .map(User::getEmail)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(email -> !email.isEmpty())
                    .distinct()
                    .toArray(String[]::new);

            if (recipientEmails.length == 0) {
                log.info(
                        "PreventiveMaintenanceNotificationJob skipped: no valid recipient emails, scheduleId={}, pmId={}",
                        scheduleId,
                        pm.getId()
                );
                return;
            }

            Map<String, Object> mailVariables = new HashMap<>();
            mailVariables.put("pmLink", frontendUrl + "/app/preventive-maintenances/" + pm.getId());
            mailVariables.put("pmTitle", pm.getTitle());

            log.info(
                    "Sending preventive maintenance notification: scheduleId={}, pmId={}, recipients={}",
                    scheduleId,
                    pm.getId(),
                    recipientEmails.length
            );

            mailServiceFactory.getMailService().sendMessageUsingThymeleafTemplate(
                    recipientEmails,
                    title,
                    mailVariables,
                    "coming-work-order.html",
                    locale
            );

            log.info(
                    "Preventive maintenance notification sent successfully: scheduleId={}, pmId={}",
                    scheduleId,
                    pm.getId()
            );

        } catch (Exception ex) {
            log.error("Failed to execute PreventiveMaintenanceNotificationJob for scheduleId={}", scheduleId, ex);
            throw new JobExecutionException("Failed to send preventive maintenance notification", ex);
        }
    }

    private Collection<User> getRecipients(PreventiveMaintenance pm) {
        if (pm == null || pm.getAssignedTo() == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(pm.getAssignedTo());
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

    private boolean canReceiveWorkOrderEmail(User user) {
        return user != null
                && user.isEnabled()
                && user.getEmail() != null
                && !user.getEmail().isBlank();
    }
}