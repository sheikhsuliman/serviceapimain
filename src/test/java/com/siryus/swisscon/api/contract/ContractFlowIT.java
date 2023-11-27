package com.siryus.swisscon.api.contract;

import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.signup.SignupResponseDTO;
import com.siryus.swisscon.api.auth.sms.ExtendedTokenService;
import com.siryus.swisscon.api.auth.sms.SmsService;
import com.siryus.swisscon.api.auth.usertoken.UserTokenEntity;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.FailFastExtension;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.contract.dto.ContractDTO;
import com.siryus.swisscon.api.contract.dto.ContractEventLogDTO;
import com.siryus.swisscon.api.contract.dto.ContractState;
import com.siryus.swisscon.api.contract.dto.ContractTaskDTO;
import com.siryus.swisscon.api.project.project.ProjectBoardDTO;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.siryus.swisscon.api.base.TestHelper.CUSTOMER_COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.CUSTOMER_COUNTRY_CODE;
import static com.siryus.swisscon.api.base.TestHelper.CUSTOMER_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.CUSTOMER_MOBILE;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_COUNTRY_CODE;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_PHONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;


@ExtendWith(FailFastExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ContractFlowIT extends AbstractMvcTestBase {

    @SpyBean
    private SmsService smsService;

    @SpyBean
    private ExtendedTokenService extendedTokenService;

    private final RoleRepository roleRepository;

    private static final String CONTRACT_NAME = "contract";
    private static final String TASK_NAME_1 = "task 1";
    private static final String INVITATION_TOKEN = "J4U8IR7";

    private Integer contractId;
    private Integer task1Id;
    private Integer secondCompanyId;
    private Integer secondCompanyOwnerId;
    private TestHelper.ExtendedTestProject testProject;

    private RequestSpecification asSecondCompany;

    private final ArgumentCaptor<String> smsCaptor = ArgumentCaptor.forClass(String.class);

    @Autowired
    public ContractFlowIT(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Test
    public void Given_contract_When_executeAcceptOfferFlow_then_Success() {
        initCompleteProjectAndContract(true);
        testContractIsReassignable();
        testSendOffer(testProject.ownerCompany.asOwner, secondCompanyId);
        testContractIsNotReassignable();
        testAcceptOffer(secondCompanyOwnerId);
        testStartTask(testProject.ownerCompany.asAdmin);
        testAllTasksCompleted();
    }

    @Test
    public void Given_contract_When_executeSelfAcceptFlow_then_Success() {
        initCompleteProjectAndContract(true);
        testSelfAcceptOffer();
        testStartTask(testProject.ownerCompany.asAdmin);
        testAllTasksCompleted();
    }

    @Test
    public void Given_contract_When_executeDeclineOfferFlow_then_Success() {
        initCompleteProjectAndContract(true);
        testSendOffer(testProject.ownerCompany.asOwner, secondCompanyId);
        testDeclineOffer(secondCompanyOwnerId);
    }

    @Test
    public void Given_contract_When_executeAcceptInvitationFlow_then_Success() {
        initCompleteProjectAndContract(false);
        testSendInvitation();
        testAcceptInvitation();
        testSendOffer(asSecondCompany, testProject.ownerCompany.companyId);
        testAcceptOffer(testProject.ownerCompany.ownerId);
    }

    @Test
    public void Given_contract_When_executeDeclineInvitationFlow_then_Success() {
        initCompleteProjectAndContract(false);
        testSendInvitation();
        testDeclineInvitation(testProject.ownerCompany.ownerId);
    }

    private void initCompleteProjectAndContract(boolean isContractorProject) {
        cleanDatabase();

        testProject = testHelper.createExtendedProject();

        asSecondCompany = initSecondCompany();

        initProjectAndContract(isContractorProject);

        changeMobile(asSecondCompany, CUSTOMER_COUNTRY_CODE, CUSTOMER_MOBILE);
        changeMobile(testProject.ownerCompany.asOwner, PROJECT_OWNER_COUNTRY_CODE, PROJECT_OWNER_PHONE);
    }


    private void changeMobile(RequestSpecification asCustomer, Integer customerCountryCode, String customerMobile) {
        captureSMS();
        testHelper.sendChangeMobileSms(asCustomer, customerCountryCode, customerMobile);
        String token = extractChangePhoneTokenOutOfText(smsCaptor.getValue());
        testHelper.updateMobile(asCustomer, token);
    }

    private void initProjectAndContract(boolean isContractorProject) {
        Integer offerReceiverId;
        Integer invitationReceiverId;

        if(isContractorProject) {
            testHelper.addCompanyToProject(testProject.ownerCompany.asOwner, testProject.projectId, secondCompanyId,
                    roleRepository.getRoleByName(RoleName.PROJECT_MANAGER.toString()).getId());
            testHelper.assignCustomerToProject(testProject.ownerCompany.asOwner, testProject.projectId, secondCompanyId);
            offerReceiverId = secondCompanyOwnerId;
            invitationReceiverId = testProject.ownerCompany.ownerId;
        } else {
            testHelper.assignCustomerToProject(testProject.ownerCompany.asOwner, testProject.projectId, testProject.ownerCompany.companyId);
            offerReceiverId = testProject.ownerCompany.ownerId;
            invitationReceiverId = secondCompanyId;
        }

        task1Id = addTaskToProject(TASK_NAME_1).getId();
        contractId = testHelper.createContract(testProject.ownerCompany.asOwner,
                TestBuilder.testCreateContractRequest(CONTRACT_NAME, testProject.ownerCompany.projectId)).getId();
        List<ContractTaskDTO> contractTaskDTOS = testHelper.addContractTasks(testProject.ownerCompany.asOwner,
                TestBuilder.testCreateContractAddTasksRequest(contractId, Collections.singletonList(task1Id)));
        Integer contractTaskId = contractTaskDTOS.get(0).getContractTaskId();
        testHelper.updateContractTask(testProject.ownerCompany.asOwner,
                testBuilder.testCreateContractUpdateTaskRequest(contractTaskId, "m2", "2.54", "14.50"));

        Mockito.doReturn(UserTokenEntity.builder()
                .externalId(String.valueOf(contractId))
                .userId(invitationReceiverId)
                .build())
                .when(extendedTokenService)
                .verifyToken(eq(INVITATION_TOKEN), Mockito.any(), Mockito.anyBoolean());
    }

    private RequestSpecification initSecondCompany() {
        SignupResponseDTO signupResponseDTO = testHelper.inviteCompanyAndSignup(testProject.ownerCompany.asOwner,
                testBuilder.testCompanyInviteDTO(CUSTOMER_COMPANY_NAME, CUSTOMER_EMAIL,
                        RoleName.CUSTOMER));
        secondCompanyId = signupResponseDTO.getCompanyId();
        secondCompanyOwnerId = signupResponseDTO.getUserId();
        return testHelper.login(CUSTOMER_EMAIL);
    }

    private void testContractIsReassignable() {
        ProjectBoardDTO projectBoard = testHelper.getProjectBoard(testProject.ownerCompany.asOwner, testProject.projectId);
        assertTrue(projectBoard.getProjectCustomerIsReassignable());
    }

    private void testSendInvitation() {
        ContractEventLogDTO contractEventLogDTO = testHelper.contractSendInvitation(testProject.ownerCompany.asOwner, contractId,
                TestBuilder.testSendMessageRequest(secondCompanyId, "Sample Message"));

        TestAssert.assertContractEventLogDTO(contractId,  ContractState.CONTRACT_INVITATION_SENT,  ContractState.CONTRACT_DRAFT,
                ContractState.CONTRACT_INVITATION_SENT,  testProject.projectId,  contractEventLogDTO);

        ContractDTO contract = testHelper.getContract(testProject.ownerCompany.asOwner, contractId);
        assertEquals(ContractState.CONTRACT_INVITATION_SENT, contract.getContractState());
    }

    private void testAcceptInvitation() {
        String invitationAcceptedHtml = testHelper.quickLinkAcceptContractInvitation(contractId, testProject.ownerCompany.ownerId);

        TestAssert.templateContainsCaseInsensitive(invitationAcceptedHtml, "invitation", "successfully", "accepted");
        assertEquals(ContractState.CONTRACT_INVITATION_ACCEPTED,
                testHelper.getContract(testProject.ownerCompany.asOwner, contractId).getContractState());
    }

    private void testDeclineInvitation(Integer userId) {
        String invitationDeclined = testHelper.quickLinkDeclineContractInvitation(contractId, userId);

        TestAssert.templateContainsCaseInsensitive(invitationDeclined, "invitation", "declined");
        assertEquals(ContractState.CONTRACT_INVITATION_DECLINED,
                testHelper.getContract(testProject.ownerCompany.asOwner, contractId).getContractState());
    }

    private void testSendOffer(RequestSpecification asSender, Integer receiverCompanyId) {
        ContractEventLogDTO contractEventLogDTO = testHelper.contractSendOffer(asSender, contractId,
                TestBuilder.testSendMessageRequest(receiverCompanyId, "send offer"));

        TestAssert.assertContractEventLogDTO(contractId,  ContractState.CONTRACT_OFFER_MADE,  ContractState.CONTRACT_DRAFT,
                ContractState.CONTRACT_OFFER_MADE,  testProject.projectId,  contractEventLogDTO);

        ContractDTO contract = testHelper.getContract(testProject.ownerCompany.asOwner, contractId);
        assertEquals(ContractState.CONTRACT_OFFER_MADE, contract.getContractState());
    }

    private void testContractIsNotReassignable() {
        ProjectBoardDTO projectBoard = testHelper.getProjectBoard(testProject.ownerCompany.asOwner, testProject.projectId);
        assertFalse(projectBoard.getProjectCustomerIsReassignable());
    }

    private void testAcceptOffer(Integer userId) {
        String offerAcceptedHtml = testHelper.quickLinkAcceptContractOffer(contractId, userId);
        TestAssert.templateContainsCaseInsensitive(offerAcceptedHtml, "successfully", "accepted");
        assertEquals(ContractState.CONTRACT_ACCEPTED,
                testHelper.getContract(testProject.ownerCompany.asOwner, contractId).getContractState());

    }

    private void testSelfAcceptOffer() {
        testHelper.selfAcceptOffer(testProject.ownerCompany.asOwner, contractId,
                TestBuilder.testSendMessageRequest(secondCompanyId, "self accept"));

    }

    private void testDeclineOffer(Integer userId) {
        String offerDeclinedHtml = testHelper.quickLinkDeclineContractOffer(contractId, userId);
        TestAssert.templateContainsCaseInsensitive(offerDeclinedHtml, "was", "declined");
        assertEquals(ContractState.CONTRACT_DECLINED,
                testHelper.getContract(testProject.ownerCompany.asOwner, contractId).getContractState());

    }


    private void testStartTask(RequestSpecification asContractor) {
        testHelper.startTimer(asContractor,
                TestBuilder.testTaskWorklogRequest(task1Id, null, "start"));
        assertEquals(ContractState.CONTRACT_IN_PROGRESS,
                testHelper.getContract(testProject.ownerCompany.asOwner, contractId).getContractState());

    }

    private void testAllTasksCompleted() {
        testHelper.completeTask(testProject.ownerCompany.asOwner,
                TestBuilder.testTaskWorklogRequest(task1Id, null, "complete"));
        testHelper.approveTask(testProject.ownerCompany.asOwner,
                TestBuilder.testTaskWorklogRequest(task1Id, null, "approved"));
        testHelper.approveTask(testProject.ownerCompany.asOwner,
                TestBuilder.testTaskWorklogRequest(task1Id, null, "approved 2"));
        assertEquals(ContractState.CONTRACT_COMPLETED,
                testHelper.getContract(testProject.ownerCompany.asOwner, contractId).getContractState());

    }

    private MainTaskDTO addTaskToProject(String taskName) {
        MainTaskDTO mainTaskDTO = testHelper.addContractualTask(testProject.ownerCompany.asAdmin,
                TestBuilder.testCreateMainTaskRequest(testProject.ownerCompany.topLocationId,
                        taskName,
                        testProject.ownerCompany.companyId));
        testProject.addMainTask(taskName, mainTaskDTO.getId());
        return mainTaskDTO;
    }

    private String extractChangePhoneTokenOutOfText(String text) {
        Matcher n = Pattern.compile("([A-Z0-9]{6})").matcher(text);
        return n.find() ? n.group(1) : StringUtils.EMPTY;
    }

    private void captureSMS() {
        doNothing().when(smsService).sendSmsMessage(smsCaptor.capture(), Mockito.anyString());
    }

}
