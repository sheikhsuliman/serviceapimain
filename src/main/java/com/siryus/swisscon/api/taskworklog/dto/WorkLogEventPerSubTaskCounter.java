package com.siryus.swisscon.api.taskworklog.dto;

public class WorkLogEventPerSubTaskCounter {
    public final Integer subTaskId;
    public final WorkLogEventType event;
    public final Long count;

    public WorkLogEventPerSubTaskCounter(Integer subTaskId, String event, Long count) {
        this.subTaskId = subTaskId;
        this.event = WorkLogEventType.valueOf(event);
        this.count = count;
    }

    public Integer getSubTaskId() {
        return subTaskId;
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
