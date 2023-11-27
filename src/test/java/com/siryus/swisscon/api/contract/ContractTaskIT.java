package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.FailFastExtension;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.contract.dto.ContractAddTasksRequest;
import com.siryus.swisscon.api.contract.dto.ContractTaskDTO;
import com.siryus.swisscon.api.contract.dto.ContractUpdateTaskRequest;
import com.siryus.swisscon.api.tasks.dto.ListTaskIdsRequest;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.util.validator.ValidationExceptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FailFastExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ContractTaskIT extends AbstractMvcTestBase {

    private static final String CONTRACT_NAME = "contract";
    private static final String CONTRACT_NAME_2 = "contract 2";
    private static final String TASK_NAME_1 = "task 1";
    private static final String TASK_NAME_2 = "task 2";
    private static final String TASK_NAME_3 = "task 3";

    private Integer contractId;
    private Integer contract2Id;
    private Integer task1Id;
    private Integer task2Id;
    private Integer task3Id;
    private Integer contractTask1Id;
    private Integer contractTask2Id;

    private TestHelper.ExtendedTestProject testProject;

    @BeforeAll
    public void beforeAll() {
        testProject = testHelper.createExtendedProject();
        task1Id = addTaskToProject(TASK_NAME_1).getId();
        task2Id = addTaskToProject(TASK_NAME_2).getId();
        task3Id = addTaskToProject(TASK_NAME_3).getId();

        testHelper.assignCustomerToProject(testProject.ownerCompany.asOwner, testProject.projectId,
                testProject.ownerCompany.companyId);

        contractId = testHelper.createContract(testProject.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(CONTRACT_NAME, testProject.ownerCompany.projectId)).getId();
        contract2Id = testHelper.createContract(testProject.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(CONTRACT_NAME_2, testProject.ownerCompany.projectId)).getId();
    }

    private MainTaskDTO addTaskToProject(String taskName) {
        MainTaskDTO mainTaskDTO = testHelper.addContractualTask(testProject.ownerCompany.asAdmin,
                TestBuilder.testCreateMainTaskRequest(testProject.ownerCompany.topLocationId,
                        taskName,
                        testProject.ownerCompany.companyId));
        testProject.addMainTask(taskName, mainTaskDTO.getId());
        return mainTaskDTO;
    }

    @Test
    @Order(1)
    public void GivenThreeValidTasks_When_availableTasksToAdd_Then_Show2Tasks() {
        List<MainTaskDTO> mainTaskDTOS = testHelper.availableContractTasksToAdd(testProject.ownerCompany.asOwner, contractId);
        assertEquals(3, mainTaskDTOS.size());
        assertTrue(mainTaskDTOS.stream().anyMatch(t -> t.getId().equals(task1Id)));
        assertTrue(mainTaskDTOS.stream().anyMatch(t -> t.getId().equals(task2Id)));
        assertTrue(mainTaskDTOS.stream().anyMatch(t -> t.getId().equals(task3Id)));
    }

    @Test
    @Order(2)
    public void Given_TwoValidTasks_When_addTaskToContract_Then_Success() {
        ContractAddTasksRequest contractAddTasksRequest = TestBuilder.testCreateContractAddTasksRequest(contractId,
                Arrays.asList(task1Id, task2Id));
        List<ContractTaskDTO> contractTaskDTOS = testHelper
                .addContractTasks(testProject.ownerCompany.asOwner, contractAddTasksRequest);
        contractTask1Id = getContractTaskId(contractTaskDTOS, task1Id);
        contractTask2Id = getContractTaskId(contractTaskDTOS, task2Id);

        testAssert.assertContractAddTask(contractAddTasksRequest, testProject.ownerCompany.topLocationId, contractTaskDTOS);
    }

    @Test
    @Order(3)
    public void GivenTwoValidContractTasks_When_loadTaskAndSubTaskIds_Then_ShowAllTasks() {
        final Map<Integer, List<Integer>> taskMap = testHelper.listTaskAndSubTaskIdsByContract(testProject.ownerCompany.asOwner, contractId, ListTaskIdsRequest.builder().build());
        assertEquals(2, taskMap.size());
        taskMap.values().forEach(subTaskIds -> assertFalse(subTaskIds.isEmpty()));
    }

    @Test
    @Order(4)
    public void GivenTwoValidTasksAddedAlreadyToContract_When_availableTasksToAdd_Then_ShowTask3() {
        List<MainTaskDTO> mainTaskDTOS = testHelper.availableContractTasksToAdd(testProject.ownerCompany.asOwner, contractId);
        assertEquals(1, mainTaskDTOS.size());
        assertTrue(mainTaskDTOS.stream().anyMatch(t -> t.getId().equals(task3Id)));
    }

    @Test
    @Order(5)
    public void Given_DuplicateTask_When_addTasksToContract_Then_Throw() {
        testHelper
                .addContractTasks(testProject.ownerCompany.asOwner,
                        TestBuilder.testCreateContractAddTasksRequest(contractId, Collections.singletonList(task1Id)),
                        r -> {
                            TestAssert.assertError(HttpStatus.BAD_REQUEST, ContractExceptions.TASK_ALREADY_EXISTS_IN_CONTRACT.getErrorCode(), r);
                            return null;
                        });
    }

    @Test
    @Order(6)
    public void Given_TwoAddedTasks_When_listContractTasks_Then_Success() {
        List<ContractTaskDTO> contractTaskDTOS = testHelper.listContractTasks(testProject.ownerCompany.asOwner, contractId);
        testAssert.assertListContractTask(task1Id, testProject.ownerCompany.topLocationId, contractTaskDTOS);
    }

    @Test
    @Order(7)
    public void Given_TwoAddedTasks_When_RemoveTaskFromContract_Then_Success() {
        testHelper.removeContractTask(testProject.ownerCompany.asOwner, contractTask2Id);

        List<ContractTaskDTO> contractTaskDTOS = testHelper.listContractTasks(testProject.ownerCompany.asOwner, contractId);

        testAssert.assertListContractTask(task1Id, testProject.ownerCompany.topLocationId, contractTaskDTOS);
        assertEquals(1, contractTaskDTOS.size());
    }

    @Test
    @Order(8)
    public void GivenOneTaskAddedAndOneRemovedFromContract_When_availableTasksToAdd_Then_Show1Task() {
        List<MainTaskDTO> mainTaskDTOS = testHelper.availableContractTasksToAdd(testProject.ownerCompany.asOwner, contractId);
        assertEquals(2, mainTaskDTOS.size());
        assertTrue(mainTaskDTOS.stream().anyMatch(t -> t.getId().equals(task2Id)));
        assertTrue(mainTaskDTOS.stream().anyMatch(t -> t.getId().equals(task3Id)));
    }

    @Test
    @Order(9)
    public void Given_TaskNotPartOfContract_When_removeTaskFromContract_Then_Throw() {
        testHelper.removeContractTask(testProject.ownerCompany.asOwner, contractTask2Id,
                r -> TestAssert.assertError(HttpStatus.BAD_REQUEST, ValidationExceptions.NOT_VALID_REFERENCE.getErrorCode(), r));
    }

    @Test
    @Order(10)
    public void Given_task3AddedToAnotherTask_When_availableTaskToAdd_Then_dontShowTask3() {
        ContractAddTasksRequest contractAddTasksRequest = TestBuilder.testCreateContractAddTasksRequest(contract2Id,
                Collections.singletonList(task3Id));
        List<ContractTaskDTO> contractTaskDTOS = testHelper
                .addContractTasks(testProject.ownerCompany.asOwner, contractAddTasksRequest);
        testAssert.assertContractAddTask(contractAddTasksRequest, testProject.ownerCompany.topLocationId, contractTaskDTOS);

        // For contract 1 > only task 2 is available
        List<MainTaskDTO> mainTaskDTOS = testHelper.availableContractTasksToAdd(testProject.ownerCompany.asOwner, contractId);
        assertEquals(1, mainTaskDTOS.size());
        assertTrue(mainTaskDTOS.stream().anyMatch(t -> t.getId().equals(task2Id)));
    }

    @Test
    @Order(11)
    public void Given_task2WhichIsNotPartOfAnyContract_When_updateTask_Then_Throw() {
        testHelper.updateContractTask(testProject.contractorCompany.asOwner,
                testBuilder.testCreateContractUpdateTaskRequest(contractTask2Id, "m2", "2.54", "14.50"),
                r -> {
                    TestAssert.assertError(HttpStatus.BAD_REQUEST, ValidationExceptions.NOT_VALID_REFERENCE.getErrorCode(), r);
                    return null;
                });
    }

    @Test
    @Order(12)
    public void Given_task1WhichIsPartOfContract_When_updateTask_Then_Success() {
        ContractUpdateTaskRequest updateTaskRequest = testBuilder.testCreateContractUpdateTaskRequest(contractTask1Id, "m2", "2.54", "14.50");
        ContractTaskDTO contractTaskDTO = testHelper.updateContractTask(testProject.contractorCompany.asOwner, updateTaskRequest);
        testAssert.assertContractUpdateTask(updateTaskRequest, "36.83", contractTaskDTO);
    }

    @Test
    @Order(13)
    public void Given_amountOrPricePerUnitIsNull_When_updateTask_Then_TotalPriceIsNullToo() {
        ContractUpdateTaskRequest updateTaskRequest = testBuilder.testCreateContractUpdateTaskRequest(contractTask1Id, "m2", null, "14.50");
        ContractTaskDTO contractTaskDTO = testHelper.updateContractTask(testProject.contractorCompany.asOwner, updateTaskRequest);
        testAssert.assertContractUpdateTask(updateTaskRequest, null, contractTaskDTO);

        ContractUpdateTaskRequest updateTaskRequest2 = testBuilder.testCreateContractUpdateTaskRequest(contractTask1Id, "m2", "4.58", null);
        ContractTaskDTO contractTaskDTO2 = testHelper.updateContractTask(testProject.contractorCompany.asOwner, updateTaskRequest2);
        testAssert.assertContractUpdateTask(updateTaskRequest2, null, contractTaskDTO2);

        ContractUpdateTaskRequest updateTaskRequest3 = testBuilder.testCreateContractUpdateTaskRequest(contractTask1Id, "m2", null, null);
        ContractTaskDTO contractTaskDTO3 = testHelper.updateContractTask(testProject.contractorCompany.asOwner, updateTaskRequest3);
        testAssert.assertContractUpdateTask(updateTaskRequest3, null, contractTaskDTO3);
    }

    private Integer getContractTaskId(List<ContractTaskDTO> contractTaskDTOS, Integer taskId) {
        return contractTaskDTOS.stream()
                .filter(dto -> dto.getTask().getId().equals(taskId)).findFirst()
                .orElseThrow().getContractTaskId();
    }

}
