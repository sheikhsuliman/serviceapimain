package com.siryus.swisscon.api.config.schedule;

import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

    private boolean isExecuted;

    @Scheduled(initialDelay = 0, fixedDelay = Long.MAX_VALUE)
    @SchedulerLock(name = "Schedulers_testScheduleMethod", lockAtLeastFor = "60m", lockAtMostFor = "60m")
    public void scheduledMethod() {
        LockAssert.assertLocked();
        isExecuted = true;
        LOGGER.info("Scheduled test method executed");
    }

    public boolean isExecuted() {
        return isExecuted;
    }

}
