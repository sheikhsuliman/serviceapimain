package com.siryus.swisscon.api.taskworklog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.auth.user.AuthorDTO;
import lombok.Builder;

import java.util.Collections;
import java.util.List;

@Builder(toBuilder = true)
public class WorkerDurationDTO {
    private final AuthorDTO worker;
    private final List<TimeWorklogDTO> timeWorklogs;
    private Long totalDurationInSeconds;

    @JsonCreator
    public WorkerDurationDTO(
            @JsonProperty("author") AuthorDTO worker,
            @JsonProperty("timeWorklogs") List<TimeWorklogDTO> timeWorklogs,
            @JsonProperty("totalDurationInSeconds") Long totalDurationInSeconds
    ) {
        this.worker = worker;
        this.timeWorklogs = Collections.unmodifiableList(timeWorklogs);
        this.totalDurationInSeconds = totalDurationInSeconds;
    }

    public AuthorDTO getWorker() {
        return worker;
    }

    public List<TimeWorklogDTO> getTimeWorklogs() {
        return timeWorklogs;
    }

    public Long getTotalDurationInSeconds() {
        return totalDurationInSeconds;
    }

    public void setTotalDurationInSeconds(Long totalDurationInSeconds) {
        this.totalDurationInSeconds = totalDurationInSeconds;
    }
}
