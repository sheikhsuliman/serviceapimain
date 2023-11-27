package com.siryus.swisscon.api.contract.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.contract.repos.ContractEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder(toBuilder = true)
@Getter
public class ContractCommentsDTO {
    private final Integer projectId;
    private final Integer contractId;
    private final Integer contractNumber;

    private final String contractName;
    private final Integer contractorCompanyId;
    private final Integer customerCompanyId;

    private final List<ContractCommentDTO> comments;

    @JsonCreator
    public ContractCommentsDTO(
            @JsonProperty("projectId") Integer projectId,
            @JsonProperty("contractId") Integer contractId,
            @JsonProperty("contractNumber") Integer contractNumber,
            @JsonProperty("contractName") String contractName,
            @JsonProperty("contractorCompanyId") Integer contractorCompanyId,
            @JsonProperty("customerCompanyId") Integer customerCompanyId,
            @JsonProperty("comments") List<ContractCommentDTO> comments
    ) {
        this.projectId = projectId;
        this.contractId = contractId;
        this.contractNumber = contractNumber;
        this.contractName = contractName;
        this.contractorCompanyId = contractorCompanyId;
        this.customerCompanyId = customerCompanyId;
        this.comments = comments;
    }

    public static ContractCommentsDTO from(ContractEntity validContract, List<ContractCommentDTO> comments) {
        return new ContractCommentsDTO(
                validContract.getProjectId(),
                validContract.getId(),
                validContract.getContractNumber(),
                validContract.getName(),
                validContract.getContractorId(),
                validContract.getCustomerId(),
                comments
        );
    }
}
