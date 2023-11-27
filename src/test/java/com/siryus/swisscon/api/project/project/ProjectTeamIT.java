package com.siryus.swisscon.api.project.project;

import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.user.TeamUserAddDTO;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.FailFastExtension;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.project.ProjectException;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.util.error.TestErrorResponse;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.siryus.swisscon.api.base.TestHelper.COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.CONTRACTOR_COMPANY_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FailFastExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectTeamIT extends AbstractMvcTestBase {

    private final RoleRepository roleRepository;

    private static final String NEW_USER_FIRSTNAME = "firstname";
    private static final String NEW_USER_LASTNAME = "firstname";

    private static TestHelper.ExtendedTestProject testProject;
    private static ProjectBoardDTO testProject2;
    private static RequestSpecification asProjectOwner;
    private static RequestSpecification asProjectManager;
    private static RequestSpecification asProjectWorker;
    private static Integer projectId;

    private static Integer unassignedOwnerCompanyUserId;
    private static Integer unassignedContractorCompanyUserId;

    private static Integer projectWorkerRoleId;

    @Autowired
    public ProjectTeamIT(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }


    @BeforeAll
    public void initTest() {
        testProject = testHelper.createExtendedProject();
        asProjectOwner = testProject.ownerCompany.asOwner;
        asProjectManager = testProject.contractorCompany.asOwner;
        asProjectWorker = testProject.contractorCompany.asWorker;

        testProject2 = testHelper.createProject(asProjectManager, TestBuilder.testNewProjectDTO("CONTRACTOR_PROJECT"));
        projectId = testProject.ownerCompany.projectId;

        TeamUserAddDTO newOwnerCompanyUser = TestBuilder.testTeamUserAddDTO(COMPANY_NAME, NEW_USER_FIRSTNAME, NEW_USER_LASTNAME);
        TeamUserDTO newOwnerCompanyUserDTO = testHelper.inviteUserAndResetPassword(asProjectOwner, newOwnerCompanyUser);
        unassignedOwnerCompanyUserId = newOwnerCompanyUserDTO.getId();

        TeamUserAddDTO newContractorCompanyUser = TestBuilder.testTeamUserAddDTO(CONTRACTOR_COMPANY_NAME, NEW_USER_FIRSTNAME, NEW_USER_LASTNAME);
        TeamUserDTO newContractorCompanyUserDTO = testHelper.inviteUserAndResetPassword(asProjectManager, newContractorCompanyUser);
        unassignedContractorCompanyUserId = newContractorCompanyUserDTO.getId();

        projectWorkerRoleId = roleRepository.getRoleByName(RoleName.PROJECT_WORKER.toString()).getId();
    }

    @Test
    @Order(0)
    public void Given_testProject_When_getAvailableUsersToAdd_Then_NoUsersAreAvailable() {
        List<TeamUserDTO> availableUsersToAddOwnerCompany = testHelper
                .getAvailableUsersToAddToProject(asProjectOwner, projectId);
        List<TeamUserDTO> availableUsersToAddContractorCompany = testHelper
                .getAvailableUsersToAddToProject(asProjectManager, projectId);

        assertTrue(availableUsersToAddOwnerCompany.stream().anyMatch(u->u.getId().equals(unassignedOwnerCompanyUserId)));
        assertTrue(availableUsersToAddContractorCompany.stream().anyMatch(u->u.getId().equals(unassignedContractorCompanyUserId)));
    }

    @Test
    @Order(1)
    public void Given_notExistingProject_When_getAvailableUsersToAdd_Then_Throw() {
        testHelper
                .getAvailableUsersToAddToProject(asProjectOwner, -15, r -> {
                    r.statusCode(HttpStatus.FORBIDDEN.value());
                    return null;
                });
    }

    @Test
    @Order(2)
    public void Given_anotherProject_When_getAvailableUsersToAdd_Then_Throw() {
        testHelper
                .getAvailableUsersToAddToProject(asProjectOwner, testProject2.getId(), r -> {
                    r.statusCode(HttpStatus.FORBIDDEN.value());
                    return null;
                });
    }

    @Test
    @Order(3)
    public void Given_testProjectLoggedInAsWorker_When_getAvailableUsersToAdd_Then_Throw() {
        testHelper
                .getAvailableUsersToAddToProject(asProjectWorker, projectId, r -> {
                    r.statusCode(HttpStatus.FORBIDDEN.value());
                    return null;
                });
    }

    @Test
    @Order(4)
    public void Given_testProject_When_getTeam_Then_ReturnCorrectTeam() {
        TeamUserDTO[] ownerCompanyTeam = testHelper.getProjectTeam(asProjectOwner, projectId, testProject.ownerCompany.companyId);
        TeamUserDTO[] contractorCompanyTeam = testHelper.getProjectTeam(asProjectManager, projectId, testProject.contractorCompany.companyId);

        assertTrue(Arrays.stream(ownerCompanyTeam).anyMatch(u->u.getId().equals(testProject.ownerCompany.ownerId)));
        assertTrue(Arrays.stream(contractorCompanyTeam).anyMatch(u->u.getId().equals(testProject.contractorCompany.ownerId)));
    }

    @Test
    @Order(5)
    public void Given_anotherCompaniesProject_When_getTeam_Then_ReturnThrow() {
        testHelper.getProjectTeam(asProjectOwner, testProject2.getId(), testProject.ownerCompany.companyId,
                r -> {
                    r.statusCode(HttpStatus.FORBIDDEN.value());
                    return null;
                });
    }

    @Test
    @Order(6)
    public void Given_nonExistingProject_When_addUserToProject_Then_Throw() {
        testHelper
                .addUserToProject(asProjectOwner,
                        -15,
                        unassignedOwnerCompanyUserId,
                        r -> {
                            r.statusCode(HttpStatus.FORBIDDEN.value());
                            return null;
                        });
    }

    @Test
    @Order(7)
    public void Given_anotherCompaniesProject_When_addUserToProject_Then_Throw() {
        testHelper
                .addUserToProject(asProjectManager,
                        testProject2.getId(),
                        unassignedOwnerCompanyUserId,
                        r -> {
                            r.statusCode(HttpStatus.FORBIDDEN.value());
                            return null;
                        });
    }

    @Test
    @Order(8)
    public void Given_anotherCompanyUser_When_addUserToProject_Then_Throw() {
        testHelper
                .addUserToProject(asProjectOwner,
                        projectId,
                        unassignedContractorCompanyUserId,
                        r -> {
                            r.statusCode(HttpStatus.FORBIDDEN.value());
                            return null;
                        });
    }

    @Test
    @Order(9)
    public void Given_unassignedUserAndLoggedInAsWorker_When_addUserToProject_Then_Throw() {
        testHelper
                .addUserToProject(asProjectWorker,
                        projectId,
                        unassignedContractorCompanyUserId,
                        r -> {
                            r.statusCode(HttpStatus.FORBIDDEN.value());
                            return null;
                        });
    }

    @Test
    @Order(10)
    public void Given_unassignedUserOfOwnerCompany_When_addUserToProject_Then_Success() {
        TeamUserDTO teamUserDTO = testHelper
                .addUserToProject(asProjectOwner,
                        projectId,
                        unassignedOwnerCompanyUserId);
        assertEquals(unassignedOwnerCompanyUserId, teamUserDTO.getId());
        assertTrue(teamUserDTO.getRoleIds().contains(projectWorkerRoleId));
        testAssert.assertNotificationFired(testProject.ownerCompany.companyId,
                projectId,
                testProject.ownerCompany.ownerId,
                unassignedOwnerCompanyUserId,
                NotificationType.PROJECT_USER_INVITED,
                projectId);
    }

    @Test
    @Order(11)
    public void Given_unassignedUserAssignedNow_When_getAvailableUsers_Then_ReturnEmptyList() {
        List<TeamUserDTO> availableUsersToAddOwnerCompany = testHelper
                .getAvailableUsersToAddToProject(asProjectOwner, projectId);

        assertTrue(availableUsersToAddOwnerCompany.isEmpty());
    }

    @Test
    @Order(12)
    public void Given_testProjectWithNewUser_When_getTeam_Then_ReturnCorrectTeam() {
        TeamUserDTO[] ownerCompanyTeam = testHelper.getProjectTeam(asProjectOwner, projectId, testProject.ownerCompany.companyId);
        assertTrue(Arrays.stream(ownerCompanyTeam).anyMatch(u->u.getId().equals(unassignedOwnerCompanyUserId)));
    }

    @Test
    @Order(13)
    public void Given_userOfAnotherCopany_When_removeUserFromProject_ThenThrow() {
        testHelper.removeUserFromProject(asProjectOwner, projectId, testProject.contractorCompany.workerId,
                r -> r.statusCode(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @Order(14)
    public void Given_projectOfAnotherCompany_When_removeUserProject_Then_Throw() {
        testHelper.removeUserFromProject(asProjectOwner, testProject2.getId(), unassignedOwnerCompanyUserId,
                r -> r.statusCode(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @Order(15)
    public void Given_assignedUserAndLoggedInAsWorker_When_removeUserFromProject_Then_Throw() {
        testHelper.removeUserFromProject(asProjectWorker, projectId, unassignedOwnerCompanyUserId,
                r -> r.statusCode(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @Order(16)
    public void Given_projectOwner_When_removeUserProject_Then_Throw() {
        testHelper.removeUserFromProject(testProject.ownerCompany.asAdmin, projectId, testProject.ownerCompany.ownerId,
                r -> {
                    TestErrorResponse error = r.statusCode(HttpStatus.CONFLICT.value())
                            .extract().as(TestErrorResponse.class);
                    assertEquals(error.getErrorCode(), ProjectException.USER_WITH_ROLE_CAN_NOT_BE_REMOVED.getErrorCode());
                    assertEquals(error.getParameters().get("roleName"), RoleName.PROJECT_OWNER.name());
                });
    }

    @Test
    @Order(17)
    public void Given_companyOwner_When_removeUserProject_Then_Throw() {
        testHelper.removeUserFromProject(testProject.contractorCompany.asOwner, projectId, testProject.contractorCompany.ownerId,
                r -> {
                    TestErrorResponse error = r.statusCode(HttpStatus.CONFLICT.value())
                            .extract().as(TestErrorResponse.class);
                    assertEquals(error.getErrorCode(), ProjectException.USER_WITH_ROLE_CAN_NOT_BE_REMOVED.getErrorCode());
                    assertEquals(error.getParameters().get("roleName"), RoleName.PROJECT_MANAGER.name());
                });
    }

    @Test
    @Order(18)
    public void Given_userWithTasksAssigned_When_removeUserProject_Then_Throw() {
        Integer invitedUserId = testHelper.inviteUserAndResetPassword(
                testProject.ownerCompany.asOwner,
                TestBuilder.testTeamUserAddDTO(COMPANY_NAME, "INVITED_FIRST_1", "INVITED_LAST_1")
        ).getId();

        // assign test project another worker to a main task
        testHelper.addLocationToExtendedProject(testProject, "random");
        MainTaskDTO mainTaskDTO = testHelper.addContractualTask(testProject.ownerCompany.asAdmin, TestBuilder.testCreateMainTaskRequest(
                testProject.locationId("random"),
                "task",
                testProject.ownerCompany.companyId,
                r -> r.toBuilder()
                        .taskTeam(Collections.singletonList(invitedUserId))
                        .build()
        ));
        testProject.addMainTask("task", mainTaskDTO.getId());

        testHelper.removeUserFromProject(testProject.ownerCompany.asOwner, projectId, invitedUserId,
                r -> {
                    TestErrorResponse error = r.statusCode(HttpStatus.CONFLICT.value())
                            .extract().as(TestErrorResponse.class);
                    assertThat(error.getReason(), containsString("user is assigned to tasks, he cannot be removed"));
                });
    }




    @Test
    @Order(19)
    public void Given_testProject_When_removeUserFromProject_Then_Success() {
        testHelper.removeUserFromProject(asProjectOwner, projectId, unassignedOwnerCompanyUserId);

        // user is in available list now
        List<TeamUserDTO> availableUsersToAddOwnerCompany = testHelper
                .getAvailableUsersToAddToProject(asProjectOwner, projectId);

        assertTrue(availableUsersToAddOwnerCompany.stream().anyMatch(u->u.getId().equals(unassignedOwnerCompanyUserId)));
        testAssert.assertNotificationFired(testProject.ownerCompany.companyId,
                projectId,
                testProject.ownerCompany.ownerId,
                unassignedOwnerCompanyUserId,
                NotificationType.PROJECT_USER_REMOVED,
                projectId);
    }

    @Test
    @Order(20)
    public void Given_removedUserFromProject_When_getTeam_Then_RemovedUserIsNotThereAnymore() {
        TeamUserDTO[] ownerCompanyTeam = testHelper.getProjectTeam(asProjectOwner, projectId, testProject.ownerCompany.companyId);
        assertTrue(Arrays.stream(ownerCompanyTeam).noneMatch(u->u.getId().equals(unassignedOwnerCompanyUserId)));
    }

}
