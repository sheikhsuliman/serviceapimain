package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.contract.dto.ContractState;
import com.siryus.swisscon.api.contract.repos.ContractEntity;
import com.siryus.swisscon.api.contract.repos.ContractTaskEntity;
import com.siryus.swisscon.api.contract.repos.ContractTaskRepository;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContractTaskValidatorTest {

    private static ContractTaskValidator validator;
    private static ContractEntity contract;
    private static ContractTaskEntity contractTask;
    private static List<MainTaskDTO> taskDTOs;
    private static List<MainTaskEntity> taskEntities;
    private static final Integer projectId = 1;

    private final ContractTaskRepository mockContractTaskRepository = Mockito.mock(ContractTaskRepository.class);
    private final ContractEventLogService mockContractEventLogService = Mockito.mock(ContractEventLogService.class);

    @BeforeEach
    public void initValidatorAndMocks() {
        validator = new ContractTaskValidator(mockContractTaskRepository, mockContractEventLogService);
        contract = ContractEntity.ref(projectId).toBuilder().projectId(projectId).build();
        taskDTOs = Stream.of(createTestTask(1, projectId, TaskStatus.DRAFT)).collect(Collectors.toList());
        taskEntities = toEntities(taskDTOs);
        contractTask = ContractTaskEntity.builder().contractId(contract.getId()).taskId(taskDTOs.get(0).getId()).build();

        Mockito.when(mockContractEventLogService.getContractState(1)).thenReturn(ContractState.CONTRACT_DRAFT);
    }

    @Test
    public void Given_validContractAndTask_When_validateTasksToAdd_Then_success() {
        validator.validateTasksToAdd(contract.getId(), projectId, taskDTOs);
    }

    @Test
    public void Given_immutableContract_When_validateTasksToAdd_Then_throw() {
        Mockito.when(mockContractEventLogService.getContractState(1)).thenReturn(ContractState.CONTRACT_IN_PROGRESS);
        TestAssert.assertErrorFromExecution(HttpStatus.BAD_REQUEST,
                ContractExceptions.CONTRACT_IS_IMMUTABLE.getErrorCode(),
                () -> validator.validateTasksToAdd(contract.getId(), projectId, taskDTOs));
    }

    @Test
    public void Given_tasksFromDifferentProjects_When_validateTasksToAdd_Then_throw() {
        taskDTOs.add(createTestTask(1, 2, TaskStatus.OPEN));
        TestAssert.assertErrorFromExecution(HttpStatus.BAD_REQUEST,
                ContractExceptions.TASK_DOES_NOT_BELONG_TO_SAME_PROJECT_AS_CONTRACT.getErrorCode(),
                () -> validator.validateTasksToAdd(contract.getId(), projectId, taskDTOs));
    }

    @Test
    public void Given_duplicateIdsInRequest_When_validateTasksToAdd_Then_throw() {
        taskDTOs.add(createTestTask(1, 1, TaskStatus.OPEN));
        TestAssert.assertErrorFromExecution(HttpStatus.BAD_REQUEST,
                ContractExceptions.DUPLICATE_TASK_IDS_IN_ADD_TASKS_REQUEST.getErrorCode(),
                () -> validator.validateTasksToAdd(contract.getId(), projectId, taskDTOs));
    }

    @Test
    public void Given_duplicateTasksInExistingContract_When_validateTasksToAdd_Then_throw() {
        Mockito.when(mockContractTaskRepository.findTaskIdsByContract(contract.getId()))
                .thenReturn(Collections.singletonList(taskEntities.get(0).getId()));
        TestAssert.assertErrorFromExecution(HttpStatus.BAD_REQUEST,
                ContractExceptions.TASK_ALREADY_EXISTS_IN_CONTRACT.getErrorCode(),
                () -> validator.validateTasksToAdd(contract.getId(), projectId, taskDTOs));
    }

    @Test
    public void Given_taskInProgress_When_validateTasksToAdd_Then_throw() {
        taskDTOs.set(0, taskDTOs.get(0).toBuilder().status(TaskStatus.IN_PROGRESS).build());
        TestAssert.assertErrorFromExecution(HttpStatus.BAD_REQUEST,
                ContractExceptions.TASK_HAS_NOT_THE_INITIAL_STATUS.getErrorCode(),
                () -> validator.validateTasksToAdd(contract.getId(), projectId, taskDTOs));
    }

    @Test
    public void Given_taskIsInAnotherActiveContract_When_validateTasksToAdd_Then_throw() {
        Mockito.when(mockContractTaskRepository.findContractIdsByTask(1))
                .thenReturn(Collections.singletonList(2));
        Mockito.when(mockContractEventLogService.getContractState(2)).thenReturn(ContractState.CONTRACT_OFFER_MADE);

        TestAssert.assertErrorFromExecution(HttpStatus.BAD_REQUEST,
                ContractExceptions.TASK_IS_ALREADY_PART_OF_ANOTHER_ACTIVE_CONTRACT.getErrorCode(),
                () -> validator.validateTasksToAdd(contract.getId(), projectId, taskDTOs));
    }

    @Test
    public void Given_validContractTask_When_validateTasksToRemove_Then_success() {
        validator.validateTaskToModify(contractTask, taskDTOs.get(0));
    }

    @Test
    public void Given_immutableContract_When_validateTasksToRemove_Then_throw() {
        Mockito.when(mockContractEventLogService.getContractState(1)).thenReturn(ContractState.CONTRACT_IN_PROGRESS);
        TestAssert.assertErrorFromExecution(HttpStatus.BAD_REQUEST,
                ContractExceptions.CONTRACT_IS_IMMUTABLE.getErrorCode(),
                () -> validator.validateTaskToModify(contractTask, taskDTOs.get(0)));
    }

    @Test
    public void Given_taskInProgress_When_validateTasksToRemove_Then_throw() {
        taskDTOs.set(0, taskDTOs.get(0).toBuilder().status(TaskStatus.IN_PROGRESS).build());
        TestAssert.assertErrorFromExecution(HttpStatus.BAD_REQUEST,
                ContractExceptions.TASK_HAS_NOT_THE_RIGHT_STATUS_TO_REMOVE.getErrorCode(),
                () -> validator.validateTaskToModify(contractTask, taskDTOs.get(0)));
    }

    @Test
    public void Given_immutableContract_When_validateAvailableTasks_Then_throw() {
        Mockito.when(mockContractEventLogService.getContractState(1)).thenReturn(ContractState.CONTRACT_IN_PROGRESS);
        TestAssert.assertErrorFromExecution(HttpStatus.BAD_REQUEST,
                ContractExceptions.CONTRACT_IS_IMMUTABLE.getErrorCode(),
                () -> validator.validateAddableTasks(contract.getId(), projectId));
    }

    private MainTaskDTO createTestTask(Integer taskId, Integer projectId, TaskStatus status) {
        return MainTaskDTO.builder()
                .id(taskId)
                .projectId(projectId)
                .status(status)
                .build();
    }

    private List<MainTaskEntity> toEntities(List<MainTaskDTO> mainTaskDTOS) {
        return mainTaskDTOS.stream()
                .map(dto -> MainTaskEntity.ref(dto.getId()).toBuilder()
                        .projectId(dto.getProjectId())
                        .status(dto.getStatus()).build())
                .collect(Collectors.toList());
    }


}
