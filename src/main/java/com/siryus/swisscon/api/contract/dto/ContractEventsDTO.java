package com.siryus.swisscon.api.contract.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.contract.repos.ContractEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder(toBuilder = true)
@Getter
public class ContractEventsDTO {
    private final Integer projectId;
    private final Integer contractId;
    private final Integer contractNumber;

    private final String contractName;
    private final Integer contractorCompanyId;
    private final Integer customerCompanyId;

    private final List<ContractEventLogDTO> events;

    @JsonCreator
    public ContractEventsDTO(
            @JsonProperty("projectId") Integer projectId,
            @JsonProperty("contractId") Integer contractId,
            @JsonProperty("contractNumber") Integer contractNumber,
            @JsonProperty("contractName") String contractName,
            @JsonProperty("contractorCompanyId") Integer contractorCompanyId,
            @JsonProperty("customerCompanyId") Integer customerCompanyId,
            @JsonProperty("events") List<ContractEventLogDTO> events
    ) {
        this.projectId = projectId;
        this.contractId = contractId;
        this.contractNumber = contractNumber;
        this.contractName = contractName;
        this.contractorCompanyId = contractorCompanyId;
        this.customerCompanyId = customerCompanyId;
        this.events = events;
    }

    public static ContractEventsDTO from(ContractEntity validContract, List<ContractEventLogDTO> events) {
        return new ContractEventsDTO(
                validContract.getProjectId(),
                validContract.getId(),
                validContract.getContractNumber(),
                validContract.getName(),
                validContract.getContractorId(),
                validContract.getCustomerId(),
                events
        );
    }
}
