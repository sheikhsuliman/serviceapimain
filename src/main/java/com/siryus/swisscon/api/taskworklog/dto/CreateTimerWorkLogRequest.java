package com.siryus.swisscon.api.taskworklog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import lombok.Builder;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
public class CreateTimerWorkLogRequest {
    @Reference(ReferenceType.USER)
    private final Integer workerId;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @NotNull
    private final LocalDateTime startTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @NotNull
    private final LocalDateTime endTime;

    @Reference(ReferenceType.SUB_TASK)
    private final Integer subTaskId;

    private final String comment;

    private final String latitude;

    private final String longitude;

    private final List<Integer> attachmentIDs;

    @JsonCreator
    public CreateTimerWorkLogRequest(
            @JsonProperty("workerId") Integer workerId,
            @JsonProperty("startTime") LocalDateTime startTime,
            @JsonProperty("endTime") LocalDateTime endTime,
            @JsonProperty("subTaskId") Integer subTaskId,
            @JsonProperty("comment") String comment,
            @JsonProperty("latitude") String latitude,
            @JsonProperty("longitude") String longitude,
            @JsonProperty("attachmentIDs") List<Integer> attachmentIDs
    ) {
        this.workerId = workerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.subTaskId = subTaskId;
        this.comment = comment;
        this.latitude = latitude;
        this.longitude = longitude;
        this.attachmentIDs = attachmentIDs;
    }

    public Integer getWorkerId() {
        return workerId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Integer getSubTaskId() {
        return subTaskId;
    }

    public String getComment() {
        return comment;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public List<Integer> getAttachmentIDs() {
        return attachmentIDs;
    }
}
