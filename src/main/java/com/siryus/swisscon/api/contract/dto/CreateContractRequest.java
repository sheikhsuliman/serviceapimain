package com.siryus.swisscon.api.contract.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
public class CreateContractRequest {
    @Reference(ReferenceType.CONTRACT)
    private final Integer primaryContractId;

    @NotNull
    @Reference(ReferenceType.PROJECT)
    private final Integer projectId;

    @NotNull
    @NotEmpty
    private final String name;

    private final String description;

    private final LocalDateTime deadline;

    @JsonCreator
    public CreateContractRequest(
            @JsonProperty("primaryContractId") Integer primaryContractId,
            @JsonProperty("projectId") Integer projectId,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("deadline") LocalDateTime deadline
    ) {
        this.primaryContractId = primaryContractId;
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.deadline = deadline;
    }
}
