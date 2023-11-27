package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.contract.dto.ContractState;
import com.siryus.swisscon.api.contract.repos.ContractLogRepository;
import com.siryus.swisscon.api.contract.repos.ContractTaskEntity;
import com.siryus.swisscon.api.contract.repos.ContractTaskRepository;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TaskToContractResolver {
    private final ContractTaskRepository contractTaskRepository;
    private final ContractLogRepository logRepository;

    @Autowired
    public TaskToContractResolver(
            ContractTaskRepository contractTaskRepository,
            ContractLogRepository logRepository
    ) {
        this.contractTaskRepository = contractTaskRepository;
        this.logRepository = logRepository;
    }

    public Optional<Integer> taskContractId(@Reference(ReferenceType.MAIN_TASK) Integer taskId) {
        return contractTaskRepository.findByTask(taskId).stream()
                .filter(t -> ! getContractState(t.getContractId()).isDeclined())
                .map(ContractTaskEntity::getContractId)
                .findFirst();
    }

    ContractState getContractState(@Reference(ReferenceType.CONTRACT) Integer contractId) {
        return logRepository.findTopLogEntry(contractId)
                .orElseThrow(() -> ContractExceptions.contractDoesNotExist(contractId)).getContractState();
    }


}
