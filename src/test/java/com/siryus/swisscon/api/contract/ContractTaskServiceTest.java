package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.contract.dto.ContractState;
import com.siryus.swisscon.api.contract.repos.ContractEntity;
import com.siryus.swisscon.api.contract.repos.ContractTaskEntity;
import com.siryus.swisscon.api.contract.repos.ContractTaskRepository;
import com.siryus.swisscon.api.event.TaskCompletedEvent;
import com.siryus.swisscon.api.event.TaskTimerStartedEvent;
import com.siryus.swisscon.api.general.unit.Unit;
import com.siryus.swisscon.api.general.unit.UnitService;
import com.siryus.swisscon.api.tasks.BasicMainTaskService;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import com.siryus.swisscon.api.taskworklog.TaskWorkLogWriterService;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;

@SuppressWarnings("FieldCanBeLocal")
public class ContractTaskServiceTest {

    private final ContractTasksReader mockTasksReader = Mockito.mock(ContractTasksReader.class);
    private final ContractBaseService mockContractBaseService = Mockito.mock(ContractBaseService.class);
    private final ContractTaskRepository mockContractTaskRepository = Mockito.mock(ContractTaskRepository.class);
    private final BasicMainTaskService mockBasicMainTaskService = Mockito.mock(BasicMainTaskService.class);
    private final ContractEventLogService mockContractEventLogService = Mockito.mock(ContractEventLogService.class);
    private final SecurityHelper mockSecurityHelper = Mockito.mock(SecurityHelper.class);
    private final TaskWorkLogWriterService mockTaskWorkLogWriterService = Mockito.mock(TaskWorkLogWriterService.class);
    private final UnitService mockUnitService = Mockito.mock(UnitService.class);

    private final ContractTaskService contractTaskService;

    private static MainTaskEntity task1;
    private static MainTaskEntity task2;
    private static MainTaskDTO task1Dto;
    private static MainTaskDTO task2Dto;
    private static ContractEntity contract1;
    private static ContractEntity contract2;
    private static ContractTaskEntity contractTaskEntity1;
    private static ContractTaskEntity contractTaskEntity2;
    private static ContractTaskEntity contractTaskEntity3;

    public ContractTaskServiceTest() {
        contractTaskService = new ContractTaskService(
                mockTasksReader,
                mockContractBaseService,
                mockContractTaskRepository,
                new ContractTaskValidator(mockContractTaskRepository, mockContractEventLogService),
                mockTaskWorkLogWriterService,
                mockSecurityHelper,
                mockBasicMainTaskService,
                mockContractEventLogService,
                mockUnitService
        );
    }

    @BeforeEach()
    public void init() {
        task1 = MainTaskEntity.ref(1).toBuilder().status(TaskStatus.COMPLETED).build();
        task2 = MainTaskEntity.ref(2).toBuilder().status(TaskStatus.IN_REVIEW).build();
        task1Dto = MainTaskDTO.builder().id(task1.getId()).status(task1.getStatus()).build();
        task2Dto = MainTaskDTO.builder().id(task2.getId()).status(task2.getStatus()).build();
        contract1 = ContractEntity.ref(1);
        contract2 = ContractEntity.ref(2);
        contractTaskEntity1 = ContractTaskEntity.ref(1).toBuilder().taskId(task1.getId()).contractId(contract1.getId()).build();
        contractTaskEntity2 = ContractTaskEntity.ref(2).toBuilder().taskId(task2.getId()).contractId(contract1.getId()).build();
        contractTaskEntity3 = ContractTaskEntity.ref(3).toBuilder().taskId(task1.getId()).contractId(contract2.getId()).build();

        Mockito.when(mockContractTaskRepository.findByTask(task1.getId()))
                .thenReturn(Arrays.asList(contractTaskEntity1, contractTaskEntity3));
        Mockito.when(mockContractEventLogService.getContractState(contract1.getId()))
                .thenReturn(ContractState.CONTRACT_IN_PROGRESS);
        Mockito.when(mockContractEventLogService.getContractState(contract2.getId()))
                .thenReturn(ContractState.CONTRACT_DECLINED);
        Mockito.when(mockUnitService.findBySymbolName(any()))
                .thenReturn(Unit.ref(1));
        Mockito.when(mockBasicMainTaskService.getMainTask(task1.getId()))
                .thenReturn(task1Dto);
        Mockito.when(mockBasicMainTaskService.getMainTask(task2.getId()))
                .thenReturn(task2Dto);
    }

    @Test
    public void Given_taskPartOfMultipleNonDeclinedContracts_When_handleTaskCompletedEvent_Then_throw() {
        Mockito.when(mockContractEventLogService.getContractState(contract2.getId()))
                .thenReturn(ContractState.CONTRACT_IN_PROGRESS);

        TestAssert.assertErrorFromExecution(HttpStatus.INTERNAL_SERVER_ERROR,
                ContractExceptions.TASK_IS_PART_OF_MULTIPLE_NON_DECLINED_CONTRACTS.getErrorCode(),
                ()->contractTaskService.handleTaskCompletedEvent(new TaskCompletedEvent(task1.getId())));
    }

    @Test
    public void Given_notAllTasksAreCompleted_When_handleTaskCompletedEvent_Then_dontSetTheContractToCompleted() {
        Mockito.when(mockContractTaskRepository.findByContract(contract1.getId()))
                .thenReturn(Arrays.asList(contractTaskEntity1, contractTaskEntity2));

        contractTaskService.handleTaskCompletedEvent(new TaskCompletedEvent(task1.getId()));
        Mockito.verify(mockContractEventLogService, Mockito.never()).completeContract(contract1.getId());
    }

    @Test
    public void Given_allTasksAreCompleted_When_handleTaskCompletedEvent_Then_newContractCompletedEventIsLogged() {
        task2.setStatus(TaskStatus.COMPLETED);
        task2Dto = task2Dto.toBuilder().status(TaskStatus.COMPLETED).build();
        Mockito.when(mockBasicMainTaskService.getMainTask(task2.getId()))
                .thenReturn(task2Dto);
        Mockito.when(mockContractTaskRepository.findByContract(contract1.getId()))
                .thenReturn(Arrays.asList(contractTaskEntity1, contractTaskEntity2));

        contractTaskService.handleTaskCompletedEvent(new TaskCompletedEvent(task1.getId()));
        Mockito.verify(mockContractEventLogService, Mockito.times(1)).completeContract(contract1.getId());
    }

    @Test
    public void Given_contractInStateAccepted_When_handleTaskStartedEvent_Then_publishEventWorkStarted() {
        Mockito.when(mockContractEventLogService.getContractState(contract1.getId()))
                .thenReturn(ContractState.CONTRACT_ACCEPTED);

        contractTaskService.handleTaskStartedEvent(new TaskTimerStartedEvent(task1.getId()));
        Mockito.verify(mockContractEventLogService, Mockito.times(1)).startContract(contract1.getId());
    }

    @Test
    public void Given_contractInStateInProgress_When_handleTaskStartedEvent_Then_publishEventWorkStarted() {
        Mockito.when(mockContractEventLogService.getContractState(contract1.getId()))
                .thenReturn(ContractState.CONTRACT_IN_PROGRESS);

        contractTaskService.handleTaskStartedEvent(new TaskTimerStartedEvent(task1.getId()));
        Mockito.verify(mockContractEventLogService, Mockito.never()).startContract(contract1.getId());
    }

}
