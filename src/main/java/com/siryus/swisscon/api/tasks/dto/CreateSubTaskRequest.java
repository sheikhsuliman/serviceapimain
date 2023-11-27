package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import com.siryus.swisscon.api.util.validator.Sanitizable;
import com.siryus.swisscon.api.util.validator.SanitizableHtml;
import lombok.Builder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Builder(toBuilder = true)
@Sanitizable
public class CreateSubTaskRequest {
    @Reference(ReferenceType.MAIN_TASK)
    private final Integer mainTaskId;

    @NotEmpty
    private final String title;

    @NotNull
    @SanitizableHtml
    private final String description;

    @PositiveOrZero
    private final Integer timeBudgetMinutes;

    private final List<Integer> taskTeam;

    @JsonCreator
    public CreateSubTaskRequest(
            @JsonProperty("mainTaskId") Integer mainTaskId,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("timeBudgetMinutes") Integer timeBudgetMinutes,
            @JsonProperty("taskTeam") List<Integer> taskTeam
    ) {
        this.mainTaskId = mainTaskId;
        this.title = title;
        this.description = description;
        this.timeBudgetMinutes = timeBudgetMinutes;
        this.taskTeam = taskTeam;
    }

    public Integer getMainTaskId() {
        return mainTaskId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getTimeBudgetMinutes(){ return timeBudgetMinutes; }

    public List<Integer> getTaskTeam() {  return taskTeam; }
}
