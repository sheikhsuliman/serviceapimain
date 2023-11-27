package com.siryus.swisscon.api.project.project;

import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.signup.SignupResponseDTO;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileRepository;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.location.location.LocationDetailsDTO;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompany;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompanyRepository;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleRepository;
import com.siryus.swisscon.api.util.DateConverter;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.siryus.swisscon.api.base.TestHelper.COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.CONTRACTOR_COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_ADMIN_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_ADMIN_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_ADMIN_LAST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_LAST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_WORKER_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_WORKER_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_WORKER_LAST_NAME;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateProjectIT extends AbstractMvcTestBase {

    private static SignupResponseDTO companyOwnerSignupResponse;
    private static TeamUserDTO adminTeamUserDTO;

    private final FileRepository fileRepository;
    private final ProjectRepository projectRepository;
    private final ProjectCompanyRepository projectCompanyRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;

    @Autowired
    public CreateProjectIT(FileRepository fileRepository, ProjectRepository projectRepository, ProjectCompanyRepository projectCompanyRepository, CompanyUserRoleRepository companyUserRoleRepository, ProjectUserRoleRepository projectUserRoleRepository) {
        this.fileRepository = fileRepository;
        this.projectRepository = projectRepository;
        this.projectCompanyRepository = projectCompanyRepository;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.projectUserRoleRepository = projectUserRoleRepository;
    }

    @BeforeAll
    public void init() {
        signupAsOwner();

        RequestSpecification asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);

        inviteProjectAdmin(asCompanyOwner);
        inviteProjectWorker(asCompanyOwner);
    }

    private void signupAsOwner() {
        companyOwnerSignupResponse = testHelper.signUp(TestBuilder.testSignupDTO(COMPANY_NAME, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME));
    }

    private void inviteProjectWorker(RequestSpecification asCompanyOwner) {
        testHelper.inviteUserAndResetPassword(
                asCompanyOwner,
                TestBuilder.testTeamUserAddDTO(CONTRACTOR_COMPANY_NAME, PROJECT_WORKER_FIRST_NAME, PROJECT_WORKER_LAST_NAME)
        );
    }

    private void inviteProjectAdmin(RequestSpecification asCompanyOwner) {
        adminTeamUserDTO = testHelper.inviteUserAndResetPassword(
                asCompanyOwner,
                TestBuilder.testTeamUserAddDTO(COMPANY_NAME, PROJECT_ADMIN_FIRST_NAME, PROJECT_ADMIN_LAST_NAME)
        );
        testHelper.toggleAdmin(asCompanyOwner, adminTeamUserDTO.getId(), true);
    }

    @Test
    public void testCreateProjectInvalidProjectType() {
        NewProjectDTO invalidDTO = TestBuilder.testNewProjectDTO("invalid", pDTO -> {
            pDTO.setTypeId(-15);
            return pDTO;
        });
        Function<ValidatableResponse, ProjectBoardDTO> statusNotFound = r -> {
            r.assertThat().statusCode(HttpStatus.NOT_FOUND.value());
            return null;
        };
        RequestSpecification asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);

        testHelper.createProject(asCompanyOwner, invalidDTO, statusNotFound);
    }

    @Test
    public void testCreateProjectInvalidCountry() {
        NewProjectDTO invalidDTO = TestBuilder.testNewProjectDTO("invalid", pDTO -> {
            pDTO.setAddress(AddressDTO.builder().countryId(-15).build());
            return pDTO;
        });
        Function<ValidatableResponse, ProjectBoardDTO> statusNotFound = r -> {
            r.assertThat().statusCode(HttpStatus.NOT_FOUND.value());
            return null;
        };
        RequestSpecification asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);

        testHelper.createProject(asCompanyOwner, invalidDTO, statusNotFound);
    }

    @Test
    public void testCreateProjectAsCompanyWorkerUnauthorized() {
        RequestSpecification asCompanyWorker = testHelper.login(PROJECT_WORKER_EMAIL);

        ProjectBoardDTO funProject = testHelper.createProject(
                asCompanyWorker, TestBuilder.testNewProjectDTO("Fun Project"),
                r -> {
                    r.assertThat().statusCode(HttpStatus.FORBIDDEN.value());
                    return null;
                }
        );

        assertNull(funProject);
    }

    @Test
    public void testCreateProjectNotLoggedIn() {

        ProjectBoardDTO funProject = testHelper.createProject(
                defaultSpec(), TestBuilder.testNewProjectDTO("Fun Project"),
                r -> {
                    r.assertThat().statusCode(HttpStatus.UNAUTHORIZED.value());
                    return null;
                }
        );

        assertNull(funProject);
    }

    @Test
    public void testCreateProjectAsCompanyOwner() {
        RequestSpecification asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);

        File temporaryFile = testHelper.fileUploadTemporary(asCompanyOwner);

        NewProjectDTO newProjectDTOWithImage = TestBuilder.testNewProjectDTO("test project", pDTO -> {
            pDTO.setDefaultImageId(temporaryFile.getId());
            return pDTO;
        });

        ProjectBoardDTO projectBoardDTO = testHelper.createProject(asCompanyOwner, newProjectDTOWithImage);

        List<LocationDetailsDTO> projectLocations = testHelper.getChildren(asCompanyOwner, projectBoardDTO.getId(), null);
        TestAssert.assertContainsLocationWithName(projectLocations, "test project");

        assertSavedProject(newProjectDTOWithImage, projectBoardDTO, companyOwnerSignupResponse.getUserId());

        testAssert.assertContainsNewEmptyDefaultMediaFolders(
                testHelper.listFiles(asCompanyOwner,
                        ReferenceType.PROJECT,
                        projectBoardDTO.getId()),
                ReferenceType.PROJECT,
                projectBoardDTO.getId());
    }

    @Test
    public void testCreateProjectAsCompanyAdmin() {

        RequestSpecification asCompanyAdmin = testHelper.login(PROJECT_ADMIN_EMAIL);

        File temporaryFile = testHelper.fileUploadTemporary(asCompanyAdmin);

        NewProjectDTO newProjectDTOWithImage = TestBuilder.testNewProjectDTO("test project", pDTO -> {
            pDTO.setDefaultImageId(temporaryFile.getId());
            return pDTO;
        });

        ProjectBoardDTO projectBoardDTO = testHelper.createProject(asCompanyAdmin,newProjectDTOWithImage);

        assertSavedProject(newProjectDTOWithImage, projectBoardDTO, adminTeamUserDTO.getId());
    }

    @Test
    public void testCreateProjectAsCompanyAdminPictureUploadedByOtherUser() {
        RequestSpecification asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);

        File temporaryFile = testHelper.fileUploadTemporary(asCompanyOwner);

        NewProjectDTO newProjectDTOWithImage = TestBuilder.testNewProjectDTO("test project", pDTO -> {
            pDTO.setDefaultImageId(temporaryFile.getId());
            return pDTO;
        });

        RequestSpecification asCompanyAdmin = testHelper.login(PROJECT_ADMIN_EMAIL);

        testHelper.createProject(
                asCompanyAdmin, newProjectDTOWithImage,
                r -> {
                    r.assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
                    return null;
                }
        );
    }

    private void assertSavedProject(NewProjectDTO newProjectDTO, ProjectBoardDTO projectBoardDTO, Integer createdUserId) {
        // assert saved project
        Optional<Project> projectOptional = projectRepository.findById(projectBoardDTO.getId());
        Project savedTestProject = projectOptional.orElseThrow();
        assertEquals(newProjectDTO.getName(), savedTestProject.getName());
        assertEquals(newProjectDTO.getTypeId(), savedTestProject.getType().getId());
        assertEquals(newProjectDTO.getDescription(), savedTestProject.getDescription());
        assertEquals(newProjectDTO.getLatitude(), savedTestProject.getLatitude());
        assertEquals(newProjectDTO.getLongitude(), savedTestProject.getLongitude());
        assertEquals(newProjectDTO.getStartDate(), DateConverter.toUtcZonedDateTime(savedTestProject.getStartDate()));
        assertEquals(newProjectDTO.getEndDate(), DateConverter.toUtcZonedDateTime(savedTestProject.getEndDate()));
        assertNotNull(savedTestProject.getDefaultImage());
        assertEquals(newProjectDTO.getDefaultImageId(), savedTestProject.getDefaultImage().getId());
        assertEquals(newProjectDTO.getAddress().getAddress(), savedTestProject.getStreet());
        assertEquals(newProjectDTO.getAddress().getCity(), savedTestProject.getCity());
        assertEquals(newProjectDTO.getAddress().getPostalCode(), savedTestProject.getCode());
        assertEquals(newProjectDTO.getAddress().getCountryId(), savedTestProject.getCountry().getId());
        assertTrue(projectBoardDTO.getProjectCustomerIsReassignable());

        // assert returned dto
        assertNotNull(projectBoardDTO);
        assertEquals(savedTestProject.getId(), projectBoardDTO.getId());
        assertEquals(newProjectDTO.getName(), projectBoardDTO.getName());
        assertEquals(newProjectDTO.getTypeId(), projectBoardDTO.getType().getId());
        assertEquals(newProjectDTO.getDefaultImageId(), projectBoardDTO.getDefaultImage().getId());
        assertEquals(newProjectDTO.getAddress().getAddress(), projectBoardDTO.getAddress().getAddress());
        assertEquals(newProjectDTO.getAddress().getCity(), projectBoardDTO.getAddress().getCity());
        assertEquals(newProjectDTO.getAddress().getPostalCode(), projectBoardDTO.getAddress().getPostalCode());
        assertEquals(newProjectDTO.getAddress().getCountryId(), projectBoardDTO.getAddress().getCountryId());

        // assert project company
        ProjectCompany testProjectCompany = projectCompanyRepository.findByProjectAndCompany(savedTestProject.getId(), companyOwnerSignupResponse.getCompanyId());
        List<CompanyUserRole> companyUserRoles = companyUserRoleRepository.findByUserAndCompany(companyOwnerSignupResponse.getUserId(), companyOwnerSignupResponse.getCompanyId());
        assertFalse(companyUserRoles.isEmpty());
        assertNotNull(testProjectCompany);
        assertTrue(companyUserRoles.stream().anyMatch(cur->cur.getCompany().getId().equals(companyOwnerSignupResponse.getCompanyId())));
        assertEquals(savedTestProject.getId(), testProjectCompany.getProject().getId());

        // assert project user role
        List<ProjectUserRole> projectTeam = projectUserRoleRepository.findProjectTeam(savedTestProject.getId());
        assertEquals(1, projectTeam.size());
        ProjectUserRole testProjectUserRole = projectTeam.get(0);
        assertEquals(RoleName.PROJECT_OWNER.toString(), testProjectUserRole.getRole().getName());
        assertEquals(createdUserId, testProjectUserRole.getUser().getId());

        // assert File References
        Optional<File> file = fileRepository.findById(newProjectDTO.getDefaultImageId());
        assertTrue(file.isPresent());
        assertEquals(ReferenceType.PROJECT.toString(), file.get().getReferenceType());
        assertEquals(savedTestProject.getId(), file.get().getReferenceId());
    }

}
