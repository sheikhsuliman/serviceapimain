package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.tasks.entity.TaskLinkEntity;
import com.siryus.swisscon.api.tasks.entity.TaskLinkType;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
public class TaskLinkDTO {
    private final Integer id;
    private final Integer projectId;
    private final TaskLinkType type;
    private final Integer sourceTaskId;
    private final Integer sourceTaskNumber;
    private final String sourceTaskTitle;
    private final Integer destinationTaskId;
    private final Integer destinationTaskNumber;
    private final String destinationTaskTitle;

    @JsonCreator
    public TaskLinkDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("projectId") Integer projectId,
            @JsonProperty("type") TaskLinkType type,
            @JsonProperty("sourceTaskId") Integer sourceTaskId,
            @JsonProperty("sourceTaskNumber") Integer sourceTaskNumber,
            @JsonProperty("sourceTaskTitle") String sourceTaskTitle,
            @JsonProperty("destinationTaskId") Integer destinationTaskId,
            @JsonProperty("destinationTaskNumber") Integer destinationTaskNumber,
            @JsonProperty("destinationTaskTitle") String destinationTaskTitle
    ) {
        this.id = id;
        this.projectId = projectId;
        this.type = type;
        this.sourceTaskId = sourceTaskId;
        this.sourceTaskNumber = sourceTaskNumber;
        this.sourceTaskTitle = sourceTaskTitle;
        this.destinationTaskId = destinationTaskId;
        this.destinationTaskNumber = destinationTaskNumber;
        this.destinationTaskTitle = destinationTaskTitle;
    }

    public static TaskLinkDTO from(TaskLinkEntity entity) {
        return new TaskLinkDTO(
                entity.getId(),
                entity.getProject().getId(),
                entity.getLinkType(),
                entity.getSourceTask().getId(),
                entity.getSourceTask().getTaskNumber(),
                entity.getSourceTask().getTitle(),
                entity.getDestinationTask().getId(),
                entity.getDestinationTask().getTaskNumber(),
                entity.getDestinationTask().getTitle()
        );
    }
}
