package com.siryus.swisscon.api.contract.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Builder(toBuilder = true)
@Getter
public class ContractAddTasksRequest {

    @Reference(ReferenceType.CONTRACT)
    @NotNull
    private final Integer contractId;

    @NotEmpty
    private final List<Integer> taskIds;

    @JsonCreator
    public ContractAddTasksRequest(
            @JsonProperty("contractId") Integer contractId,
            @JsonProperty("taskIds") List<Integer> taskIds
    ) {
        this.contractId = contractId;
        this.taskIds = taskIds;
    }

}
