package com.siryus.swisscon.api.taskworklog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.siryus.swisscon.api.taskworklog.entity.TaskWorklogEntity;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class TaskWorklogDTO {
    private final Integer id;

    private final Integer mainTaskId;

    private final Integer subTaskId;

    private final Integer workerId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private final LocalDateTime timestamp;

    private final WorkLogEventType event;

    private final String comment;

    private final String latitude;

    private final String longitude;

    private final List<Integer> attachmentIDs;

    @JsonCreator
    public TaskWorklogDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("mainTaskId") Integer mainTaskId,
            @JsonProperty("subTaskId") Integer subTaskId,
            @JsonProperty("workerId") Integer workerId,
            @JsonProperty("timestamp") LocalDateTime timestamp,
            @JsonProperty("event") WorkLogEventType event,
            @JsonProperty("comment") String comment,
            @JsonProperty("latitude") String latitude,
            @JsonProperty("longitude") String longitude,
            @JsonProperty("attachmentIDs") List<Integer> attachmentIDs
    ) {
        this.id = id;
        this.mainTaskId = mainTaskId;
        this.subTaskId = subTaskId;
        this.workerId = workerId;
        this.timestamp = timestamp;
        this.event = event;
        this.comment = comment;
        this.latitude = latitude;
        this.longitude = longitude;
        this.attachmentIDs = attachmentIDs;
    }

    public static TaskWorklogDTO from(TaskWorklogEntity taskWorklogEntity) {
        if (taskWorklogEntity == null) {
            return null;
        }

        return new TaskWorklogDTO(
            taskWorklogEntity.getId(),
            taskWorklogEntity.getMainTask() != null? taskWorklogEntity.getMainTask().getId() : null,
            taskWorklogEntity.getSubTask() != null ? taskWorklogEntity.getSubTask().getId() : null,
            taskWorklogEntity.getWorker().getId(),
            taskWorklogEntity.getTimestamp(),
            taskWorklogEntity.getEvent(),
            taskWorklogEntity.getComment(),
            taskWorklogEntity.getLatitude(),
            taskWorklogEntity.getLongitude(),
            taskWorklogEntity.getAttachmentIDs() == null
                    ? null
                    : taskWorklogEntity.getAttachmentIDs().stream().map( e -> e.getFile().getId()).collect(Collectors.toList())
        );
    }
}
