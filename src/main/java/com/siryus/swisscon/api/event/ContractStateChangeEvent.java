package com.siryus.swisscon.api.event;

import com.siryus.swisscon.api.contract.dto.ContractEvent;
import com.siryus.swisscon.api.contract.dto.ContractEventLogDTO;
import com.siryus.swisscon.api.contract.dto.ContractState;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
@Getter
public class ContractStateChangeEvent implements CustomEvent {
    private final Integer projectId;
    private final Integer contractId;

    private final ContractEvent event;
    private final ContractState contractState;

    private final Integer eventInitiatorId;

    private final Integer recipientCompanyId;
    private final Map<String, String> variables;

    private final List<Integer> contractTaskIds;
    private final List<Integer> negatedTaskIds;

    public ContractStateChangeEvent(
            Integer projectId,
            Integer contractId,
            ContractEvent event,
            ContractState contractState,
            Integer eventInitiatorId,
            Integer recipientCompanyId,
            Map<String, String> variables,
            List<Integer> contractTaskIds,
            List<Integer> negatedTaskIds
    ) {
        this.projectId = projectId;
        this.contractId = contractId;
        this.event = event;
        this.contractState = contractState;
        this.eventInitiatorId = eventInitiatorId;
        this.recipientCompanyId = recipientCompanyId;
        this.variables = variables;
        this.contractTaskIds = contractTaskIds;
        this.negatedTaskIds = negatedTaskIds;
    }

    public static ContractStateChangeEvent from(ContractEventLogDTO dto) {
        return ContractStateChangeEvent.builder()
                .projectId(dto.getProjectId())
                .contractId(dto.getContractId())
                .event(dto.getEvent())
                .contractState(dto.getContractState())
            .build();
    }
}
