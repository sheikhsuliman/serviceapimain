package com.siryus.swisscon.api.taskworklog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Builder
@Getter
public class RejectTaskRequest {
    @JsonProperty
    private final ZonedDateTime dueDate;
    @JsonProperty
    private final TaskWorklogRequest worklogRequest;

    @JsonCreator
    public RejectTaskRequest(
            @JsonProperty("dueDate") ZonedDateTime dueDate,
            @JsonProperty("worklogRequest") TaskWorklogRequest worklogRequest
    ) {
        this.dueDate = dueDate.truncatedTo(ChronoUnit.SECONDS);
        this.worklogRequest = worklogRequest;
    }
}
