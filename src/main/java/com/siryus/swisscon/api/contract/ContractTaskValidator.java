package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.contract.dto.ContractState;
import com.siryus.swisscon.api.contract.repos.ContractTaskEntity;
import com.siryus.swisscon.api.contract.repos.ContractTaskRepository;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
class ContractTaskValidator {

    private final ContractTaskRepository contractTaskRepository;
    private final ContractEventLogService contractEventLogService;

    @Autowired
    ContractTaskValidator(ContractTaskRepository contractTaskRepository, ContractEventLogService contractEventLogService) {
        this.contractTaskRepository = contractTaskRepository;
        this.contractEventLogService = contractEventLogService;
    }

    void validateTasksToAdd(Integer contractId, Integer projectId, List<MainTaskDTO> tasks) {
        validateContractStatus(contractId, projectId);
        validateTasksAreInSameProject(tasks, contractId, projectId);
        validateNoDuplicateTaskIds(tasks);
        validateNoDuplicateExistingTasks(tasks, contractId);
        validateTaskStatusToAdd(tasks);
        validateTaskInOtherContracts(tasks, contractId);
    }

    void validateTaskToModify(ContractTaskEntity contractTask, MainTaskDTO mainTaskDTO) {
        validateContractTaskModifyable(contractTask);
        validateContractStatus(contractTask.getContractId(), contractTask.getProjectId());
        validateTaskStatusToRemove(mainTaskDTO);
    }

    private void validateContractTaskModifyable(ContractTaskEntity contractTask) {
        ValidationUtils.throwIfNotNull(
                contractTask.getNegatedContractTaskId(),
                () -> ContractExceptions.negatedTaskCanNotBeModified(contractTask.getId())
        );
    }

    void validateAddableTasks(Integer contractId, Integer projectId) {
        validateContractStatus(contractId, projectId);
    }

    private void validateContractStatus(Integer contractId, Integer projectId) {
        ContractState state = contractEventLogService.getContractState(contractId);
        ValidationUtils.throwIfNot(state.isMutable(), () -> ContractExceptions.contractIsInImmutableState(
                contractId,
                projectId,
                state));
    }

    private void validateTasksAreInSameProject(List<MainTaskDTO> taskIds, Integer contractId, Integer projectId) {
        taskIds.forEach(t -> ValidationUtils.throwIfNot(projectId.equals(t.getProjectId()),
                () -> ContractExceptions.taskDoesNotBelongToSameProjectAsContract(contractId,
                        projectId)));
    }

    private void validateNoDuplicateTaskIds(List<MainTaskDTO> tasks) {
        int uniqueIdSize = tasks.stream().map(MainTaskDTO::getId).collect(Collectors.toSet()).size();
        ValidationUtils.throwIfNot(uniqueIdSize == tasks.size(), ContractExceptions::duplicateTaskIdsInAddTasksRequest);
    }

    private void validateNoDuplicateExistingTasks(List<MainTaskDTO> tasks, Integer contractId) {
        Set<Integer> existingTaskIds = new HashSet<>(contractTaskRepository.findTaskIdsByContract(contractId));
        tasks.forEach(nt -> ValidationUtils.throwIf(existingTaskIds.contains(nt.getId()),
                () -> ContractExceptions.taskAlreadyExistsInContract(contractId, nt.getId())));
    }

    private void validateTaskStatusToAdd(List<MainTaskDTO> tasks) {
        tasks.forEach(t -> ValidationUtils.throwIfNot(t.getStatus().isAddableToContract(),
                () -> ContractExceptions.taskHasNotTheInitialStatus(t.getId(), t.getStatus().name())));
    }

    private void validateTaskStatusToRemove(MainTaskDTO task) {
        ValidationUtils.throwIfNot(task.getStatus().isRemovableFromContract(),
                ()->ContractExceptions.taskHasNotTheRightStatusToRemove(task.getId(), task.getStatus().name()));
    }

    private void validateTaskInOtherContracts(List<MainTaskDTO> tasks, Integer contractId) {
        tasks.forEach(t -> contractTaskRepository.findContractIdsByTask(t.getId())
                .forEach(cId -> ValidationUtils.throwIf(taskIsInOtherActiveContract(cId, contractId),
                        () -> ContractExceptions.taskIsAlreadyPartOfAnotherActiveContract(t.getId(), cId))));
    }

    private boolean taskIsInOtherActiveContract(Integer otherContractId, Integer contractId) {
        return !otherContractId.equals(contractId) &&
                !contractEventLogService.getContractState(otherContractId).isDeclined();
    }
}
