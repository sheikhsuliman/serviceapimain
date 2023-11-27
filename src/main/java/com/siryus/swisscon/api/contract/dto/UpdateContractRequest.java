package com.siryus.swisscon.api.contract.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
public class UpdateContractRequest {
    @NotNull
    @NotEmpty
    private final String name;

    private final String description;

    private final LocalDateTime deadline;

    @JsonCreator
    public UpdateContractRequest(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("deadline") LocalDateTime deadline
    ) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
    }
}
