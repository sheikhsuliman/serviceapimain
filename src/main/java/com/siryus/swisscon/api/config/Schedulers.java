package com.siryus.swisscon.api.config;

import com.siryus.swisscon.api.file.file.FileService;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Class to manage all scheduled/cronjob method invocations.
 * All methods should be annotated with @Scheduled with the according cronjob expression
 * as well as @SchedulerLock to ensure the job is only executed once if the spring applications runs
 * on multiple instances.
 */
@Component
public class Schedulers {

    private final FileService fileService;

    @Autowired
    public Schedulers(FileService fileService) {
        this.fileService = fileService;
    }

    @Scheduled(cron = "0 0 1 * * ?", zone = "UTC") // execute night at 1am
    @SchedulerLock(name = "Schedulers_cleanTemporaryFiles", lockAtLeastFor = "5m", lockAtMostFor = "60m")
    public void cleanTemporaryFiles() {
        LockAssert.assertLocked();
        fileService.cleanupTemporaryFiles();
    }

}
