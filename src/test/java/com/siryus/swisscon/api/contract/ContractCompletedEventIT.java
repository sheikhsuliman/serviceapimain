package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.contract.dto.ContractState;
import com.siryus.swisscon.api.contract.dto.ContractSummaryDTO;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
//TODO This Test needs to be completed, as soon as the whole contract workflow is implemented
//TODO ATM we cannot start working on the task > if the contract is not started as well
public class ContractCompletedEventIT extends AbstractMvcTestBase {

    private static final String CONTRACT_NAME = "contract";
    private static final String TASK_NAME_1 = "task 1";

    private Integer contractId;
    private Integer task1Id;
    private Integer contractTask1Id;

    private TestHelper.ExtendedTestProject testProject;

    @BeforeAll
    public void beforeAll() {
        testProject = testHelper.createExtendedProject();
        task1Id = addTaskToProject(TASK_NAME_1).getId();
        contractId = testHelper.createContract(testProject.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(CONTRACT_NAME, testProject.ownerCompany.projectId)).getId();
        testHelper.addContractTasks(testProject.ownerCompany.asOwner,
                TestBuilder.testCreateContractAddTasksRequest(contractId, Collections.singletonList(task1Id)));

        testHelper.startTimer(testProject.ownerCompany.asOwner,
                TestBuilder.testTaskWorklogRequest(task1Id, null, "Started"));
        testHelper.stopTimer(testProject.ownerCompany.asOwner,
                TestBuilder.testTaskWorklogRequest(task1Id, null, "Stopped"));
        testHelper.completeTask(testProject.ownerCompany.asOwner,
                TestBuilder.testTaskWorklogRequest(task1Id, null, "completed"));
    }

    @Test
    public void Given_IncompletedTask_When_approveTask_Then_CompleteContractAsWell() {
        testHelper.approveTask(testProject.ownerCompany.asOwner, TestBuilder.testTaskWorklogRequest(task1Id, null, "approved"));
        testHelper.approveTask(testProject.ownerCompany.asOwner, TestBuilder.testTaskWorklogRequest(task1Id, null, "approved 2"));
        List<ContractSummaryDTO> contractSummaryDTOS = testHelper.listContracts(testProject.ownerCompany.asOwner,
                TestBuilder.testCreateListContractRequest(testProject.projectId, testProject.ownerCompany.companyId));
        assertEquals(1, contractSummaryDTOS.size());
        assertEquals(ContractState.CONTRACT_COMPLETED, contractSummaryDTOS.get(0).getContractState());
    }



    private MainTaskDTO addTaskToProject(String taskName) {
        MainTaskDTO mainTaskDTO = testHelper.addContractualTask(testProject.ownerCompany.asAdmin,
                TestBuilder.testCreateMainTaskRequest(testProject.ownerCompany.topLocationId,
                        taskName,
                        testProject.ownerCompany.companyId));
        testProject.addMainTask(taskName, mainTaskDTO.getId());
        return mainTaskDTO;
    }
}
