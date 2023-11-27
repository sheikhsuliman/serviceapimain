package com.siryus.swisscon.api.project.project;

import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.project.ProjectException;
import com.siryus.swisscon.api.util.error.TestErrorResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

import static com.siryus.swisscon.api.base.TestHelper.COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_EMAIL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectUserChangeRoleIT extends AbstractMvcTestBase {

    private final RoleRepository roleRepository;

    private static TestHelper.TestProject testProject;
    private static RequestSpecification asProjectOwner;

    private static Integer projectOwnerRoleId;
    private static Integer projectAdminRoleId;
    private static Integer projectManagerRoleId;
    private static Integer projectWorkerRoleId;

    @Autowired
    public ProjectUserChangeRoleIT(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @BeforeAll
    public void initTest() {
        testProject = testHelper.createProject();
        asProjectOwner = testHelper.login(PROJECT_OWNER_EMAIL);

        projectOwnerRoleId = roleRepository.getRoleByName(RoleName.PROJECT_OWNER.toString()).getId();
        projectAdminRoleId = roleRepository.getRoleByName(RoleName.PROJECT_ADMIN.toString()).getId();
        projectManagerRoleId = roleRepository.getRoleByName(RoleName.PROJECT_MANAGER.toString()).getId();
        projectWorkerRoleId = roleRepository.getRoleByName(RoleName.PROJECT_WORKER.toString()).getId();
    }

    @Test
    public void Given_Worker_When_changeAdminRoleToAdmin_Then_success() {
        Integer userId = createUserWithRole(projectWorkerRoleId);

        testHelper.changeProjectAdminRole(asProjectOwner,
                testProject.ownerCompany.projectId,
                userId,
                projectAdminRoleId);

        assertRoleChange(projectAdminRoleId, userId);
    }

    @Test
    public void Given_Admin_When_changeAdminRoleToWorker_Then_success() {
        Integer userId = createUserWithRole(projectAdminRoleId);

        testHelper.changeProjectAdminRole(asProjectOwner,
                testProject.ownerCompany.projectId,
                userId,
                projectWorkerRoleId);

        assertRoleChange(projectWorkerRoleId, userId);
    }

    @Test
    public void Given_Worker_When_changeAdminRoleToProjectManager_Then_success() {
        Integer userId = createUserWithRole(projectAdminRoleId);

        testHelper.changeProjectAdminRole(asProjectOwner,
                testProject.ownerCompany.projectId,
                userId,
                projectManagerRoleId);

        assertRoleChange(projectManagerRoleId, userId);
    }

    @Test
    public void Given_Admin_When_changeAdminRoleToOwner_Then_throw() {
        Integer userId = createUserWithRole(projectAdminRoleId);

        testHelper.changeProjectAdminRole(asProjectOwner,
                testProject.ownerCompany.projectId,
                userId,
                projectOwnerRoleId,
                r -> {
                    TestErrorResponse error = r.statusCode(HttpStatus.CONFLICT.value())
                            .extract()
                            .as(TestErrorResponse.class);
                    assertThat(error.getReason(), containsString("cannot be the project owner role"));
                    return null;
                });
    }

    @Test
    public void Given_Admin_When_changeAdminRoleOfNonExistingProject_Then_throw() {
        Integer userId = createUserWithRole(projectAdminRoleId);

        testHelper.changeProjectAdminRole(asProjectOwner,
                -15,
                userId,
                projectManagerRoleId,
                r -> {
                    r.statusCode(HttpStatus.BAD_REQUEST.value());
                    return null;
                });
    }

    @Test
    public void Given_Admin_When_changeAdminRoleToNonExistingRole_Then_throw() {
        Integer userId = createUserWithRole(projectAdminRoleId);

        testHelper.changeProjectAdminRole(asProjectOwner,
                testProject.ownerCompany.projectId,
                userId,
                -15,
                r -> {
                    r.statusCode(HttpStatus.BAD_REQUEST.value());
                    return null;
                });
    }

    @Test
    public void Given_UserWhichIsNotPartOfProject_When_changeAdminRoleToManager_Then_throw() {
        TeamUserDTO userNotPartOfProject = createUser();

        testHelper.changeProjectAdminRole(asProjectOwner,
                testProject.ownerCompany.projectId,
                userNotPartOfProject.getId(),
                projectManagerRoleId,
                r -> {
                    TestErrorResponse error = r.statusCode(HttpStatus.CONFLICT.value())
                            .extract()
                            .as(TestErrorResponse.class);
                    assertThat(error.getReason(), containsString("User isn't part of the Project"));
                    return null;
                });
    }

    @Test
    public void Given_OwnerOfAnotherCompany_When_changeAdminRoleToWorker_Then_throw() {
        Integer ownerOfAnotherCompany = testProject.contractorCompany.ownerId;

        testHelper.changeProjectAdminRole(asProjectOwner,
                testProject.ownerCompany.projectId,
                ownerOfAnotherCompany,
                projectWorkerRoleId,
                r -> {
                    TestErrorResponse error = r.statusCode(HttpStatus.FORBIDDEN.value())
                            .extract()
                            .as(TestErrorResponse.class);
                    assertEquals(error.getErrorCode(), ProjectException.CAN_NOT_CHANGE_ROLE_NOT_YOUR_COMPANY.getErrorCode());
                    return null;
                });
    }

    /**
     * NON ADMIN TESTS
     **/

    @Test
    public void Given_Worker_When_changeNonAdminRoleToManager_Then_success() {
        Integer userId = createUserWithRole(projectWorkerRoleId);

        testHelper.changeProjectNonAdminRole(asProjectOwner,
                testProject.ownerCompany.projectId,
                userId,
                projectManagerRoleId);

        assertRoleChange(projectManagerRoleId, userId);
    }

    @Test
    public void Given_Manager_When_changeNonAdminRoleToWorker_Then_success() {
        Integer userId = createUserWithRole(projectManagerRoleId);

        testHelper.changeProjectNonAdminRole(asProjectOwner,
                testProject.ownerCompany.projectId,
                userId,
                projectWorkerRoleId);

        assertRoleChange(projectWorkerRoleId, userId);
    }

    @Test
    public void Given_Worker_When_changeNonAdminRoleToAdmin_Then_throw() {
        Integer userId = createUserWithRole(projectWorkerRoleId);

        testHelper.changeProjectNonAdminRole(asProjectOwner,
                testProject.ownerCompany.projectId,
                userId,
                projectAdminRoleId,
                r -> {
                    TestErrorResponse error = r.statusCode(HttpStatus.FORBIDDEN.value())
                            .extract()
                            .as(TestErrorResponse.class);
                    assertThat(error.getReason(), containsString("target role or the existing role is project admin"));
                    return null;
                });
    }

    @Test
    public void Given_Admin_When_changeAdminRoleToWorker_Then_throw() {
        Integer userId = createUserWithRole(projectAdminRoleId);

        testHelper.changeProjectNonAdminRole(asProjectOwner,
                testProject.ownerCompany.projectId,
                userId,
                projectWorkerRoleId,
                r -> {
                    TestErrorResponse error = r.statusCode(HttpStatus.FORBIDDEN.value())
                            .extract()
                            .as(TestErrorResponse.class);
                    assertThat(error.getReason(), containsString("target role or the existing role is project admin"));
                    return null;
                });
    }

    @Test
    public void Given_Admin_When_changeAdminRoleToManager_Then_throw() {
        Integer userId = createUserWithRole(projectAdminRoleId);

        testHelper.changeProjectNonAdminRole(asProjectOwner,
                testProject.ownerCompany.projectId,
                userId,
                projectManagerRoleId,
                r -> {
                    TestErrorResponse error = r.statusCode(HttpStatus.FORBIDDEN.value())
                            .extract()
                            .as(TestErrorResponse.class);
                    assertThat(error.getReason(), containsString("target role or the existing role is project admin"));
                    return null;
                });

    }

    @Test
    public void Given_Admin_When_changeNonAdminRoleToOwner_Then_throw() {
        Integer userId = createUserWithRole(projectAdminRoleId);

        testHelper.changeProjectNonAdminRole(asProjectOwner,
                testProject.ownerCompany.projectId,
                userId,
                projectOwnerRoleId,
                r -> {
                    TestErrorResponse error = r.statusCode(HttpStatus.CONFLICT.value())
                            .extract()
                            .as(TestErrorResponse.class);
                    assertThat(error.getReason(), containsString("cannot be the project owner role"));
                    return null;
                });

    }

    @Test
    public void Given_OwnerOfAnotherCompany_When_changeNonAdminRoleToManager_Then_throw() {
        testHelper.changeProjectNonAdminRole(asProjectOwner,
                testProject.ownerCompany.projectId,
                testProject.contractorCompany.workerId,
                projectManagerRoleId,
                r -> {
                    TestErrorResponse error = r.statusCode(HttpStatus.FORBIDDEN.value())
                            .extract()
                            .as(TestErrorResponse.class);
                    assertEquals(error.getErrorCode(), ProjectException.CAN_NOT_CHANGE_ROLE_NOT_YOUR_COMPANY.getErrorCode());
                    return null;
                });

    }

    private void assertRoleChange(Integer roleId, Integer userId) {
        TeamUserDTO[] projectTeam = testHelper.getProjectTeam(asProjectOwner, testProject.ownerCompany.projectId, testProject.ownerCompany.companyId);

        boolean roleSuccessfullyChanged = Arrays
                .stream(projectTeam)
                .anyMatch(tdto -> tdto.getId().equals(userId) && tdto.getRoleId().equals(roleId));

        assertTrue(roleSuccessfullyChanged);
    }

    private Integer createUserWithRole(Integer roleId) {
        TeamUserDTO teamUserDTO = createUser();
        testHelper.addUserToProject(asProjectOwner, testProject.ownerCompany.projectId, teamUserDTO.getId());
        testHelper.changeProjectAdminRole(asProjectOwner, testProject.ownerCompany.projectId, teamUserDTO.getId(), roleId);
        return teamUserDTO.getId();
    }

    private TeamUserDTO createUser() {
        return testHelper.inviteUserAndResetPassword(
                    asProjectOwner,
                    TestBuilder.testTeamUserAddDTO(COMPANY_NAME,
                            RandomStringUtils.randomAlphabetic(10),
                            RandomStringUtils.randomAlphabetic(10))
            );
    }

}
