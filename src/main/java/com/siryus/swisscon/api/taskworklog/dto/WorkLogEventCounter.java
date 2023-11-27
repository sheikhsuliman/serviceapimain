package com.siryus.swisscon.api.taskworklog.dto;

public class WorkLogEventCounter {
    public final WorkLogEventType event;
    public final Long count;

    public WorkLogEventCounter(String event, Long count) {
        this.event = WorkLogEventType.valueOf(event);
        this.count = count;
    }

    public String getEvent() {
        return event.name();
    }

    public Long getCount() {
        return count;
    }

    public WorkLogEventType getEventType() {
        return event;
    }
}
