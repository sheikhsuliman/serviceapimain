package com.siryus.swisscon.api.contract.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.contract.repos.ContractEntity;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
public class ContractSummaryDTO {
    private final Integer contractId;

    private final Integer primaryContractId;

    private final Integer projectId;

    private final Integer contractNumber;
    private final String name;
    private final ContractState contractState;

    private final Integer contractorCompanyId;
    private final Integer customerCompanyId;

    private final Integer numberOfTasks;
    private final Integer numberOfCompletedTasks;

    private final BigDecimal totalAmount;

    private final LocalDateTime signed;

    private final boolean hasExtensions;

    @JsonCreator
    public ContractSummaryDTO(
            @JsonProperty("contractId") Integer contractId,
            @JsonProperty("primaryContractId") Integer primaryContractId,
            @JsonProperty("projectId") Integer projectId,
            @JsonProperty("contractNumber") Integer contractNumber,
            @JsonProperty("name") String name,
            @JsonProperty("contractState") ContractState contractState,
            @JsonProperty("contractorCompanyId") Integer contractorCompanyId,
            @JsonProperty("customerCompanyId") Integer customerCompanyId,
            @JsonProperty("numberOfTasks") Integer numberOfTasks,
            @JsonProperty("numberOfCompletedTasks") Integer numberOfCompletedTasks,
            @JsonProperty("totalAmount") BigDecimal totalAmount,
            @JsonProperty("signed") LocalDateTime signed,
            @JsonProperty("hasExtensions") boolean hasExtensions
    ) {
        this.contractId = contractId;
        this.primaryContractId = primaryContractId;
        this.contractNumber = contractNumber;
        this.name = name;
        this.contractState = contractState;
        this.projectId = projectId;
        this.contractorCompanyId = contractorCompanyId;
        this.customerCompanyId = customerCompanyId;
        this.numberOfTasks = numberOfTasks;
        this.numberOfCompletedTasks = numberOfCompletedTasks;
        this.totalAmount = totalAmount;
        this.signed = signed;
        this.hasExtensions = hasExtensions;
    }

    public static ContractSummaryDTO from(
            ContractEntity entity,
            ContractState contractState,
            Integer numberOfTasks,
            Integer numberOfCompletedTasks,
            BigDecimal totalAmount,
            LocalDateTime signed,
            boolean hasExtensions
    ) {
        return new ContractSummaryDTO(
                entity.getId(),
                entity.getPrimaryContractId(),
                entity.getProjectId(),
                entity.getContractNumber(),
                entity.getName(),
                contractState,
                entity.getContractorId(),
                entity.getCustomerId(),
                numberOfTasks,
                numberOfCompletedTasks,
                totalAmount,
                signed,
                hasExtensions
        );
    }

    public Integer getProgress() {
        return numberOfTasks == 0 ? 0 : numberOfCompletedTasks * 100 / numberOfTasks;
    }
}
