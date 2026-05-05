package com.emms.backend.config;

import com.emms.backend.entity.Schedule;
import com.emms.backend.repository.ScheduleRepository;
import com.emms.backend.job.WorkOrderCreationJob;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class QuartzScheduleConfig {

    private final Scheduler scheduler;
    private final ScheduleRepository scheduleRepository;

    @Bean
    public CommandLineRunner registerWorkOrderJobs() {
        return args -> {
            for (Schedule schedule : scheduleRepository.findAll()) {
                if (schedule.isDisabled()) continue;

                JobDetail jobDetail = JobBuilder.newJob(WorkOrderCreationJob.class)
                        .withIdentity("wo-create-job-" + schedule.getScheduleId(), "pm-auto-wo")
                        .usingJobData("scheduleId", schedule.getScheduleId())
                        .storeDurably()
                        .build();

                Trigger trigger = TriggerBuilder.newTrigger()
                        .forJob(jobDetail)
                        .withIdentity("wo-create-trigger-" + schedule.getScheduleId(), "pm-auto-wo")
                        .withSchedule(CronScheduleBuilder.cronSchedule("0 0 1 * * ?"))
                        .build();

                if (!scheduler.checkExists(jobDetail.getKey())) {
                    scheduler.scheduleJob(jobDetail, trigger);
                }
            }
        };
    }
}