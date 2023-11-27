package com.siryus.swisscon.api.taskworklog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder(toBuilder = true)
@Getter
public class WorkerStatusDTO {
    private final Integer workerId;
    private final List<Integer> activeSubTasks;

    @JsonCreator
    public WorkerStatusDTO(
            @JsonProperty("workerId") Integer workerId,
            @JsonProperty("activeSubTasks") List<Integer> activeSubTasks
    ) {
        this.workerId = workerId;
        this.activeSubTasks = activeSubTasks;
    }
}
