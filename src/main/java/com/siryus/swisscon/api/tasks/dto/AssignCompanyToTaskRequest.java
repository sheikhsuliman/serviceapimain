package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder(toBuilder = true)
@Getter
public class AssignCompanyToTaskRequest {
    @Reference(ReferenceType.COMPANY)
    private final Integer companyId;
    private final List<@Reference(ReferenceType.USER) Integer> taskTeam;

    @JsonCreator
    public AssignCompanyToTaskRequest(
            @JsonProperty("companyId") Integer companyId,
            @JsonProperty("taskTeam") List<Integer> taskTeam
    ) {
        this.companyId = companyId;
        this.taskTeam = taskTeam;
    }
}
