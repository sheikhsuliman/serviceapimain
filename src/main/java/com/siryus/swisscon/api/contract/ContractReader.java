package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.contract.repos.ContractEntity;
import com.siryus.swisscon.api.contract.repos.ContractRepository;
import com.siryus.swisscon.api.contract.repos.ContractTaskEntity;
import com.siryus.swisscon.api.contract.repos.ContractTaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractReader {
    private final ContractRepository contractRepository;
    private final ContractTaskRepository contractTaskRepository;

    public ContractReader(
            ContractRepository contractRepository,
            ContractTaskRepository contractTaskRepository
    ) {
        this.contractRepository = contractRepository;
        this.contractTaskRepository = contractTaskRepository;
    }

    ContractEntity getValidContract(Integer contractId) {
        return getValidContract(contractId, false);
    }

    ContractEntity getValidContract(Integer contractId, boolean canBeDisabled) {
        var exitingContract = getExistingContract(contractId);

        if (canBeDisabled) {
            return exitingContract;
        }

        if (exitingContract.getDisabled() == null) {
            return exitingContract;
        }

        throw ContractExceptions.contractHasBenDeleted(contractId);
    }

    List<ContractEntity> getValidContractExtensions(Integer primaryContractId) {
        return contractRepository.findAllPrimaryContractExtensions(primaryContractId);
    }

    private ContractEntity getExistingContract(Integer contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> ContractExceptions.contractDoesNotExist(contractId));
    }

    List<ContractTaskEntity> listContractTasks(Integer contractId) {
        return contractTaskRepository.findByContract(contractId);
    }
}
