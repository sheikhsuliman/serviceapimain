package com.siryus.swisscon.api.tasks.tasks;

import com.siryus.swisscon.api.auth.AuthException;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.FailFastExtension;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.tasks.dto.AssignCompanyToTaskRequest;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.tasks.dto.SubTaskDTO;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

import static com.siryus.swisscon.api.base.TestHelper.CONTRACTOR_COMPANY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(FailFastExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskTeamIT extends AbstractMvcTestBase {
    private static final String MAIN_TASK = "Main Task";
    private static final String NO_COMPANY_TASK = "NO_COMPANY_TASK";
    private static final String SUB_TASK_1 = "Sub Task 1";
    private static final String TOP_LOCATION = "TOP";
    private static final String ANOTHER_MAIN_TASK = "Another Main Task";

    private TestHelper.ExtendedTestProject testProject;
    private static Integer invitedUserId;
    private static Integer invitedUser2Id;

    @BeforeAll
    void doBeforeAll() {
        testProject = testHelper.createExtendedProject();


        testHelper.addLocationToExtendedProject(testProject, TOP_LOCATION);

        MainTaskDTO mainTaskDTO = testHelper.addContractualTask(testProject.ownerCompany.asOwner, TestBuilder.testCreateMainTaskRequest(testProject.locationId("TOP"), MAIN_TASK, testProject.contractorCompany.companyId));
        testProject.addMainTask(MAIN_TASK, mainTaskDTO.getId());

        MainTaskDTO anotherMainTask = testHelper.addContractualTask(testProject.ownerCompany.asOwner, TestBuilder.testCreateMainTaskRequest(testProject.locationId("TOP"), ANOTHER_MAIN_TASK, testProject.contractorCompany.companyId));
        testProject.addMainTask(ANOTHER_MAIN_TASK, anotherMainTask.getId());

        SubTaskDTO subTaskDTO = testHelper.addSubTask(testProject.contractorCompany.asOwner, TestBuilder.testCreateSubTaskRequest(testProject.mainTaskId(MAIN_TASK), SUB_TASK_1));
        testProject.addSubTask(SUB_TASK_1, subTaskDTO.getId());

        MainTaskDTO noCompanyTaskDTO = testHelper.addDirectorialTask(testProject.ownerCompany.asOwner, TestBuilder.testCreateDirectorialTaskRequest(testProject.locationId("TOP"), NO_COMPANY_TASK, null));
        testProject.addMainTask(NO_COMPANY_TASK, noCompanyTaskDTO.getId());
    }

    @Test
    @Order(1)
    void Given_tasksWithoutUsers_When_addUserToMainTask_Then_Success() {
        testHelper.addUserToMainTask(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK), testProject.contractorCompany.workerId);

        List<TeamUserDTO> mainTaskTeam = testHelper.getMainTaskTeam(testProject.ownerCompany.asOwner, testProject.mainTaskIDs.get(MAIN_TASK));

        TestAssert.assertTeamContainsUser(mainTaskTeam, testProject.contractorCompany.workerId);
    }

    @Test
    @Order(2)
    void Given_tasksWithoutUsers_When_addUserToSubTask_Then_Success() {
        testHelper.addUserToSubTask(testProject.contractorCompany.asOwner, testProject.subTaskId(SUB_TASK_1), testProject.contractorCompany.workerId);

        List<TeamUserDTO> subTaskTeam = testHelper.getSubTaskTeam(testProject.contractorCompany.asOwner, testProject.subTaskIDs.get(SUB_TASK_1));

        TestAssert.assertTeamContainsUser(subTaskTeam, testProject.contractorCompany.workerId);
    }

    @Test
    @Order(3)
    void Given_userNotPartOfTaskTeam_When_getAvailableUsers_Then_thisUserIsListed() {
        List<TeamUserDTO> availableUsers = testHelper.listMainTaskAvailableUsers(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK));

        assertNotNull(availableUsers);
        assertEquals(1, availableUsers.size());
        TestAssert.assertTeamContainsUser(availableUsers, testProject.contractorCompany.anotherWorkerId);
    }


    @Test
    @Order(4)
    void Given_noUsersAssigned_When_getAvailableUsers_Then_returnAllUsersAddedToProject() {
        List<TeamUserDTO> availableUsers = testHelper.listMainTaskAvailableUsers(testProject.contractorCompany.asOwner, testProject.mainTaskId(ANOTHER_MAIN_TASK));

        TestAssert.assertAvailableUsersContainUser(availableUsers, testProject.contractorCompany.workerId, true);
        TestAssert.assertAvailableUsersContainUser(availableUsers, testProject.contractorCompany.anotherWorkerId, true);
    }

    @Test
    @Order(5)
    void Given_userNotPartOfSubTask_When_getAvailableUsers_Then_thisUserIsListed() {
        List<TeamUserDTO> availableUsers = testHelper.listSubTaskAvailableUsers(testProject.contractorCompany.asOwner, testProject.subTaskId(SUB_TASK_1));

        TestAssert.assertAvailableUsersContainUser(availableUsers, testProject.contractorCompany.anotherWorkerId, true);
    }

    @Test
    @Order(6)
    void Given_userNotPartOfProject_When_getMainTaskAvailableUsers_Then_userIsPartOfList() {
        invitedUserId = testHelper.inviteUserAndResetPassword(
                testProject.contractorCompany.asOwner,
                TestBuilder.testTeamUserAddDTO(CONTRACTOR_COMPANY_NAME, "INVITED_FIRST", "INVITED_LAST")
        ).getId();

        List<TeamUserDTO> availableUsers = testHelper.listMainTaskAvailableUsers(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK));
        TestAssert.assertAvailableUsersContainUser(availableUsers, testProject.contractorCompany.anotherWorkerId, true);
        TestAssert.assertAvailableUsersContainUser(availableUsers, invitedUserId, false);
    }

    @Test
    @Order(7)
    void Given_userNotPartOfProject_When_getSubTaskAvailableUsers_Then_userIsPartOfList() {
        List<TeamUserDTO> availableUsers = testHelper.listSubTaskAvailableUsers(testProject.contractorCompany.asOwner, testProject.subTaskId(SUB_TASK_1));
        TestAssert.assertAvailableUsersContainUser(availableUsers, testProject.contractorCompany.anotherWorkerId, true);
        TestAssert.assertAvailableUsersContainUser(availableUsers, invitedUserId, false);
    }

    @Test
    @Order(8)
    void Given_userNotPartOfProject_When_addUserToMainTaskAsWorker_Then_throw() {
        testHelper.addUserToMainTask(testProject.contractorCompany.asWorker, testProject.mainTaskId(MAIN_TASK), invitedUserId,
                r -> TestAssert.assertError(HttpStatus.FORBIDDEN, AuthException.NOT_AUTHORIZED.getErrorCode(), r));
    }

    @Test
    @Order(9)
    void Given_userNotPartOfProject_When_addUserToMainTask_Then_userIsAddedToTaskAndProject() {
        testHelper.addUserToMainTask(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK), invitedUserId);
        TeamUserDTO[] projectTeam = testHelper.getProjectTeam(testProject.contractorCompany.asOwner, testProject.projectId, testProject.contractorCompany.companyId);
        TestAssert.assertTeamContainsUser(Arrays.asList(projectTeam), invitedUserId);

        List<TeamUserDTO> taskTeam = testHelper.getMainTaskTeam(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK));
        TestAssert.assertTeamContainsUser(taskTeam, invitedUserId);
    }

    @Test
    @Order(10)
    void Given_invitedUserIsNowPartOfProjectAndMainTask_When_getAvailableUsersForMainTask_Then_invitedUserIsNotPartOfList() {
        List<TeamUserDTO> availableUsers = testHelper.listMainTaskAvailableUsers(testProject.contractorCompany.asOwner, testProject.mainTaskId(MAIN_TASK));
        assertFalse(availableUsers.stream().anyMatch(dto -> dto.getId().equals(invitedUserId)));
    }

    @Test
    @Order(11)
    void Given_invitedUserIsNowPartOfProject_When_getAvailableUsersForSubTasks_Then_invitedUserIsPartOfList() {
        List<TeamUserDTO> availableUsers = testHelper.listSubTaskAvailableUsers(testProject.contractorCompany.asOwner, testProject.subTaskId(SUB_TASK_1));
        TestAssert.assertAvailableUsersContainUser(availableUsers, invitedUserId, true);
    }

    @Test
    @Order(12)
    void Given_userNotPartOfProject_When_addUserToSubTask_Then_userIsPartOfSubTaskTeamAndProject() {
        invitedUser2Id = testHelper.inviteUserAndResetPassword(
                testProject.contractorCompany.asOwner,
                TestBuilder.testTeamUserAddDTO(CONTRACTOR_COMPANY_NAME, "INVITED_FIRST_2", "INVITED_LAST_2")
        ).getId();

        testHelper.addUserToSubTask(testProject.contractorCompany.asOwner, testProject.subTaskId(SUB_TASK_1), invitedUser2Id);
        TeamUserDTO[] projectTeam = testHelper.getProjectTeam(testProject.contractorCompany.asOwner, testProject.projectId, testProject.contractorCompany.companyId);
        TestAssert.assertTeamContainsUser(Arrays.asList(projectTeam), invitedUserId);
        TestAssert.assertTeamContainsUser(Arrays.asList(projectTeam), invitedUser2Id);

        List<TeamUserDTO> taskTeam = testHelper.getSubTaskTeam(testProject.contractorCompany.asOwner, testProject.subTaskId(SUB_TASK_1));
        TestAssert.assertTeamContainsUser(taskTeam, invitedUser2Id);
    }

    @Test
    @Order(13)
    void Given_unassignedTask_When_assignCompany_Then_success() {
        var taskDTO = testHelper.assignCompanyToMainTask(
                testProject.ownerCompany.asOwner,
                testProject.mainTaskId(NO_COMPANY_TASK),
                AssignCompanyToTaskRequest.builder().companyId(testProject.contractorCompany.companyId).build()
        );

        assertNotNull(taskDTO);
        assertEquals(taskDTO.getStatus(), TaskStatus.ASSIGNED);
    }

    @Test
    @Order(14)
    void Given_assignedTask_When_unAssignCompany_Then_success() {
        var taskDTO = testHelper.unAssignCompanyFromMainTask(
                testProject.ownerCompany.asOwner,
                testProject.mainTaskId(NO_COMPANY_TASK)
        );

        assertNotNull(taskDTO);
        assertEquals(taskDTO.getStatus(), TaskStatus.OPEN);
    }
}
