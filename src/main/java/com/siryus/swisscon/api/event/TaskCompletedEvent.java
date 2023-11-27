package com.siryus.swisscon.api.event;

import lombok.Getter;

@Getter
public class TaskCompletedEvent implements CustomEvent {

    private final Integer taskId;

    public TaskCompletedEvent(Integer taskId) {
        this.taskId = taskId;
    }

}
