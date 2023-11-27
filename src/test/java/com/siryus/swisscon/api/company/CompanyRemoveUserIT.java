package com.siryus.swisscon.api.company;

import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompany;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompanyRepository;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleRepository;
import com.siryus.swisscon.api.util.error.TestErrorResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

public class CompanyRemoveUserIT extends AbstractMvcTestBase {

    private static final String PATH = BASE_PATH + "/companies/";

    private final UserRepository userRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final RoleRepository roleRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final ProjectCompanyRepository projectCompanyRepository;

    private static User testUser;
    private static CompanyUserRole testCompanyUserRole;
    private static ProjectUserRole testProjectUserRole;

    @Autowired
    public CompanyRemoveUserIT(UserRepository userRepository, CompanyUserRoleRepository companyUserRoleRepository, RoleRepository roleRepository, ProjectUserRoleRepository projectUserRoleRepository, ProjectCompanyRepository projectCompanyRepository) {
        this.userRepository = userRepository;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.roleRepository = roleRepository;
        this.projectUserRoleRepository = projectUserRoleRepository;
        this.projectCompanyRepository = projectCompanyRepository;
    }

    @Test
    public void testDeleteUserWithoutAnyProject() {
        given()
                .spec(loginSpec())
                .queryParam("user", testUser.getId())
                .post(PATH + "1/remove-user")
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.OK.value()));

        // check if the user is disconnected of the company
        List<CompanyUserRole> companyUserRoles = companyUserRoleRepository.findByUserAndCompany(testUser.getId(), 1);
        Optional<CompanyUserRole> testCompanyUserRoleOpt = companyUserRoleRepository.findById(testCompanyUserRole.getId());
        assertTrue(companyUserRoles.isEmpty());
        assertFalse(testCompanyUserRoleOpt.isPresent());

        // check if user is disabled
        assertNotNull(testUser.getId());
        Optional<User> userOpt = userRepository.findById(testUser.getId());
        assertTrue(userOpt.isPresent());
        assertNotNull(userOpt.get().getDisabled());
    }

    @Test
    public void testDeleteUserWhichIsNotPartOfTheTeam() {
        companyUserRoleRepository.deleteById(testCompanyUserRole.getId());

        TestErrorResponse error = given()
                .spec(loginSpec())
                .queryParam("user", testUser.getId())
                .post(PATH + "1/remove-user")
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.CONFLICT.value()))
                .extract()
                .as(TestErrorResponse.class);

        assertNotNull(error.getReason());
        assertEquals(CompanyExceptions.USER_IS_NOT_FROM_COMPANY.getErrorCode(), error.getErrorCode());

        // check if user is not disabled
        assertNotNull(testUser.getId());
        Optional<User> userOpt = userRepository.findById(testUser.getId());
        assertTrue(userOpt.isPresent());
        assertNull(userOpt.get().getDisabled());
    }

    @Test
    public void testDeleteUserWhichHasAProjectAssigned() {
        // assign the test user to project 1 (so we are not allowed to delete him anymore)
        ProjectCompany projectCompany = projectCompanyRepository.findByProjectAndCompany(1, 1);
        ProjectUserRole projectUserRole = ProjectUserRole.builder()
                .projectCompany(projectCompany)
                .project(projectCompany.getProject())
                .user(testUser)
                .role(roleRepository.getRoleByName(RoleName.PROJECT_WORKER.toString()))
                .build();
        testProjectUserRole = projectUserRoleRepository.save(projectUserRole);

        TestErrorResponse error = given()
                .spec(loginSpec())
                .queryParam("user", testUser.getId())
                .post(PATH + "1/remove-user")
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.CONFLICT.value()))
                .extract()
                .as(TestErrorResponse.class);

        assertNotNull(error.getReason());
        assertTrue(error.getReason().contains("User cannot be deleted because he is part in projects"));

        // check if user is not disabled
        assertNotNull(testUser.getId());
        Optional<User> userOpt = userRepository.findById(testUser.getId());
        assertTrue(userOpt.isPresent());
        assertNull(userOpt.get().getDisabled());
    }

    @Test
    public void testDeleteUserWithInvalidCompanyId() {
        given()
                .spec(loginSpec())
                .queryParam("user", testUser.getId())
                .post(PATH + "987654312/remove-user")
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.FORBIDDEN.value()))
                .extract()
                .as(TestErrorResponse.class);
    }

    @Test
    public void testDeleteUserWithInvalidUserId() {
        given()
                .spec(loginSpec())
                .queryParam("user", 231654978)
                .post(PATH + "1/remove-user")
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.BAD_REQUEST.value()))
                .extract()
                .as(TestErrorResponse.class);
    }

    @BeforeEach
    public void initTestData() {
        testUser = createTestUser();
        testCompanyUserRole = createCompanyUserRole();
    }

    @AfterEach
    public void cleanTestData() {
        Optional.ofNullable(testProjectUserRole).ifPresent(e -> deleteIfExists(projectUserRoleRepository, e.getId()));
        Optional.ofNullable(testCompanyUserRole).ifPresent(e -> deleteIfExists(companyUserRoleRepository, e.getId()));
        Optional.ofNullable(testUser).ifPresent(e -> deleteIfExists(userRepository, e.getId()));
        testProjectUserRole = null;
        testCompanyUserRole = null;
        testUser = null;
    }

    private User createTestUser() {
        String email = RandomStringUtils.randomAlphabetic(10) + "@test.com";
        User tempUser = User.builder().username(email).build();
        tempUser.setEmail(email);
        tempUser.setPassword(RandomStringUtils.randomAlphabetic(10));
        return userRepository.save(tempUser);
    }

    private CompanyUserRole createCompanyUserRole() {
        CompanyUserRole companyUserRole = CompanyUserRole.builder()
                .role(roleRepository.getRoleByName(RoleName.COMPANY_WORKER.toString()))
                .user(testUser)
                .company(Company.builder().id(1).build())
                .build();
        return companyUserRoleRepository.save(companyUserRole);
    }

}
