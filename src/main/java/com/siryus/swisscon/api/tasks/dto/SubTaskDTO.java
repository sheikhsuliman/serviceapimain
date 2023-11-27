package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.location.location.LocationReferenceDTO;

import java.time.LocalDateTime;
import java.util.List;

public class SubTaskDTO {

    private final Integer id;
    private final Integer createdBy;
    private final LocalDateTime created;

    private final Integer mainTaskId;
    private final Integer companyId;
    private final List<LocationReferenceDTO> locationPath;

    private final Integer subTaskNumber;

    private final String title;
    private final String description;

    private final TaskStatus status;

    private final List<TaskChecklistItem> subTaskCheckLists;

    private final Integer timeBudgetMinutes;

    private final List<Integer> team;

    @JsonCreator
    public SubTaskDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("createdBy")  Integer createdBy,
            @JsonProperty("created")  LocalDateTime created,
            @JsonProperty("mainTaskId")  Integer mainTaskId,
            @JsonProperty("companyId") Integer companyId,
            @JsonProperty("locationPath")  List<LocationReferenceDTO> locationPath,
            @JsonProperty("subTaskNumber")  Integer subTaskNumber,
            @JsonProperty("title")  String title,
            @JsonProperty("description")  String description,
            @JsonProperty("status")  TaskStatus status,
            @JsonProperty("subTaskCheckLists")  List<TaskChecklistItem> subTaskCheckLists,
            @JsonProperty("timeBudgetMinutes")  Integer timeBudgetMinutes,
            @JsonProperty("team")  List<Integer> team
    ) {
        this.id = id;
        this.createdBy = createdBy;
        this.created = created;
        this.mainTaskId = mainTaskId;
        this.companyId = companyId;
        this.locationPath = locationPath;
        this.subTaskNumber = subTaskNumber;
        this.title = title;
        this.description = description;
        this.status = status;
        this.subTaskCheckLists = subTaskCheckLists;
        this.timeBudgetMinutes = timeBudgetMinutes;
        this.team = team;
    }

    public Integer getId() {
        return id;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreated() {
        return created;
    }
    public Integer getMainTaskId() {
        return mainTaskId;
    }

    public Integer getCompanyId() { return companyId; }

    public List<LocationReferenceDTO> getLocationPath() {
        return locationPath;
    }

    public Integer getSubTaskNumber() {
        return subTaskNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public List<TaskChecklistItem> getSubTaskCheckLists() {
        return subTaskCheckLists;
    }

    public Integer getTimeBudgetMinutes() {
        return timeBudgetMinutes;
    }

    public List<Integer> getTeam() {
        return team;
    }
}
