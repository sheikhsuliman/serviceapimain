package com.siryus.swisscon.api.contract.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.contract.repos.ContractEventLogEntity;
import com.siryus.swisscon.api.auth.user.AuthorDTO;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.function.Function;

@Builder(toBuilder = true)
@Getter
public class ContractEventLogDTO {
    private final Integer id;
    private final Integer projectId;
    private final Integer contractId;
    private final LocalDateTime created;

    private final AuthorDTO eventSender;
    private final ContractEvent event;
    private final ContractState contractState;
    private final Integer counterPartCompanyId;

    @JsonCreator
    public ContractEventLogDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("projectId") Integer projectId,
            @JsonProperty("contractId") Integer contractId,
            @JsonProperty("created") LocalDateTime created,
            @JsonProperty("eventSender") AuthorDTO eventSender,
            @JsonProperty("event") ContractEvent event,
            @JsonProperty("contractState") ContractState contractState,
            @JsonProperty("counterPartCompanyId") Integer counterPartCompanyId
    ) {
        this.id = id;
        this.projectId = projectId;
        this.contractId = contractId;
        this.created = created;
        this.eventSender = eventSender;
        this.event = event;
        this.contractState = contractState;
        this.counterPartCompanyId = counterPartCompanyId;
    }

    public static ContractEventLogDTO from(ContractEventLogEntity entity, Function<ContractEventLogEntity,AuthorDTO> author) {
        return new ContractEventLogDTO(
                entity.getId(),
                entity.getProjectId(),
                entity.getContractId(),
                entity.getCreatedDate(),
                author.apply(entity),
                entity.getEvent(),
                entity.getContractState(),
                entity.getCounterPartId()
        );
    }
}