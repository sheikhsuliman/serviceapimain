package com.siryus.swisscon.api.taskworklog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public class ModifyTimerWorkLogRequest {
    private final LocalDateTime startTime;
    private final LocalDateTime stopTime;

    @JsonCreator
    public ModifyTimerWorkLogRequest(
            @JsonProperty("startTime") LocalDateTime startTime,
            @JsonProperty("endTime") LocalDateTime stopTime
    ) {
        this.startTime = startTime;
        this.stopTime = stopTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getStopTime() {
        return stopTime;
    }
}
