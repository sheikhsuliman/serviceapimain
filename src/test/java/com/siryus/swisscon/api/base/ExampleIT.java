package com.siryus.swisscon.api.base;

import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.signup.SignupResponseDTO;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.company.company.CompanyDirectoryDTO;
import com.siryus.swisscon.api.location.location.LocationDetailsDTO;
import com.siryus.swisscon.api.project.project.ProjectBoardDTO;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import com.siryus.swisscon.api.taskworklog.dto.RejectTaskRequest;
import com.siryus.swisscon.api.taskworklog.dto.TaskStatusDTO;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static com.siryus.swisscon.api.base.TestHelper.COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.CONTRACTOR_COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_ADMIN_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_ADMIN_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_ADMIN_LAST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_MANAGER_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_LAST_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ExampleIT extends AbstractMvcTestBase {
    @BeforeEach
    public void doBeforeEach() {
        cleanDatabase();
    }

    @Test
    public void Given_validSignUp_When_signUp_Then_returnValidResponse() {
        SignupResponseDTO ownerAndCompany = testHelper.signUp(
            TestBuilder.testSignupDTO(
                COMPANY_NAME, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME,
                dto ->  dto.toBuilder().company(
                            dto.getCompany().toBuilder().tradeIds(Collections.singletonList(tradeId("ARCHITECT"))).build()
                        ).build()
            )
        );

        assertNotNull(ownerAndCompany);
        assertNotNull(ownerAndCompany.getUserId());
        assertNotNull(ownerAndCompany.getCompanyId());
    }

    @Test
    public void Given_validEmail_When_invite_Then_returnValidResponse() {
        testHelper.signUp(
                TestBuilder.testSignupDTO(
                        COMPANY_NAME, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME
                )
        );

        RequestSpecification asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);

        TeamUserDTO userDTO = testHelper.inviteUserAndResetPassword(
                asCompanyOwner,
                TestBuilder.testTeamUserAddDTO(COMPANY_NAME, PROJECT_ADMIN_FIRST_NAME, PROJECT_ADMIN_LAST_NAME)
        );

        assertNotNull(userDTO);
    }

    @Test
    public void Given_companyOwner_When_createProject_Then_returnValidResponse() {
        testHelper.signUp(
                TestBuilder.testSignupDTO(
                        COMPANY_NAME, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME
                )
        );

        RequestSpecification asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);

        ProjectBoardDTO funProject = testHelper.createProject(asCompanyOwner, TestBuilder.testNewProjectDTO("Fun Project"));

        assertNotNull(funProject);
    }

    @Test
    public void Given_companyWorker_When_createProject_Then_FORBIDDEN() {
        testHelper.signUp(
                TestBuilder.testSignupDTO(
                        COMPANY_NAME, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME
                )
        );

        RequestSpecification asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);

        testHelper.inviteUserAndResetPassword(
                asCompanyOwner,
                TestBuilder.testTeamUserAddDTO(COMPANY_NAME, PROJECT_ADMIN_FIRST_NAME, PROJECT_ADMIN_LAST_NAME)
        );

        RequestSpecification asCompanyWorker = testHelper.login(PROJECT_ADMIN_EMAIL);

        ProjectBoardDTO funProject = testHelper.createProject(
                asCompanyWorker, TestBuilder.testNewProjectDTO("Fun Project"),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return null; }
        );

        assertNull(funProject);
    }

    @Test
    public void Given_companyWorker_When_changeRoleToAdmin_Then_PROJECT_ADMIN() {
        testHelper.signUp(
                TestBuilder.testSignupDTO(
                        COMPANY_NAME, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME
                )
        );

        RequestSpecification asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);

        TeamUserDTO invitedUser = testHelper.inviteUserAndResetPassword(
                asCompanyOwner,
                TestBuilder.testTeamUserAddDTO(COMPANY_NAME, PROJECT_ADMIN_FIRST_NAME, PROJECT_ADMIN_LAST_NAME)
        );

        ProjectBoardDTO funProject = testHelper.createProject(asCompanyOwner, TestBuilder.testNewProjectDTO("Fun Project"));

        TeamUserDTO teamUserDTO = testHelper.addUserToProject(asCompanyOwner, funProject.getId(), invitedUser.getId());

        assertNotNull(teamUserDTO);

        TeamUserDTO adminTeamUserDTO = testHelper.changeProjectAdminRole(asCompanyOwner, funProject.getId(), invitedUser.getId(), roleId(RoleName.PROJECT_ADMIN));

        assertNotNull(adminTeamUserDTO);
        assertEquals(roleId(RoleName.PROJECT_ADMIN), adminTeamUserDTO.getRoleId());
    }

    @Test
    public void Given_projectAdmin_When_createLocation_Then_success() {
        TestHelper.TestProjectOwnerCompany projectOwnerCompany = testHelper.createProjectOwnerCompany();

        LocationDetailsDTO locationDetailsDTO = testHelper.createLocation(projectOwnerCompany.asAdmin, TestBuilder.testLocationCreateDTO(projectOwnerCompany.projectId, "TOP"));

        assertNotNull(locationDetailsDTO);

        LocationDetailsDTO subLocationDetailsDTO = testHelper.createLocation(
            projectOwnerCompany.asAdmin,
            TestBuilder.testLocationCreateDTO(
                projectOwnerCompany.projectId, "Bottom",
                r -> r.toBuilder().parent(locationDetailsDTO.getId()).build()
            )
        );

        assertNotNull(subLocationDetailsDTO);
    }

    @Test
    public void Given_projectAdmin_When_inviteCompanyToProject_Then_success() {
        TestHelper.TestProjectOwnerCompany projectOwnerCompany = testHelper.createProjectOwnerCompany();
        TestHelper.TestProjectContractorCompany projectContractorCompany = testHelper.createProjectContractorCompany();

        CompanyDirectoryDTO directoryDTO = testHelper.inviteCompanyToProject(projectOwnerCompany.asAdmin, projectOwnerCompany.projectId, projectContractorCompany.companyId);

        assertNotNull(directoryDTO);
        assertEquals(CONTRACTOR_COMPANY_NAME, directoryDTO.getName());
        assertEquals(PROJECT_MANAGER_EMAIL, directoryDTO.getOwner().getEmail());
    }

    @Test
    public void Given_projectAdminAndValidRequest_When_addMainTaskToLocation_Then_success() {
        TestHelper.TestProjectOwnerCompany projectOwnerCompany = testHelper.createProjectOwnerCompany();
        TestHelper.TestProjectContractorCompany projectContractorCompany = testHelper.createProjectContractorCompany();

        testHelper.inviteCompanyToProject(projectOwnerCompany.asAdmin, projectOwnerCompany.projectId, projectContractorCompany.companyId);

        LocationDetailsDTO locationDetailsDTO = testHelper.createLocation(projectOwnerCompany.asAdmin, TestBuilder.testLocationCreateDTO(projectOwnerCompany.projectId, "TOP"));

        MainTaskDTO mainTaskDTO = testHelper.addContractualTask(projectOwnerCompany.asAdmin, TestBuilder.testCreateMainTaskRequest(
                locationDetailsDTO.getId(),
                "Main Task",
                projectContractorCompany.companyId
        ));

        assertNotNull(mainTaskDTO);
    }

    @Test
    public void Given_projectManagerAndValidRequest_When_assignWorkerToSubTask_Then_success() {
        TestHelper.TestProject testProject = testHelper.createProject();

        testHelper.addUserToSubTask(
                testProject.contractorCompany.asOwner,
                testProject.subTaskId,
                testProject.contractorCompany.workerId,

                r -> r.statusCode(HttpStatus.CREATED.value())
        );
    }

    @Test
    public void Given_projectWorker_When_startFirstTimer_Then_mainTaskInProgress() {
        TestHelper.TestProject testProject = testHelper.createProject();

        testHelper.addUserToSubTask(
                testProject.contractorCompany.asOwner,
                testProject.subTaskId,
                testProject.contractorCompany.workerId
        );

        TaskStatusDTO taskStatusDTO = testHelper.startTimer(testProject.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject.subTaskId, "Work Is Hard"));

        assertNotNull(taskStatusDTO);
        assertEquals(TaskStatus.IN_PROGRESS, taskStatusDTO.getMainTaskStatus());
    }

    @Test
    public void Given_projectManager_When_completeTask_Then_taskInReview() {
        TestHelper.TestProject testProject = testHelper.createProject();

        testHelper.addUserToSubTask(
                testProject.contractorCompany.asOwner,
                testProject.subTaskId,
                testProject.contractorCompany.workerId
        );

        testHelper.startTimer(testProject.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId, null, "Work Is Hard"));

        TaskStatusDTO statusDTO = testHelper.completeTask(testProject.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId, null, "Work Is Done"));

        assertNotNull(statusDTO);
        assertEquals(TaskStatus.IN_REVIEW, statusDTO.getMainTaskStatus());
    }

    @Test
    public void Given_projectWorker_When_completeTask_Then_taskInContractorReview() {
        TestHelper.TestProject testProject = testHelper.createProject();

        testHelper.addUserToSubTask(
                testProject.contractorCompany.asOwner,
                testProject.subTaskId,
                testProject.contractorCompany.workerId
        );

        testHelper.addUserToMainTask(
                testProject.contractorCompany.asOwner,
                testProject.mainTaskId,
                testProject.contractorCompany.workerId
        );

        testHelper.startTimer(testProject.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(null, testProject.subTaskId, "Work Is Hard"));

        TaskStatusDTO statusDTO = testHelper.completeTask(testProject.contractorCompany.asWorker, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId, null, "Work Is Done"));

        assertNotNull(statusDTO);
        assertEquals(TaskStatus.IN_CONTRACTOR_REVIEW, statusDTO.getMainTaskStatus());
    }

    @Test
    public void Given_projectAdmin_When_approveTask_Then_taskComplete() {
        TestHelper.TestProject testProject = testHelper.createProject();

        testHelper.addUserToSubTask(
                testProject.contractorCompany.asOwner,
                testProject.subTaskId,
                testProject.contractorCompany.workerId
        );

        testHelper.startTimer(testProject.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId, null, "Work Is Hard"));

        testHelper.completeTask(testProject.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId, null, "Work Is Done"));

        TaskStatusDTO statusDTO = testHelper.approveTask(testProject.ownerCompany.asAdmin, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId, null, "Good Job!"));

        assertNotNull(statusDTO);
        assertEquals(TaskStatus.COMPLETED, statusDTO.getMainTaskStatus());

    }

    @Test
    public void Given_projectAdmin_When_rejectTask_Then_taskRejected() {
        TestHelper.TestProject testProject = testHelper.createProject();

        testHelper.addUserToSubTask(
                testProject.contractorCompany.asOwner,
                testProject.subTaskId,
                testProject.contractorCompany.workerId
        );

        testHelper.startTimer(testProject.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId, null, "Work Is Hard"));

        testHelper.completeTask(testProject.contractorCompany.asOwner, TestBuilder.testTaskWorklogRequest(testProject.mainTaskId, null, "Work Is Done"));

        RejectTaskRequest request = RejectTaskRequest.builder()
                .dueDate(ZonedDateTime.now().plusDays(14).truncatedTo(ChronoUnit.SECONDS))
                .worklogRequest(TestBuilder.testTaskWorklogRequest(testProject.mainTaskId, null, "Good Job!"))
                .build();
        TaskStatusDTO statusDTO = testHelper.rejectTask(testProject.ownerCompany.asAdmin, request);

        assertNotNull(statusDTO);
        assertEquals(TaskStatus.REJECTED, statusDTO.getMainTaskStatus());
        assertEquals(
            request.getDueDate().withZoneSameInstant(ZoneId.of(ZoneOffset.UTC.getId())),
            statusDTO.getDueDate().withZoneSameInstant(ZoneId.of(ZoneOffset.UTC.getId()))
        );

    }
}
