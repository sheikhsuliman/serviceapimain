package com.siryus.swisscon.api.taskworklog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import lombok.Builder;

import java.util.Collections;
import java.util.List;

@Builder(toBuilder = true)
public class TaskWorklogRequest {
    @Reference(ReferenceType.MAIN_TASK)
    private final Integer mainTaskId;

    @Reference(ReferenceType.SUB_TASK)
    private final Integer subTaskId;

    private final String comment;

    private final String latitude;

    private final String longitude;

    private final List<Integer> attachmentIDs;

    public TaskWorklogRequest(Integer mainTaskId, Integer subTaskId, String comment) {
        this(mainTaskId, subTaskId, comment, null, null, Collections.emptyList());
    }

    public TaskWorklogRequest(Integer mainTaskId, Integer subTaskId, String comment, List<Integer> attachmentIDs) {
        this(mainTaskId, subTaskId, comment, null, null, attachmentIDs);
    }

    @JsonCreator
    public TaskWorklogRequest(
            @JsonProperty("mainTaskId") Integer mainTaskId,
           @JsonProperty("subTaskId") Integer subTaskId,
           @JsonProperty("comment")  String comment,
           @JsonProperty("latitude") String latitude,
           @JsonProperty("longitude") String longitude,
            @JsonProperty("attachmentIDs") List<Integer> attachmentIDs
    ) {
        this.mainTaskId = mainTaskId;
        this.subTaskId= subTaskId;
        this.comment=comment;
        this.latitude=latitude;
        this.longitude=longitude;
        this.attachmentIDs = attachmentIDs;
    }

    public TaskWorklogRequest withTaskIds(Integer mainTaskId, Integer subTaskId) {
        return new TaskWorklogRequest(
                mainTaskId,
                subTaskId,
                this.comment,
                this.latitude,
                this.longitude,
                this.attachmentIDs
        );
    }

    public TaskWorklogRequest withSubTaskId(Integer subTaskId) {
        return new TaskWorklogRequest(
                this.mainTaskId,
                subTaskId,
                this.comment,
                this.latitude,
                this.longitude,
                this.attachmentIDs
        );
    }

    public Integer getMainTaskId() {
        return mainTaskId;
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
