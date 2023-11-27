package com.siryus.swisscon.api.project.project;

import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.siryus.swisscon.api.base.TestHelper.COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.CONTRACTOR_COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_LAST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_WORKER_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_WORKER_LAST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.companyEmail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetProjectIT extends AbstractMvcTestBase {

    private static ProjectBoardDTO testProject1;
    private static ProjectBoardDTO testProject2;
    private static ProjectBoardDTO testProject3;

    private static RequestSpecification asCompanyOwner1;
    private static RequestSpecification asCompanyOwner2;
    private static RequestSpecification asCompanyWorker;

    private static final String COMPANY_NAME_2 = COMPANY_NAME + 2;
    private static final String PROJECT_OWNER_FIRST_NAME_2 = PROJECT_OWNER_FIRST_NAME + 2;
    private static final String PROJECT_OWNER_LAST_NAME_2 = PROJECT_OWNER_LAST_NAME + 2;

    @BeforeAll
    public void initOwnerAndProjects() {
        signupAsOwner1();
        signupAsOwner2();

        asCompanyOwner1 = testHelper.login(PROJECT_OWNER_EMAIL);
        String companyEmail2 = companyEmail(COMPANY_NAME_2, PROJECT_OWNER_FIRST_NAME_2, PROJECT_OWNER_LAST_NAME_2);
        asCompanyOwner2 = testHelper.login(companyEmail2);
        TeamUserDTO teamUserDTO = inviteProjectWorker(asCompanyOwner1);

        String workerEmail = companyEmail(CONTRACTOR_COMPANY_NAME, PROJECT_WORKER_FIRST_NAME, PROJECT_WORKER_LAST_NAME);
        asCompanyWorker = testHelper.login(workerEmail);

        testProject1 = testHelper.createProject(asCompanyOwner1, TestBuilder.testNewProjectDTO("project 1"));
        testProject2 = testHelper.createProject(asCompanyOwner1, TestBuilder.testNewProjectDTO("project 2"));
        testProject3 = testHelper.createProject(asCompanyOwner2, TestBuilder.testNewProjectDTO("project 3"));

        testHelper.addUserToProject(asCompanyOwner1, testProject1.getId(), teamUserDTO.getId());
        testHelper.addUserToProject(asCompanyOwner1, testProject2.getId(), teamUserDTO.getId());
    }

    private void signupAsOwner1() {
        testHelper.signUp(TestBuilder.testSignupDTO(COMPANY_NAME, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME));
    }

    private void signupAsOwner2() {
        testHelper.signUp(TestBuilder.testSignupDTO(COMPANY_NAME_2, PROJECT_OWNER_FIRST_NAME_2, PROJECT_OWNER_LAST_NAME_2));
    }

    private TeamUserDTO inviteProjectWorker(RequestSpecification asCompanyOwner) {
        return testHelper.inviteUserAndResetPassword(
                asCompanyOwner,
                TestBuilder.testTeamUserAddDTO(CONTRACTOR_COMPANY_NAME, PROJECT_WORKER_FIRST_NAME, PROJECT_WORKER_LAST_NAME)
        );
    }

    @Test
    public void testGetMyProjectsAsOwner() {
        List<ProjectDTO> projectsCompany1 = testHelper.getProjects(asCompanyOwner1);

        assertEquals(2, projectsCompany1.size());
        ProjectDTO testProject1DTO = projectsCompany1.stream().filter(p -> p.getId().equals(testProject1.getId())).findFirst().orElseThrow();
        ProjectDTO testProject2DTO = projectsCompany1.stream().filter(p -> p.getId().equals(testProject2.getId())).findFirst().orElseThrow();
        assertProjectDTO(testProject1DTO, testProject1);
        assertProjectDTO(testProject2DTO, testProject2);

        List<ProjectDTO> projectsCompany2 = testHelper.getProjects(asCompanyOwner2);

        assertEquals(1, projectsCompany2.size());
        ProjectDTO testProject3DTO = projectsCompany2.stream().filter(p -> p.getId().equals(testProject3.getId())).findFirst().orElseThrow();
        assertProjectDTO(testProject3DTO, testProject3);
    }

    @Test
    public void testGetMyProjectsAsWorker() {
        List<ProjectDTO> projectsCompany1 = testHelper.getProjects(asCompanyWorker);
        assertEquals(2, projectsCompany1.size());
    }

    //TODO has no correct permissions yet in controller
    @Test
    public void testGetMyProjectWithoutLogin() {
        testHelper.getProjects(
                defaultSpec(),
                r -> {
                    r.assertThat().statusCode(HttpStatus.UNAUTHORIZED.value());
                    return null;
                }
        );
    }
    
    @Test
    public void testGetProjectBoardAsOwner() {
        ProjectBoardDTO projectBoardDTO = testHelper.getProjectBoard(asCompanyOwner1, testProject1.getId());
        assertProjectBoardDTO(testProject1, projectBoardDTO);
    }

    @Test
    public void testGetProjectBoardAsWorker() {
        ProjectBoardDTO projectBoardDTO = testHelper.getProjectBoard(asCompanyWorker, testProject1.getId());
        assertProjectBoardDTO(testProject1, projectBoardDTO);
    }

    @Test
    public void testGetProjectBoardProjectOfOtherCompany() {
        testHelper.getProjectBoard(asCompanyOwner2, testProject1.getId(),
                r-> {
                    r.assertThat().statusCode(HttpStatus.FORBIDDEN.value());
                    return null;
                });
    }

    @Test
    public void testGetProjectBoardNotLoggedIn() {
        testHelper.getProjectBoard(defaultSpec(), testProject1.getId(),
                r-> {
                    r.assertThat().statusCode(HttpStatus.UNAUTHORIZED.value());
                    return null;
                });
    }

    @Test
    public void testGetProjectDetailsAsOwner() {
        ProjectDTO projectDTO = testHelper.getProject(asCompanyOwner1, testProject1.getId());
        assertProjectDTO(projectDTO, testProject1);
    }

    @Test
    public void testGetProjectDetailsAsWorker() {
        ProjectDTO projectDTO = testHelper.getProject(asCompanyWorker, testProject1.getId());
        assertProjectDTO(projectDTO, testProject1);
    }

    @Test
    public void testGetProjectDetailsNotLoggedIn() {
        testHelper.getProject(defaultSpec(), testProject1.getId(),
                r-> {
                    r.assertThat().statusCode(HttpStatus.UNAUTHORIZED.value());
                    return null;
                });
    }

    @Test
    public void testGetProjectDetailsOfAnothersCompaniesProject() {
        testHelper.getProject(asCompanyOwner1, testProject3.getId(),
                r -> {
                    r.assertThat().statusCode(HttpStatus.FORBIDDEN.value());
                    return null;
                });
    }

    private void assertProjectBoardDTO(ProjectBoardDTO left, ProjectBoardDTO right) {
        assertEquals(left.getId(), right.getId());
        assertEquals(left.getName(), right.getName());
        assertEquals(left.getType().getId(), right.getType().getId());
        assertEquals(left.getStatus().getId(), right.getStatus().getId());
        assertEquals(left.getDescription(), right.getDescription());
        assertEquals(left.getLatitude(), right.getLatitude());
        assertEquals(left.getLongitude(), right.getLongitude());

        assertEquals(left.getStartDate(), right.getStartDate());
        assertEquals(left.getEndDate(), right.getEndDate());
        assertEquals(left.getStarred(), right.getStarred());

        if(left.getAddress() != null) {
            assertEquals(left.getAddress().getCountryId(), right.getAddress().getCountryId());
            assertEquals(left.getAddress().getCity(), right.getAddress().getCity());
            assertEquals(left.getAddress().getAddress(), right.getAddress().getAddress());
            assertEquals(left.getAddress().getPostalCode(), right.getAddress().getPostalCode());
        } else {
            assertNull(right.getAddress());
        }

        if(left.getDefaultImage() != null) {
            assertEquals(left.getDefaultImage().getId(), right.getDefaultImage().getId());
        } else {
            assertNull(right.getDefaultImage());
        }
    }

    private void assertProjectDTO(ProjectDTO projectDTO, ProjectBoardDTO projectBoardDTO) {
        assertEquals(projectBoardDTO.getId(), projectDTO.getId());
        assertEquals(projectBoardDTO.getName(), projectDTO.getName());
        assertEquals(projectBoardDTO.getType().getId(), projectDTO.getType().getId());
        assertEquals(projectBoardDTO.getStarred(), projectBoardDTO.getStarred());
        assertEquals(projectBoardDTO.getStartDate().toLocalDateTime(), projectDTO.getStartDate());
        assertEquals(projectBoardDTO.getStartDate().toLocalDateTime(), projectDTO.getStartDate());
        assertNotNull(projectDTO.getProgressPercentage());
        assertNotNull(projectDTO.getStatus());

        if(projectBoardDTO.getDefaultImage() != null) {
            assertEquals(projectBoardDTO.getDefaultImage().getId(), projectDTO.getDefaultImage().getId());
        }

        boolean teamContainsOwner = projectBoardDTO.getTeamMembers().stream()
                .anyMatch(t -> projectDTO.getProjectOwner().getFirstName().equals(t.getFirstName()));
        assertTrue(teamContainsOwner);

        if(projectBoardDTO.getAddress() != null) {
            assertEquals(projectBoardDTO.getAddress().getCountryId(), projectDTO.getAddress().getCountryId());
            assertEquals(projectBoardDTO.getAddress().getPostalCode(), projectDTO.getAddress().getPostalCode());
            assertEquals(projectBoardDTO.getAddress().getCity(), projectDTO.getAddress().getCity());
            assertEquals(projectBoardDTO.getAddress().getAddress(), projectDTO.getAddress().getAddress());
        }
    }

}
