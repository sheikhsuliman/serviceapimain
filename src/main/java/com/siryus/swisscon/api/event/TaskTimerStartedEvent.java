package com.siryus.swisscon.api.event;

import lombok.Getter;

@Getter
public class TaskTimerStartedEvent implements CustomEvent {

    private final Integer mainTaskId;

    public TaskTimerStartedEvent(Integer mainTaskId) {
        this.mainTaskId = mainTaskId;
    }

}
