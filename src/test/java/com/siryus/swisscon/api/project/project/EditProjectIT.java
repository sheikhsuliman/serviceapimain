package com.siryus.swisscon.api.project.project;

import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileRepository;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.DateConverter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static com.siryus.swisscon.api.base.TestHelper.COMPANY_NAME;
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

public class EditProjectIT extends AbstractMvcTestBase {

    private static TestHelper.TestProject testProject;
    private static RequestSpecification asProjectOwner;

    private final ProjectRepository projectRepository;
    private final FileRepository fileRepository;

    @Autowired
    public EditProjectIT(ProjectRepository projectRepository, FileRepository fileRepository) {
        this.projectRepository = projectRepository;
        this.fileRepository = fileRepository;
    }

    @BeforeAll
    public void initTest() {
        testProject = testHelper.createProject();
        asProjectOwner = testHelper.login(PROJECT_OWNER_EMAIL);
    }

    @Test
    public void testEditProjectWithNonExistingProject() {
        testHelper.editProject(asProjectOwner, -15, TestBuilder.testEditProjectDTO(),
                r -> {
                    r.assertThat().statusCode(HttpStatus.FORBIDDEN.value());
                    return null;
                });
    }

    @Test
    public void testEditProjectWithNonExistingType() {
        testHelper.editProject(asProjectOwner, testProject.ownerCompany.projectId,
                TestBuilder.testEditProjectDTO(dto -> {
                    dto.setTypeId(-15);
                    return dto;
                }),
                r -> {
                    r.assertThat().statusCode(HttpStatus.NOT_FOUND.value());
                    return null;
                });
    }

    @Test
    public void testEditProjectWithNonExistingCountry() {
        testHelper.editProject(asProjectOwner, testProject.ownerCompany.projectId,
                TestBuilder.testEditProjectDTO(dto -> {
                    dto.getAddress().setCountryId(-15);
                    return dto;
                }),
                r -> {
                    r.assertThat().statusCode(HttpStatus.NOT_FOUND.value());
                    return null;
                });
    }

    @Test
    public void testEditProjectAsProjectOwner() {
        ProjectDTO project = testHelper.getProject(asProjectOwner, testProject.ownerCompany.projectId);
        Integer oldDefaultImageId = project.getDefaultImage().getId();

        File temporaryFile = testHelper.fileUploadTemporary(asProjectOwner);
        EditProjectDTO editProjectDTO = TestBuilder.testEditProjectDTO(dto -> {
            dto.setDefaultImageId(temporaryFile.getId());
            return dto;
        });

        ProjectBoardDTO projectBoardDTO = testHelper.editProject(asProjectOwner, testProject.ownerCompany.projectId, editProjectDTO);

        assertEditProjectDTO(editProjectDTO, projectBoardDTO);
        assertDefaultImageSavedCorrectly(oldDefaultImageId, editProjectDTO);
    }

    @Test
    public void testEditProjectAsProjectWorker() {
        TeamUserDTO workerDTO = testHelper.inviteUserAndResetPassword(
                asProjectOwner,
                TestBuilder.testTeamUserAddDTO(COMPANY_NAME, PROJECT_WORKER_FIRST_NAME, PROJECT_WORKER_LAST_NAME)
        );

        testHelper.addUserToProject(asProjectOwner, testProject.ownerCompany.projectId, workerDTO.getId());

        RequestSpecification asProjectWorker = testHelper.login(companyEmail(COMPANY_NAME, PROJECT_WORKER_FIRST_NAME, PROJECT_WORKER_LAST_NAME));

        testHelper.editProject(asProjectWorker, testProject.ownerCompany.projectId, TestBuilder.testEditProjectDTO(),
                r -> {
                    r.assertThat().statusCode(HttpStatus.FORBIDDEN.value());
                    return null;
                });
    }

    @Test
    public void testEditProjectNotLoggedIn() {
        testHelper.editProject(defaultSpec(), testProject.ownerCompany.projectId, TestBuilder.testEditProjectDTO(),
                r -> {
                    r.assertThat().statusCode(HttpStatus.UNAUTHORIZED.value());
                    return null;
                });
    }

    @Test
    public void testEditProjectWithAnotherCompany() {
        testHelper.signUp(
                TestBuilder.testSignupDTO(
                        "ANOTHER_COMPANY", PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME
                )
        );
        RequestSpecification anotherCompanyOwner = testHelper.login(companyEmail("ANOTHER_COMPANY", PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME));

        testHelper.editProject(anotherCompanyOwner, testProject.ownerCompany.projectId, TestBuilder.testEditProjectDTO(),
                r -> {
                    r.assertThat().statusCode(HttpStatus.FORBIDDEN.value());
                    return null;
                });
    }

    private void assertEditProjectDTO(EditProjectDTO editProjectDTO, ProjectBoardDTO projectBoardDTO) {
        // assert saved project
        Optional<Project> projectOptional = projectRepository.findById(projectBoardDTO.getId());
        assertTrue(projectOptional.isPresent());
        Project savedProject = projectOptional.get();
        assertEquals(editProjectDTO.getName(), savedProject.getName());
        assertEquals(editProjectDTO.getDescription(), savedProject.getDescription());
        assertEquals(editProjectDTO.getLatitude(), savedProject.getLatitude());
        assertEquals(editProjectDTO.getLongitude(), savedProject.getLongitude());
        assertEquals(editProjectDTO.getStartDate(), DateConverter.toUtcZonedDateTime(savedProject.getStartDate()));
        assertEquals(editProjectDTO.getEndDate(), DateConverter.toUtcZonedDateTime(savedProject.getEndDate()));
        assertEquals(editProjectDTO.getDefaultImageId(), savedProject.getDefaultImage().getId());
        assertEquals(editProjectDTO.getAddress().getAddress(), savedProject.getStreet());
        assertEquals(editProjectDTO.getAddress().getCity(), savedProject.getCity());
        assertEquals(editProjectDTO.getAddress().getPostalCode(), savedProject.getCode());
        assertEquals(editProjectDTO.getAddress().getCountryId(), savedProject.getCountry().getId());

        // assert returned dto
        assertEquals(editProjectDTO.getName(), projectBoardDTO.getName());
        assertEquals(editProjectDTO.getDescription(), projectBoardDTO.getDescription());
        assertEquals(editProjectDTO.getTypeId(), projectBoardDTO.getType().getId());
        assertEquals(editProjectDTO.getLatitude(), projectBoardDTO.getLatitude());
        assertEquals(editProjectDTO.getLongitude(), projectBoardDTO.getLongitude());
        assertEquals(editProjectDTO.getStartDate(), projectBoardDTO.getStartDate());
        assertEquals(editProjectDTO.getEndDate(), projectBoardDTO.getEndDate());

        if (editProjectDTO.getAddress() != null) {
            assertEquals(editProjectDTO.getAddress().getPostalCode(), projectBoardDTO.getAddress().getPostalCode());
            assertEquals(editProjectDTO.getAddress().getAddress(), projectBoardDTO.getAddress().getAddress());
            assertEquals(editProjectDTO.getAddress().getCity(), projectBoardDTO.getAddress().getCity());
            assertEquals(editProjectDTO.getAddress().getCountryId(), projectBoardDTO.getAddress().getCountryId());
        } else {
            assertNull(projectBoardDTO.getAddress());
        }

        if (editProjectDTO.getDefaultImageId() != null) {
            assertEquals(editProjectDTO.getDefaultImageId(), projectBoardDTO.getDefaultImage().getId());
        } else {
            assertNull(projectBoardDTO.getDefaultImage());
        }
    }

    private void assertDefaultImageSavedCorrectly(Integer oldDefaultImageId, EditProjectDTO editProjectDTO) {
        // check if the old file was deleted
        Optional<File> deletedFileOpt = fileRepository.findById(oldDefaultImageId);
        assertTrue(deletedFileOpt.isPresent());
        assertNotNull(deletedFileOpt.get().getDisabled());

        // check if the new file has the right reference
        File file = fileRepository.findById(editProjectDTO.getDefaultImageId()).orElseThrow();
        assertEquals(ReferenceType.PROJECT.toString(), file.getReferenceType());
        assertEquals(testProject.ownerCompany.projectId, file.getReferenceId());
    }

}
