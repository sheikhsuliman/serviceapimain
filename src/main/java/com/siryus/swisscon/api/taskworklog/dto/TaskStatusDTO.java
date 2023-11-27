package com.siryus.swisscon.api.taskworklog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.siryus.swisscon.api.mediawidget.MediaWidgetFileDTO;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class TaskStatusDTO {
    private final Integer mainTaskId;
    private final TaskStatus mainTaskStatus;
    private final ZonedDateTime dueDate;

    private final Map<Integer, TaskStatus> subTaskStatuses;

    private final Integer taskWorkLogId;

    private final String comment;
    private final List<MediaWidgetFileDTO> attachments;

    @JsonCreator
    public TaskStatusDTO(
            @JsonProperty("mainTaskId") Integer mainTaskId,
            @JsonProperty("mainTaskStatus") TaskStatus mainTaskStatus,
            @JsonProperty("dueDate") ZonedDateTime dueDate,
            @JsonProperty("subTaskStatuses") Map<Integer, TaskStatus> subTaskStatuses,
            @JsonProperty("taskWorkLogId") Integer taskWorkLogId,
            @JsonProperty("comment") String comment,
            @JsonProperty("attachments") List<MediaWidgetFileDTO> attachments
    ) {
        this.mainTaskId = mainTaskId;
        this.mainTaskStatus = mainTaskStatus;
        this.dueDate = dueDate.truncatedTo(ChronoUnit.SECONDS);
        this.subTaskStatuses = ImmutableMap.copyOf(subTaskStatuses);
        this.taskWorkLogId = taskWorkLogId;
        this.comment = comment;
        this.attachments = attachments;
    }

    public Integer getMainTaskId() {
        return mainTaskId;
    }

    public TaskStatus getMainTaskStatus() {
        return mainTaskStatus;
    }

    public Map<Integer, TaskStatus> getSubTaskStatuses() {
        return subTaskStatuses;
    }

    public Integer getTaskWorkLogId() {
        return taskWorkLogId;
    }

    public String getComment() {
        return comment;
    }

    public List<MediaWidgetFileDTO> getAttachments() {
        return attachments;
    }

    public ZonedDateTime getDueDate() {
        return dueDate;
    }
}
