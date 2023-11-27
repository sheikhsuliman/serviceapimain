package com.siryus.swisscon.api.contract.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.contract.repos.ContractEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Builder(toBuilder = true)
@Getter
public class ContractDTO {
    private final Integer id;
    private final Integer primaryContractId;
    private final Integer projectId;
    private final Integer contractNumber;

    private final LocalDateTime disabled;

    private final String name;
    private final String description;

    private final Integer contractorCompanyId;
    private final List<TeamUserDTO> contractorSigners;

    private final Integer customerCompanyId;
    private final List<TeamUserDTO> customerSigners;

    private final LocalDateTime deadline;

    private final boolean mutable;
    private final ContractState contractState;

    @JsonCreator
    public ContractDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("primaryContractId") Integer primaryContractId,
            @JsonProperty("projectId") Integer projectId,
            @JsonProperty("contractNumber") Integer contractNumber,
            @JsonProperty("disabled") LocalDateTime disabled,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("contractorCompanyId") Integer contractorCompanyId,
            @JsonProperty("contractorSigners") List<TeamUserDTO> contractorSigners,
            @JsonProperty("customerCompanyId") Integer customerCompanyId,
            @JsonProperty("customerSigners") List<TeamUserDTO> customerSigners,
            @JsonProperty("deadline") LocalDateTime deadline,
            @JsonProperty("mutable") boolean mutable,
            @JsonProperty("contractState") ContractState contractState
    ) {
        this.id = id;
        this.primaryContractId = primaryContractId;
        this.projectId = projectId;
        this.contractNumber = contractNumber;
        this.disabled = disabled;
        this.name = name;
        this.description = description;
        this.contractorCompanyId = contractorCompanyId;
        this.contractorSigners = contractorSigners;
        this.customerCompanyId = customerCompanyId;
        this.customerSigners = customerSigners;
        this.deadline = deadline;
        this.mutable = mutable;
        this.contractState = contractState;
    }

    public static ContractDTO from(
            ContractEntity entity,
            Function<ContractEntity, List<TeamUserDTO>> contractorSigners,
            Function<ContractEntity, List<TeamUserDTO>> customerSigners,
            Function<ContractEntity, Boolean> mutable,
            Function<ContractEntity, ContractState> contractState
    ) {
        return new ContractDTO(
                entity.getId(),
                entity.getPrimaryContractId(),
                entity.getProjectId(),
                entity.getContractNumber(),
                entity.getDisabled(),
                entity.getName(),
                entity.getDescription(),
                entity.getContractorId(),
                contractorSigners.apply(entity),
                entity.getCustomerId(),
                customerSigners.apply(entity),
                entity.getDeadline(),
                mutable.apply(entity),
                contractState.apply(entity)
        );
    }

    @JsonIgnore
    public boolean isPrimaryContract() {
        return Objects.equals(id, primaryContractId);
    }
}
