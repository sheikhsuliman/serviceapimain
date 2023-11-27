package com.siryus.swisscon.api.taskworklog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.taskworklog.entity.TaskWorklogEntity;

public class StartStopWorklogDTO {
    private final TaskWorklogDTO start;
    private final TaskWorklogDTO stop;

    @JsonCreator
    public StartStopWorklogDTO(
            @JsonProperty("start") TaskWorklogDTO start,
            @JsonProperty("stop") TaskWorklogDTO stop
    ) {
        this.start = start;
        this.stop = stop;
    }

    public static StartStopWorklogDTO from(TaskWorklogEntity startEvent, TaskWorklogEntity stopEvent) {
        return new StartStopWorklogDTO(
            TaskWorklogDTO.from(startEvent),
            TaskWorklogDTO.from(stopEvent)
        );
    }

    public TaskWorklogDTO getStart() {
        return start;
    }

    public TaskWorklogDTO getStop() {
        return stop;
    }
}
