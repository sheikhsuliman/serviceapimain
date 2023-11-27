package com.siryus.swisscon.api.contract.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Builder(toBuilder = true)
@Getter
public class ListContractsRequest {
    @NotNull
    @Reference(ReferenceType.PROJECT)
    private final Integer projectId;

    private final ContractState contractStateFilter;

    @Reference(ReferenceType.COMPANY)
    private final Integer contractorIdFilter;

    private final ContractOrderBy orderBy;
    private final boolean orderAscending;

    @JsonCreator
    public ListContractsRequest(
            @JsonProperty("projectId") Integer projectId,
            @JsonProperty("contractStateFilter") ContractState contractStateFilter,
            @JsonProperty("contractorIdFilter") Integer contractorIdFilter,
            @JsonProperty("orderBy") ContractOrderBy orderBy,
            @JsonProperty("orderAscending") boolean orderAscending
    ) {
        this.projectId = projectId;
        this.contractStateFilter = contractStateFilter;
        this.contractorIdFilter = contractorIdFilter;
        this.orderBy = orderBy;
        this.orderAscending = orderAscending;
    }
}
