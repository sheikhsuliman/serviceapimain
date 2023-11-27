package com.siryus.swisscon.api.taskworklog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.mediawidget.MediaWidgetFileDTO;
import com.siryus.swisscon.api.taskworklog.entity.TaskWorklogEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Builder( toBuilder = true )
public class EventHistoryDTO {
    private final UserInfoEventHistoryDTO user;
    private final LocalDateTime date;
    private final List<MediaWidgetFileDTO> files; //Attachment/s
    private final WorkLogEventType type;
    // REQUIRED
    // START_TASK | COMPLETE_TASK | REJECT_TASK | APPROVE_TASK
    private final Integer mainTaskId;
    private final Integer subTaskId;
    private final String latitude;
    private final String longitude;
    private final String comment;
    
    private final Boolean startCompleted;
    private final Long duration;

    private final Boolean cancelled;

    @JsonCreator
    public EventHistoryDTO(
            @JsonProperty("user") UserInfoEventHistoryDTO user,
            @JsonProperty("date") LocalDateTime date,
            @JsonProperty("files") List<MediaWidgetFileDTO> files,
            @JsonProperty("type") WorkLogEventType type,
            @JsonProperty("mainTaskId") Integer mainTaskId,
            @JsonProperty("subTaskId") Integer subTaskId,
            @JsonProperty("latitude") String latitude,
            @JsonProperty("longitude") String longitude,
            @JsonProperty("comment") String comment,
            @JsonProperty("startCompleted") Boolean startCompleted,
            @JsonProperty("duration") Long duration,
            @JsonProperty("cancelled") Boolean cancelled
    ) {
        this.user = user;
        this.date = date;
        this.files = files;
        this.type = type;
        this.mainTaskId = mainTaskId;
        this.subTaskId = subTaskId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.comment = comment;
        this.startCompleted = startCompleted;
        this.duration = duration;
        this.cancelled = cancelled;
    }


    public static EventHistoryDTO from(TaskWorklogEntity taskWorklogEntity, List<MediaWidgetFileDTO> files, User user, Map<Integer, Double> durations, Set<Integer> cancelled)
    {   //Change ASA as new entity is updated in SIR-701 (where I store user instead of
        // UserId in TaskWorklogEntity), take off User and use taskWorklogEntity.getUser().

        boolean startCompleted = false;
        Long duration = null;

        if (durations.containsKey(taskWorklogEntity.getId())) {
            // Completed start event
            duration = durations.get(taskWorklogEntity.getId()).longValue();
            startCompleted = true;
        } else if (taskWorklogEntity.getEvent() == WorkLogEventType.START_TIMER) {
            // Incomplete start event
            duration = Duration.between(taskWorklogEntity.getTimestamp(), LocalDateTime.now()).toSeconds();
        }

        return new EventHistoryDTO(
                UserInfoEventHistoryDTO.from(user),
                taskWorklogEntity.getTimestamp(),
                files,
                taskWorklogEntity.getEvent(),
                taskWorklogEntity.getMainTask() != null ? taskWorklogEntity.getMainTask().getId() : null,
                taskWorklogEntity.getSubTask() != null ? taskWorklogEntity.getSubTask().getId() : null,
                taskWorklogEntity.getLatitude(),
                taskWorklogEntity.getLongitude(),
                taskWorklogEntity.getComment(),
                startCompleted,
                duration,
                cancelled.contains(taskWorklogEntity.getId())
        );
    }
}

