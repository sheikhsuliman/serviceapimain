package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.contract.dto.ContractAddTasksRequest;
import com.siryus.swisscon.api.contract.dto.ContractDTO;
import com.siryus.swisscon.api.contract.dto.ContractTaskDTO;
import com.siryus.swisscon.api.contract.dto.ContractUpdateTaskRequest;
import com.siryus.swisscon.api.contract.dto.ListContractsRequest;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractExtensionsIT extends AbstractMvcTestBase {
    private static final AtomicInteger TEST_COUNTER = new AtomicInteger(0);
    private static final String TOP_LOCATION = "TOP";
    private static final String TASK_A = "task-a";
    private static final String TASK_B = "task-b";
    private static final String TASK_C = "task-c";
    private static final String TASK_D = "task-d";

    private static final String PRIMARY_CONTRACT = "primary-contract";

    private static final Integer WHEELBARROW = 46;
    private static final String CONTRACT_NAME = "Fun On The Bun";
    private static final String CONTRACT_DESCRIPTION = "Reference to Futurama";

    TestHelper.ExtendedTestProject newTestProject(Integer thisTestId) {
        var testProject = testHelper.createExtendedProject(
                TestHelper.COMPANY_NAME + "_" + thisTestId,
                  TestHelper.CONTRACTOR_COMPANY_NAME + "_" + thisTestId
        );

        testHelper.assignCustomerToProject(
                testProject.ownerCompany.asOwner,
                testProject.ownerCompany.projectId,
                testProject.contractorCompany.companyId
        );

        testHelper.addLocationToExtendedProject(testProject, TOP_LOCATION);

        List.of(TASK_A, TASK_B, TASK_C, TASK_D).forEach(
                taskName -> testProject.addMainTask(
                        taskName,
                        testHelper.addContractualTask(
                                testProject.ownerCompany.asAdmin,
                                TestBuilder.testCreateMainTaskRequest(
                                        testProject.locationId(TOP_LOCATION),
                                        taskName,
                                        testProject.ownerCompany.companyId
                                )
                        ).getId()
                )
        );

        ContractDTO primaryContract = testHelper.createContract(
                testProject.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(CONTRACT_NAME + "_PRIMARY_" + thisTestId, testProject.ownerCompany.projectId));

        testProject.addContract(PRIMARY_CONTRACT, primaryContract.getId());

        var contractTasks = testHelper.addContractTasks(
                testProject.ownerCompany.asOwner,
                TestBuilder.testCreateContractAddTasksRequest(
                        primaryContract.getId(),
                        List.of(testProject.mainTaskId(TASK_A), testProject.mainTaskId(TASK_B), testProject.mainTaskId(TASK_C))
                )
        );

        contractTasks.forEach(
                task -> testHelper.updateContractTask(testProject.ownerCompany.asOwner, new ContractUpdateTaskRequest(
                        task.getContractTaskId(),
                        WHEELBARROW,
                        BigDecimal.ONE,
                        BigDecimal.ONE
                ))
        );

        testHelper.selfAcceptOffer(testProject.ownerCompany.asOwner, primaryContract.getId(), TestBuilder.testSendMessageRequest(
                testProject.contractorCompany.companyId, "Hello"));

        return testProject;
    }

    @Test
    void Given_primaryContractExists_When_extensionAdded_Then_listShowsOnlyPrimaryContracts() {
        var project = newTestProject(TEST_COUNTER.addAndGet(1));
        var contractsBeforeExtension = testHelper.listContracts(project.ownerCompany.asOwner, ListContractsRequest.builder().projectId(project.projectId).build());

        ContractDTO contractExtension1 = testHelper.createContract(project.ownerCompany.asOwner, TestBuilder.testCreateContractRequest(project.contractId(PRIMARY_CONTRACT), CONTRACT_NAME + "_SUB_1", project.ownerCompany.projectId));

        var contractsAfterExtension = testHelper.listContracts(project.ownerCompany.asOwner, ListContractsRequest.builder().projectId(project.projectId).build());

        assertEquals(contractsBeforeExtension.size(), contractsAfterExtension.size());
    }

    @Test
    void Given_extensionNegateTaskC_When_listContracts_Then_primaryContractSummaryProperlyUpdated() {
        var project = newTestProject(TEST_COUNTER.addAndGet(1));

        var contractsBeforeExtension = testHelper.listContracts(project.ownerCompany.asOwner, ListContractsRequest.builder().projectId(project.projectId).build());

        ContractDTO contractExtension1 = testHelper.createContract(project.ownerCompany.asOwner, TestBuilder.testCreateContractRequest(project.contractId(PRIMARY_CONTRACT), CONTRACT_NAME + "_SUB_1", project.ownerCompany.projectId));

        var contractTasks = testHelper.listContractTasks(project.ownerCompany.asOwner, project.contractId(PRIMARY_CONTRACT));

        var negateContractTask = testHelper.negateContractTask(project.ownerCompany.asOwner, contractExtension1.getId(), contractTaskId( contractTasks, project.mainTaskId(TASK_C)));

        testHelper.selfAcceptOffer(project.ownerCompany.asOwner, contractExtension1.getId(), TestBuilder.testSendMessageRequest(
                project.contractorCompany.companyId, "Hello"));

        var contractsAfterExtension = testHelper.listContracts(project.ownerCompany.asOwner, ListContractsRequest.builder().projectId(project.projectId).build());

        var primaryContractBeforeExtension = contractsBeforeExtension.stream().filter(c -> c.getContractId().equals(project.contractId(PRIMARY_CONTRACT))).findFirst().get();
        var primaryContractAfterExtension = contractsAfterExtension.stream().filter(c -> c.getContractId().equals(project.contractId(PRIMARY_CONTRACT))).findFirst().get();

        assertEquals(primaryContractBeforeExtension.getNumberOfTasks()-1, primaryContractAfterExtension.getNumberOfTasks());
        assertEquals(primaryContractBeforeExtension.getTotalAmount().subtract(BigDecimal.ONE), primaryContractAfterExtension.getTotalAmount());
    }

    @Test
    void Given_extensionCreated_When_listPrimaryContractExtensions_Then_extensionListed() {
        var project = newTestProject(TEST_COUNTER.addAndGet(1));

        var contractsBeforeExtension = testHelper.listContracts(project.ownerCompany.asOwner, ListContractsRequest.builder().projectId(project.projectId).build());

        ContractDTO contractExtension1 = testHelper.createContract(project.ownerCompany.asOwner, TestBuilder.testCreateContractRequest(project.contractId(PRIMARY_CONTRACT), CONTRACT_NAME + "_SUB_1", project.ownerCompany.projectId));

        var contractExtensions = testHelper.listPrimaryContractExtensions(project.ownerCompany.asOwner, project.contractId(PRIMARY_CONTRACT));

        assertEquals(1 ,contractExtensions.size());
        assertEquals(contractExtension1.getId(), contractExtensions.get(0).getContractId());
    }

    @Test
    void Given_extensionCreated_When_listPrimaryContractTasks_Then_allTasksListed() {
        var project = newTestProject(TEST_COUNTER.addAndGet(1));
        var contractTasks = testHelper.listContractTasks(project.ownerCompany.asOwner, project.contractId(PRIMARY_CONTRACT));

        var contractsBeforeExtension = testHelper.listContracts(project.ownerCompany.asOwner, ListContractsRequest.builder().projectId(project.projectId).build());

        ContractDTO contractExtension1 = testHelper.createContract(project.ownerCompany.asOwner, TestBuilder.testCreateContractRequest(project.contractId(PRIMARY_CONTRACT), CONTRACT_NAME + "_SUB_1", project.ownerCompany.projectId));
        var negateContractTask = testHelper.negateContractTask(project.ownerCompany.asOwner, contractExtension1.getId(), contractTaskId( contractTasks, project.mainTaskId(TASK_C)));
        testHelper.addContractTasks(
                project.ownerCompany.asOwner,
                ContractAddTasksRequest.builder()
                    .contractId(contractExtension1.getId())
                    .taskIds(List.of(project.mainTaskId(TASK_D)))
                .build()
        );

        var primaryContractTasks  = testHelper.listPrimaryContractTasks(project.ownerCompany.asOwner, project.contractId(PRIMARY_CONTRACT));

        assertEquals(5 ,primaryContractTasks.size());
    }

    @Test
    void Given_extensionCreated_When_listNegateableTasks_Then_listCorrectTasks() {
        var project = newTestProject(TEST_COUNTER.addAndGet(1));
        var negateableTasksBeforeExtension = testHelper.listNegateableTasks(project.ownerCompany.asOwner, project.contractId(PRIMARY_CONTRACT));

        assertEquals(3, negateableTasksBeforeExtension.size());
        assertNotEquals(0, contractTaskId(negateableTasksBeforeExtension, project.mainTaskId(TASK_A)));
        assertNotEquals(0, contractTaskId(negateableTasksBeforeExtension, project.mainTaskId(TASK_B)));
        assertNotEquals(0, contractTaskId(negateableTasksBeforeExtension, project.mainTaskId(TASK_C)));
        assertEquals(0, contractTaskId(negateableTasksBeforeExtension, project.mainTaskId(TASK_D)));

        ContractDTO contractExtension1 = testHelper.createContract(project.ownerCompany.asOwner, TestBuilder.testCreateContractRequest(project.contractId(PRIMARY_CONTRACT), CONTRACT_NAME + "_SUB_1", project.ownerCompany.projectId));
        var negateContractTask = testHelper.negateContractTask(project.ownerCompany.asOwner, contractExtension1.getId(), contractTaskId( negateableTasksBeforeExtension, project.mainTaskId(TASK_C)));
        var addedTasks = testHelper.addContractTasks(
                project.ownerCompany.asOwner,
                ContractAddTasksRequest.builder()
                        .contractId(contractExtension1.getId())
                        .taskIds(List.of(project.mainTaskId(TASK_D)))
                        .build()
        );
        testHelper.updateContractTask(project.ownerCompany.asOwner, new ContractUpdateTaskRequest(
                addedTasks.get(0).getContractTaskId(),
                WHEELBARROW,
                BigDecimal.ONE,
                BigDecimal.ONE
        ));
        testHelper.selfAcceptOffer(project.ownerCompany.asOwner, contractExtension1.getId(), TestBuilder.testSendMessageRequest(
                project.contractorCompany.companyId, "Hello"));

        var negateableTasksAfterExtension = testHelper.listNegateableTasks(project.ownerCompany.asOwner, project.contractId(PRIMARY_CONTRACT));

        assertEquals(3 ,negateableTasksAfterExtension.size());
        assertNotEquals(0, contractTaskId(negateableTasksAfterExtension, project.mainTaskId(TASK_A)));
        assertNotEquals(0, contractTaskId(negateableTasksAfterExtension, project.mainTaskId(TASK_B)));
        assertEquals(0, contractTaskId(negateableTasksAfterExtension, project.mainTaskId(TASK_C)));
        assertNotEquals(0, contractTaskId(negateableTasksAfterExtension, project.mainTaskId(TASK_D)));
    }

    @Test
    void Given_primaryContractExists_When_extensionNegateTaskC_Then_taskCBecomeAvailableToReAddInAnotherExtension() {
        var project = newTestProject(TEST_COUNTER.addAndGet(1));

        ContractDTO contractExtension1 = testHelper.createContract(project.ownerCompany.asOwner, TestBuilder.testCreateContractRequest(project.contractId(PRIMARY_CONTRACT), CONTRACT_NAME + "_SUB_1", project.ownerCompany.projectId));

        var contractTasks = testHelper.listContractTasks(project.ownerCompany.asOwner, project.contractId(PRIMARY_CONTRACT));

        var negateContractTask = testHelper.negateContractTask(project.ownerCompany.asOwner, contractExtension1.getId(), contractTaskId( contractTasks, project.mainTaskId(TASK_C)));

        // TODO:
        // The task-c should become available at this point, so we can re-add it if needed... but as of now, it does not work :(
        // var availableTasks1 = testHelper.availableContractTasksToAdd(project.ownerCompany.asOwner, contractExtension1.getId());
        // assertTrue(containsTask(availableTasks1, TASK_C));

        testHelper.selfAcceptOffer(project.ownerCompany.asOwner, contractExtension1.getId(), TestBuilder.testSendMessageRequest(
                project.contractorCompany.companyId, "Hello"));

        ContractDTO contractExtension2 = testHelper.createContract(project.ownerCompany.asOwner, TestBuilder.testCreateContractRequest(project.contractId(PRIMARY_CONTRACT), CONTRACT_NAME + "_SUB_2", project.ownerCompany.projectId));

        var availableTasks2 = testHelper.availableContractTasksToAdd(project.ownerCompany.asOwner, contractExtension2.getId());

        assertTrue(containsTask(availableTasks2, TASK_C));
    }

    private Integer contractTaskId(List<ContractTaskDTO> contractTaskDTOS, Integer mainTaskId) {
        return contractTaskDTOS.stream().filter( c -> c.getTask().getId().equals(mainTaskId))
                .map( c -> c.getContractTaskId())
                .findFirst().orElse(0);
    }

    private boolean containsTask(List<MainTaskDTO> taskDTOS, String taskName) {
        return taskDTOS.stream().anyMatch( t -> t.getTitle().equals(taskName));
    }
}
