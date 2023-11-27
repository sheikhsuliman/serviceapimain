package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.contract.dto.ContractDTO;
import com.siryus.swisscon.api.contract.dto.ContractUpdateTaskRequest;
import com.siryus.swisscon.api.exceptions.LocalizedReason;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContractBaseServiceIT extends AbstractMvcTestBase {
    private static final Integer WHEELBARROW = 46;
    private static final String CONTRACT_NAME = "Fun On The Bun";
    private static final String CONTRACT_DESCRIPTION = "Reference to Futurama";

    private static TestHelper.TestProject customerTestProject;
    private static TestHelper.TestProject contractorTestProject1;
    private static TestHelper.TestProject contractorTestProject2;
    private static TestHelper.TestProject contractorTestProject3;
    private static TestHelper.TestProject contractorTestProject4;

    @BeforeAll
    void initTest() {
        customerTestProject = testHelper.createProject(TestHelper.CUSTOMER_LAST_NAME,
                TestHelper.CONTRACTOR_COMPANY_NAME, RoleName.CUSTOMER, false);
        contractorTestProject1 = testHelper.createProject(TestHelper.COMPANY_NAME + "_1",
                                                          TestHelper.CONTRACTOR_COMPANY_NAME + "_1", RoleName.COMPANY_OWNER, false);
        testHelper.assignCustomerToProject(contractorTestProject1.ownerCompany.asOwner, contractorTestProject1.ownerCompany.projectId, contractorTestProject1.contractorCompany.companyId);

        contractorTestProject2 = testHelper.createProject(TestHelper.COMPANY_NAME + "_2",
                                                          TestHelper.CONTRACTOR_COMPANY_NAME + "_2", RoleName.COMPANY_OWNER, false);
        testHelper.assignCustomerToProject(contractorTestProject2.ownerCompany.asOwner, contractorTestProject2.ownerCompany.projectId, contractorTestProject2.contractorCompany.companyId);

        contractorTestProject3 = testHelper.createProject(TestHelper.COMPANY_NAME + "_3",
                                                          TestHelper.CONTRACTOR_COMPANY_NAME + "_3", RoleName.COMPANY_OWNER, false);
        testHelper.assignCustomerToProject(contractorTestProject3.ownerCompany.asOwner, contractorTestProject3.ownerCompany.projectId, contractorTestProject3.contractorCompany.companyId);

        contractorTestProject4 = testHelper.createProject(TestHelper.COMPANY_NAME + "_4",
                                                          TestHelper.CONTRACTOR_COMPANY_NAME + "_4", RoleName.COMPANY_OWNER, false);
        testHelper.assignCustomerToProject(contractorTestProject4.ownerCompany.asOwner, contractorTestProject4.ownerCompany.projectId, contractorTestProject4.contractorCompany.companyId);
    }

    @Test
    void Give_correctRequestAsCustomer_When_createContract_Then_contractCreated() {
        ContractDTO contract = testHelper.createContract(customerTestProject.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(CONTRACT_NAME + "_CUSTOMER", customerTestProject.ownerCompany.projectId));

        assertNotNull(contract);

        assertNotNull(contract.getContractNumber());
        assertNull(contract.getContractorCompanyId());
        assertEquals(customerTestProject.ownerCompany.companyId, contract.getCustomerCompanyId());
    }

    @Test
    void Give_projectWithoutCustomer_When_createContract_Then_throw() {
        TestHelper.TestProject project = testHelper.createProject(TestHelper.COMPANY_NAME + "_NO_CUSTOMER",
                TestHelper.CONTRACTOR_COMPANY_NAME + "_NO_CUSTOMER", RoleName.COMPANY_OWNER, false);
        testHelper.createContract(project.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(CONTRACT_NAME + "_NO_CUSTOMER", project.ownerCompany.projectId), r -> {
                    TestAssert.assertError(HttpStatus.CONFLICT, ContractExceptions.PROJECT_HAS_NO_CUSTOMER_SET.getErrorCode(), r);
                    return null;
                });
    }

    @Test
    void Give_correctRequestAsContractor_When_createContract_Then_contractCreated() {
        ContractDTO contract = testHelper.createContract(
                contractorTestProject1.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(CONTRACT_NAME + "_CONTRACTOR", contractorTestProject1.ownerCompany.projectId));

        assertNotNull(contract);

        assertNotNull(contract.getContractNumber());
        assertNotNull(contract.getContractorCompanyId());
        assertEquals(contractorTestProject1.ownerCompany.companyId, contract.getContractorCompanyId());
        assertEquals(contractorTestProject1.contractorCompany.companyId, contract.getCustomerCompanyId());
    }

    @Test
    void Give_nonUniqueName_When_createContract_Then_fail() {
        testHelper.createContract(customerTestProject.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(CONTRACT_NAME + "_DUPLICATE", customerTestProject.ownerCompany.projectId));
        testHelper.createContract(customerTestProject.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(CONTRACT_NAME + "_DUPLICATE", customerTestProject.ownerCompany.projectId),
                v -> {
                    LocalizedReason reason = v.statusCode(HttpStatus.BAD_REQUEST.value()).extract().as(LocalizedReason.class);

                    assertEquals(ContractExceptions.NON_UNIQ_CONTRACT_NAME.getErrorCode(), reason.getErrorCode());

                    return null;
                });
    }

    @Test
    void Give_correctRequestAsCustomerAndContractMutable_When_updateContract_Then_contractUpdated() {
        ContractDTO originalContract = testHelper.createContract(customerTestProject.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(CONTRACT_NAME + "_UPDATED", customerTestProject.ownerCompany.projectId));

        ContractDTO updatedContract = testHelper.updateContract(customerTestProject.ownerCompany.asOwner, originalContract.getId(), TestBuilder.testUpdateContractRequest(originalContract.getName(), CONTRACT_DESCRIPTION, LocalDateTime.now()));

        assertNotNull(updatedContract);

        assertEquals(originalContract.getName(), updatedContract.getName());
        assertEquals(CONTRACT_DESCRIPTION, updatedContract.getDescription());
    }

    @Test
    void Given_correctPrimaryContract_When_createSubContract_Then_correctSubContractCreated() {
        ContractDTO primaryContract = testHelper.createContract(
                contractorTestProject1.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(CONTRACT_NAME + "_PRIMARY_1", contractorTestProject1.ownerCompany.projectId));
        var contractTasks = testHelper.addContractTasks(contractorTestProject1.ownerCompany.asOwner, TestBuilder.testCreateContractAddTasksRequest(primaryContract.getId(), List.of(
                contractorTestProject1.mainTaskId)));

        testHelper.updateContractTask(contractorTestProject1.ownerCompany.asOwner, new ContractUpdateTaskRequest(
                contractTasks.get(0).getContractTaskId(),
                WHEELBARROW,
                BigDecimal.ONE,
                BigDecimal.ONE
        ));

        testHelper.selfAcceptOffer(contractorTestProject1.ownerCompany.asOwner, primaryContract.getId(), TestBuilder.testSendMessageRequest(
                contractorTestProject1.contractorCompany.companyId, "Hello"));

        ContractDTO subContract = testHelper.createContract(contractorTestProject1.ownerCompany.asOwner, TestBuilder.testCreateContractRequest(primaryContract.getId(), CONTRACT_NAME + "_SUB_1", contractorTestProject1.ownerCompany.projectId));

        assertNotNull(subContract);
        assertEquals(primaryContract.getContractorCompanyId(), subContract.getContractorCompanyId());
        assertEquals(primaryContract.getCustomerCompanyId(), subContract.getCustomerCompanyId());
    }

    @Test
    void Given_primaryContractFromDifferentProject_When_createSubContract_Then_fail() {
        var otherProject = testHelper.createProject(contractorTestProject2.ownerCompany.asOwner, TestBuilder.testNewProjectDTO(
                "otherProject"
        ));
        var primaryContract = testHelper.createContract(
                contractorTestProject2.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(CONTRACT_NAME + "_PRIMARY_2", contractorTestProject2.ownerCompany.projectId));
        var subContract = testHelper.createContract(
                contractorTestProject2.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(primaryContract.getId(), CONTRACT_NAME + "_SUB_2", otherProject.getId()),
                v -> { v.assertThat().statusCode(HttpStatus.BAD_REQUEST.value()); return  null; }
        );
    }

    @Test
    void Given_primaryContractInWrongState_When_createSubContract_Then_fail() {
        ContractDTO primaryContract = testHelper.createContract(
                contractorTestProject3.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(CONTRACT_NAME + "_PRIMARY_3", contractorTestProject3.ownerCompany.projectId));
        ContractDTO subContract = testHelper.createContract(
                contractorTestProject3.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(primaryContract.getId(), CONTRACT_NAME + "_SUB_3", contractorTestProject3.ownerCompany.projectId),
                v -> { v.assertThat().statusCode(HttpStatus.CONFLICT.value()); return  null; }
        );
    }

    @Test
    void Given_primaryContractHasNegateableTasks_When_negateTask_Then_success() {
        ContractDTO primaryContract = testHelper.createContract(
                contractorTestProject4.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(CONTRACT_NAME + "_PRIMARY_4", contractorTestProject4.ownerCompany.projectId));
        var contractTasks = testHelper.addContractTasks(contractorTestProject4.ownerCompany.asOwner, TestBuilder.testCreateContractAddTasksRequest(primaryContract.getId(), List.of(
                contractorTestProject4.mainTaskId)));

        var contractTask = testHelper.updateContractTask(contractorTestProject4.ownerCompany.asOwner, new ContractUpdateTaskRequest(
                contractTasks.get(0).getContractTaskId(),
                WHEELBARROW,
                BigDecimal.ONE,
                BigDecimal.ONE
        ));

        testHelper.selfAcceptOffer(contractorTestProject4.ownerCompany.asOwner, primaryContract.getId(), TestBuilder.testSendMessageRequest(
                contractorTestProject4.contractorCompany.companyId, "Hello"));

        ContractDTO subContract = testHelper.createContract(contractorTestProject4.ownerCompany.asOwner, TestBuilder.testCreateContractRequest(primaryContract.getId(), CONTRACT_NAME + "_SUB_4", contractorTestProject4.ownerCompany.projectId));

        var negateContractTask = testHelper.negateContractTask(contractorTestProject4.ownerCompany.asOwner, subContract.getId(), contractTask.getContractTaskId());

        assertNotNull(negateContractTask);
        assertEquals(contractTask.getTask().getId(), negateContractTask.getTask().getId());
        assertEquals(contractTask.getPrice().negate().intValue(), negateContractTask.getPrice().intValue());
    }
}
