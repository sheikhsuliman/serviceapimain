package com.siryus.swisscon.api.config.schedule;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScheduleIT extends AbstractMvcTestBase {

    private final Scheduler scheduler;

    @Autowired
    public ScheduleIT(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Test
    public void Given_Scheduler_When_Executed_Then_propertyHasChanged() {
        assertTrue(scheduler.isExecuted());
    }
}
