package com.siryus.swisscon.api.project.project;

import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.signup.SignupResponseDTO;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.FailFastExtension;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyDirectoryDTO;
import com.siryus.swisscon.api.company.company.CompanyRepository;
import com.siryus.swisscon.api.company.company.SimpleCompanyDTO;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.util.error.TestErrorResponse;
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
import java.util.List;

import static com.siryus.swisscon.api.base.TestHelper.COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.CONTRACTOR_COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.companyEmail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FailFastExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectCompanyIT extends AbstractMvcTestBase {

    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;

    private static TestHelper.ExtendedTestProject testProject;
    private static RequestSpecification asProjectOwner;
    private static RequestSpecification asOtherCompanyOwner;
    private static Integer projectId;
    private static Integer otherCompanyId;
    private static Integer otherCompanyOwnerId;

    private static final String OTHER_COMPANY = "other-company";
    private static final String OTHER_COMPANY_OWNER_FIRST_NAME = "other-first-name";
    private static final String OTHER_COMPANY_OWNER_LAST_NAME = "other-last-name";
    private static final String OTHER_COMPANY_EMAIL = companyEmail(OTHER_COMPANY, OTHER_COMPANY_OWNER_FIRST_NAME, OTHER_COMPANY_OWNER_LAST_NAME);
    private static final String TEST_LOCATION = "test-location";
    private static final String TEST_TASK = "test-task";


    @Autowired
    public ProjectCompanyIT(CompanyRepository companyRepository, RoleRepository roleRepository) {
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
    }

    @BeforeAll
    public void initTest() {
        testProject = testHelper.createExtendedProject(COMPANY_NAME, CONTRACTOR_COMPANY_NAME);
        projectId = testProject.ownerCompany.projectId;
        testHelper.addLocationToExtendedProject(testProject, TEST_LOCATION);

        asProjectOwner = testHelper.login(TestHelper.PROJECT_OWNER_EMAIL);

        SignupResponseDTO signupResponseDTO = testHelper.signUp(TestBuilder.testSignupDTO(OTHER_COMPANY, OTHER_COMPANY_OWNER_FIRST_NAME, OTHER_COMPANY_OWNER_LAST_NAME));
        otherCompanyId = signupResponseDTO.getCompanyId();
        otherCompanyOwnerId = signupResponseDTO.getUserId();

        asOtherCompanyOwner = testHelper.login(OTHER_COMPANY_EMAIL);
    }

    @Test
    @Order(0)
    public void testGetAvailableCompaniesToAddNotLoggedIn() {
        testHelper.getAvailableCompaniesToAddToProject(defaultSpec(), projectId,
                r -> {
                    r.assertThat().statusCode(HttpStatus.UNAUTHORIZED.value());
                    return null;
                });
    }

    @Test
    @Order(1)
    public void testGetAvailableCompaniesToAddButLoggedInUserNotPartOfProject() {
        testHelper.getAvailableCompaniesToAddToProject(asOtherCompanyOwner, projectId,
                r -> {
                    r.assertThat().statusCode(HttpStatus.FORBIDDEN.value());
                    return null;
                });
    }

    @Test
    @Order(2)
    public void testGetAvailableCompaniesOtherCompanyInList() {
        List<SimpleCompanyDTO> availableCompanies = testHelper
                .getAvailableCompaniesToAddToProject(asProjectOwner, projectId);

        assertFalse(availableCompanies.isEmpty());
        SimpleCompanyDTO otherCompanyDTO = availableCompanies.stream().filter(c -> c.getId().equals(otherCompanyId)).findFirst().orElseThrow();
        assertAvailableCompany(otherCompanyDTO);
    }

    @Test
    @Order(3)
    public void testGetCompaniesOfProjectWhenOnlyOwnerAndContractorArePartOf() {
        List<CompanyDirectoryDTO> projectCompanies = testHelper.getProjectCompanies(asProjectOwner, projectId);
        assertTrue(projectCompanies.stream()
                .anyMatch(pc->pc.getId().equals(testProject.ownerCompany.companyId)));
        assertTrue(projectCompanies.stream()
                .anyMatch(pc->pc.getId().equals(testProject.contractorCompany.companyId)));
    }

    @Test
    @Order(4)
    public void testAddCompanyToNonExistingProject() {
        testHelper.addCompanyToProject(asProjectOwner, -15, otherCompanyId,
                r -> {
                    r.assertThat().statusCode(HttpStatus.FORBIDDEN.value());
                    return null;
                });
    }

    @Test
    @Order(5)
    public void testAddNonExistingCompanyToProject() {
        testHelper.addCompanyToProject(asProjectOwner, projectId, -15,
                r -> {
                    r.assertThat().statusCode(HttpStatus.NOT_FOUND.value());
                    return null;
                });
    }

    @Test
    @Order(6)
    public void testAddCompanyToProjectButLoggedInUserNotPartOfProject() {
        testHelper.addCompanyToProject(asOtherCompanyOwner, projectId, otherCompanyId,
                r -> {
                    r.assertThat().statusCode(HttpStatus.FORBIDDEN.value());
                    return null;
                });
    }

    @Test
    @Order(7)
    public void testRemoveCompanyFromProjectLoggedInWithUserNotPartOfProject() {
        testHelper.removeCompanyFromProject(asOtherCompanyOwner, projectId, testProject.contractorCompany.companyId,
                r -> r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @Order(8)
    public void Given_validInitialTestProject_When_addCompanyToProjectWithDefaultRole_Then_companyIsAddedAndOwnerHasDefaultRole() {
        testHelper.addCompanyToProject(asProjectOwner, projectId, otherCompanyId,
                r -> {
                    r.assertThat().statusCode(HttpStatus.OK.value());
                    return null;
                });

        // check that after you add a company > their owner is a project manager by default
        TeamUserDTO[] projectTeam = testHelper.getProjectTeam(asProjectOwner, projectId, otherCompanyId);
        TeamUserDTO otherCompanyOwner = Arrays.stream(projectTeam).filter(t -> t.getId().equals(otherCompanyOwnerId)).findFirst().orElseThrow();
        assertEquals(otherCompanyOwner.getRoleId(), roleRepository.getRoleByName(RoleName.PROJECT_MANAGER.toString()).getId());
    }

    @Test
    @Order(9)
    public void testGetCompaniesOfProjectWhen3CompaniesArePartOf() {
        List<CompanyDirectoryDTO> projectCompanies = testHelper.getProjectCompanies(asProjectOwner, projectId);
        assertTrue(projectCompanies.stream()
                .anyMatch(pc->pc.getId().equals(testProject.ownerCompany.companyId)));
        assertTrue(projectCompanies.stream()
                .anyMatch(pc->pc.getId().equals(testProject.contractorCompany.companyId)));
        assertTrue(projectCompanies.stream()
                .anyMatch(pc->pc.getId().equals(otherCompanyId)));
    }

    @Test
    @Order(10)
    public void testGetAvailableCompaniesOtherCompanyNonInListAnymore() {
        List<SimpleCompanyDTO> availableCompanies = testHelper.getAvailableCompaniesToAddToProject(asProjectOwner, projectId);
        assertTrue(availableCompanies.stream().noneMatch(c -> c.getId().equals(otherCompanyId)));
    }

    @Test
    @Order(11)
    public void testRemoveCompanyFromProjectNotLoggedIn() {
        testHelper.removeCompanyFromProject(defaultSpec(), projectId, otherCompanyId,
                r -> r.assertThat().statusCode(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    @Order(12)
    public void testRemoveCompanyFromProjectNotExistingProject() {
        testHelper.removeCompanyFromProject(asProjectOwner, -15, otherCompanyId,
                r -> r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @Order(13)
    public void testRemoveCompanyFromProjectNotExistingCompany() {
        testHelper.removeCompanyFromProject(asProjectOwner, projectId, -15,
                r -> r.assertThat().statusCode(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @Order(14)
    public void testRemoveProjectOwnersCompanyLoggedInAsAnotherCompany() {
        testHelper.removeCompanyFromProject(asOtherCompanyOwner, projectId, testProject.ownerCompany.companyId,
               r-> r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @Order(15)
    public void testRemoveCompanyFromProjectWhichUserHasTaskAssigned() {
        // adding the task will automatically add the other company owner to the task team
        // which means that we cannot remove the other company anymore
        MainTaskDTO mainTaskDTO = testHelper.addContractualTask(testProject.ownerCompany.asAdmin, TestBuilder.testCreateMainTaskRequest(
                testProject.locationId(TEST_LOCATION),
                TEST_TASK,
                otherCompanyId
        ));

        testHelper.removeCompanyFromProject(asProjectOwner, projectId, otherCompanyId,
                r -> {
                    TestErrorResponse error = r.assertThat().statusCode(HttpStatus.CONFLICT.value())
                            .extract().as(TestErrorResponse.class);
                    assertThat(error.getReason(), containsString("user is assigned to tasks"));
                    assertThat(error.getReason(), containsString(otherCompanyOwnerId.toString()));
                });

        testHelper.removeUserFromMainTask(asOtherCompanyOwner, mainTaskDTO.getId(), otherCompanyOwnerId);
    }

    @Test
    @Order(16)
    public void testRemoveOtherCompanyAsProjectOwner() {
        testHelper.removeCompanyFromProject(asProjectOwner, projectId, otherCompanyId,
                r -> r.assertThat().statusCode(HttpStatus.OK.value()));
        testGetAvailableCompaniesOtherCompanyInList();
    }

    @Test
    @Order(17)
    public void Given_validInitialTestProject_When_addCompanyToProjectWithSpecificRole_Then_companyIsAddedAndOwnerHasSpecificRole() {      
        Integer workerRoleId = roleRepository.getRoleByName(RoleName.PROJECT_WORKER.name()).getId();

        testHelper.addCompanyToProject(asProjectOwner, projectId, otherCompanyId, workerRoleId,
            r -> {
                r.assertThat().statusCode(HttpStatus.OK.value());
                return null;
            }
        );

        TeamUserDTO[] projectTeam = testHelper.getProjectTeam(asProjectOwner, projectId, otherCompanyId);        
        TeamUserDTO otherCompanyOwner = Arrays.stream(projectTeam).filter(t -> t.getId().equals(otherCompanyOwnerId)).findFirst().orElseThrow();        
        assertEquals(otherCompanyOwner.getRoleId(), workerRoleId);
    }
    
    private void assertAvailableCompany(SimpleCompanyDTO otherCompanyDTO) {
        Company otherCompany = companyRepository.findById(otherCompanyId).orElseThrow();
        assertEquals(otherCompany.getId(), otherCompanyDTO.getId());
        assertEquals(otherCompany.getName(), otherCompanyDTO.getName());
        if (otherCompany.getPicture() != null) {
            assertEquals(otherCompany.getPicture().getId(), otherCompanyDTO.getPicture().getId());
            assertEquals(otherCompany.getPicture().getUrl(), otherCompanyDTO.getPicture().getUrl());
            assertEquals(otherCompany.getPicture().getUrlMedium(), otherCompanyDTO.getPicture().getUrlMedium());
            assertEquals(otherCompany.getPicture().getUrlSmall(), otherCompanyDTO.getPicture().getUrlSmall());
        }
    }

}
