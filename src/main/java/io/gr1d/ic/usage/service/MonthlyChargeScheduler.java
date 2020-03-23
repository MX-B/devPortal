package io.gr1d.ic.usage.service;

import io.gr1d.ic.usage.model.Execution;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static io.gr1d.ic.usage.util.DateTimes.nextWorkDay;

@Slf4j
@Component
public class MonthlyChargeScheduler implements Job {

    private final RoutineExecutor routineExecutor;

    @Autowired
    public MonthlyChargeScheduler(final RoutineExecutor routineExecutor) {
        this.routineExecutor = routineExecutor;
    }

    @Override
    public void execute(final JobExecutionContext context) {
        log.info("Executing monthly charge routine. now={}", ZonedDateTime.now());

        final LocalDate referenceMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        final LocalDate chargeDate = nextWorkDay(LocalDate.now().plusDays(5));
        final Execution execution = routineExecutor.execute(referenceMonth, chargeDate, "system",
                "MonthlyCharge", "Monthly Usage Routine");

        context.setResult(execution);
    }
}
