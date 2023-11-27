package com.siryus.swisscon.api.taskworklog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.siryus.swisscon.api.taskworklog.entity.TaskWorklogEntity;
import com.siryus.swisscon.api.taskworklog.exceptions.TaskWorklogExceptions;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;

import static com.siryus.swisscon.api.taskworklog.dto.WorkLogEventType.START_TIMER;
import static com.siryus.swisscon.api.taskworklog.dto.WorkLogEventType.STOP_TIMER;

@Getter
@Setter
@Builder
public class TimeWorklogDTO {
    private Integer mainTaskId;
    private Integer subTaskId;
    private Integer workerId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startTime;

    private Integer startTimeWorklogId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime stopTime;

    private Integer stopTimeWorklogId;

    private boolean cancelled;

    @JsonCreator
    public TimeWorklogDTO(
            @JsonProperty("mainTaskId") Integer mainTaskId,
            @JsonProperty("subTaskId") Integer subTaskId,
            @JsonProperty("workerId") Integer workerId,
            @JsonProperty("startTime") LocalDateTime startTime,
            @JsonProperty("startTimeWorklogId") Integer startTimeWorklogId,
            @JsonProperty("stopTime") LocalDateTime stopTime,
            @JsonProperty("stopTimeWorklogId") Integer stopTimeWorklogId,
            @JsonProperty("cancelled") Boolean cancelled
    ) {
        this.mainTaskId = mainTaskId;
        this.subTaskId = subTaskId;
        this.workerId = workerId;

        this.startTime = startTime;
        this.startTimeWorklogId = startTimeWorklogId;

        this.stopTime = stopTime;
        this.stopTimeWorklogId = stopTimeWorklogId;

        this.cancelled = cancelled;
    }

    public static TimeWorklogDTO from(@NotNull  TaskWorklogEntity startEntity, TaskWorklogEntity stopEntity) {
        validateWorkLogEventType(startEntity, START_TIMER);
        validateWorkLogEventType(stopEntity, STOP_TIMER);

        return new TimeWorklogDTO(
                startEntity.getMainTask().getId(),
                startEntity.getSubTask().getId(),
                startEntity.getWorker().getId(),
                startEntity.getTimestamp(),
                startEntity.getId(),
                stopEntity == null ? LocalDateTime.now() : stopEntity.getTimestamp(),
                stopEntity == null ? null : stopEntity.getId(),
                false
        );
    }

    private static void validateWorkLogEventType(TaskWorklogEntity eventEntity, WorkLogEventType expectedEventType) {
        if( eventEntity != null && ! eventEntity.getEvent().equals(expectedEventType)) {
            throw TaskWorklogExceptions.unexpectedEventType();
        }
    }

    public Long getFullDurationInSeconds() {
        return cancelled || startTime == null || stopTime == null ? 0 : Duration.between(startTime, stopTime).getSeconds();
    }
}

