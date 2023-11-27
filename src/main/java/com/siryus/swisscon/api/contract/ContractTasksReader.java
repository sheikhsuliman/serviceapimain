package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.contract.dto.ContractTaskDTO;
import com.siryus.swisscon.api.contract.repos.ContractTaskEntity;
import com.siryus.swisscon.api.contract.repos.ContractTaskRepository;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.general.unit.UnitService;
import com.siryus.swisscon.api.tasks.BasicMainTaskService;
import com.siryus.swisscon.api.tasks.dto.ListTaskIdsRequest;
import com.siryus.swisscon.api.util.validator.Reference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ContractTasksReader {
    private final ContractTaskRepository contractTaskRepository;
    private final BasicMainTaskService basicMainTaskService;
    private final UnitService unitService;

    public ContractTasksReader(
            ContractTaskRepository contractTaskRepository,
            BasicMainTaskService basicMainTaskService,
            UnitService unitService
    ) {
        this.contractTaskRepository = contractTaskRepository;
        this.basicMainTaskService = basicMainTaskService;
        this.unitService = unitService;
    }

    public List<ContractTaskDTO> listTasks(@Reference(ReferenceType.CONTRACT) Integer contractId) {
        return contractTaskRepository
                .findByContract(contractId)
                .stream()
                .map(this::toContractTaskDTO)
                .collect(Collectors.toList());
    }

    public List<ContractTaskDTO> listTasksByPrimaryContract(@Reference(ReferenceType.CONTRACT) Integer primaryContractId) {
        return contractTaskRepository
                .findByPrimaryContract(primaryContractId)
                .stream()
                .map(this::toContractTaskDTO)
                .collect(Collectors.toList());
    }

    public Map<Integer, List<Integer>> listTaskAndSubTaskIdsByContract(@Reference(ReferenceType.CONTRACT) Integer contractId, ListTaskIdsRequest request) {
        return basicMainTaskService.listSubTaskIdsInMainTaskIds(
                contractTaskRepository
                        .findByContract(contractId)
                        .stream()
                        .map(ContractTaskEntity::getTaskId)
                        .distinct()
                        .collect(Collectors.toList()),
                request
        );
    }

    public ContractTaskDTO getTask(@Reference(ReferenceType.CONTRACT_TASK) Integer contractTaskId) {
        return toContractTaskDTO(
            contractTaskRepository.findById(contractTaskId)
                .orElseThrow(() -> ContractExceptions.contractTaskIdDoesNotExist(contractTaskId))
        );
    }

    ContractTaskDTO toContractTaskDTO(ContractTaskEntity contractTask) {
        return ContractTaskDTO.from(
                contractTask,
                basicMainTaskService.getMainTask(contractTask.getTaskId()),
                unitService.getUnitById(contractTask.getUnitId())
        );
    }
}
