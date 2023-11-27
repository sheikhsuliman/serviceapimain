package com.siryus.swisscon.api.company;

import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.project.project.Project;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompany;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompanyRepository;
import com.siryus.swisscon.api.util.error.TestErrorResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompanyRemoveIT extends AbstractMvcTestBase {

    private static final String PATH = BASE_PATH + "/companies/";

    private final UserRepository userRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final RoleRepository roleRepository;
    private final ProjectCompanyRepository projectCompanyRepository;
    private final CompanyRepository companyRepository;

    private PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private static String ownerEmail;
    private static String ownerPassword;

    private static Company testCompany;
    private static User testUserOwner;
    private static User testUserWorker;
    private static CompanyUserRole testCompanyUserRoleOwner;
    private static CompanyUserRole testCompanyUserRoleWorker;
    private static ProjectCompany testProjectCompany;

    @Autowired
    public CompanyRemoveIT(UserRepository userRepository, CompanyUserRoleRepository companyUserRoleRepository, RoleRepository roleRepository, ProjectCompanyRepository projectCompanyRepository, CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.roleRepository = roleRepository;
        this.projectCompanyRepository = projectCompanyRepository;
        this.companyRepository = companyRepository;
    }

    @Test
    public void testDeleteUserWithoutAnyProject() {
        RequestSpecification loginSpec = loginSpec(ownerEmail, ownerPassword);
        final String path = PATH + testCompany.getId() + "/remove-company";

        given()
                .spec(defaultSpec())
                .post(path)
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.UNAUTHORIZED.value()));

        given()
                .spec(loginSpec)
                .post(path)
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.OK.value()));

        // owner should be disabled
        Optional<User> ownerOpt = userRepository.findById(Objects.requireNonNull(testUserOwner.getId()));
        assert ownerOpt.isPresent();
        assertNotNull(ownerOpt.get().getDisabled());

        // worker should be disabled
        Optional<User> workerOpt = userRepository.findById(Objects.requireNonNull(testUserWorker.getId()));
        assert workerOpt.isPresent();
        assertNotNull(workerOpt.get().getDisabled());

        // company user role should be deleted
        assertFalse(companyUserRoleRepository.existsById(testCompanyUserRoleOwner.getId()));
        assertFalse(companyUserRoleRepository.existsById(testCompanyUserRoleWorker.getId()));

        // company should be disabled
        Optional<Company> companyOpt = companyRepository.findById(testCompany.getId());
        assert companyOpt.isPresent();
        assertNotNull(companyOpt.get().getDisabled());
    }

    @Test
    public void testDeleteCompanyAssignedToProject() {
        // we assign the company to a project > you cannot delete the company
        testProjectCompany = createTestProjectCompany();

        RequestSpecification loginSpec = loginSpec(ownerEmail, ownerPassword);
        TestErrorResponse error = given()
                .spec(loginSpec)
                .post(PATH + testCompany.getId() + "/remove-company")
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.CONFLICT.value()))
                .extract()
                .as(TestErrorResponse.class);

        assertNotNull(error.getReason());
        assertTrue(error.getReason().contains("Company cannot be removed as long as it is assigned to projects"));
    }

    @BeforeEach
    public void initTestData() {
        ownerEmail = RandomStringUtils.randomAlphabetic(10) + "@test.com";
        ownerPassword = RandomStringUtils.randomAlphabetic(10);

        testCompany = createTestCompany();
        testUserOwner = createTestUser(ownerEmail, ownerPassword);
        testUserWorker = createTestUser();
        testCompanyUserRoleOwner = createCompanyUserRole(testUserOwner, RoleName.COMPANY_OWNER);
        testCompanyUserRoleWorker = createCompanyUserRole(testUserWorker, RoleName.COMPANY_WORKER);
    }

    private ProjectCompany createTestProjectCompany() {
        Project project = Project.builder().build();
        project.setId(1);

        ProjectCompany projectCompany = ProjectCompany.builder()
                .project(project)
                .company(testCompany)
                .build();
        return projectCompanyRepository.save(projectCompany);
    }

    private Company createTestCompany() {
        Company testCompany = Company.builder()
                .name("test company")
                .build();
        return companyRepository.save(testCompany);
    }

    private User createTestUser() {
        return createTestUser(null, null);
    }

    private User createTestUser(String email, String password) {
        String realMail = email == null ? RandomStringUtils.randomAlphabetic(10) + "@test.com" : email;
        String realPassword = password == null ? RandomStringUtils.randomAlphabetic(10) : password;
        User tempUser = User.builder().username(email).build();
        tempUser.setEmail(realMail);
        tempUser.setPassword(passwordEncoder.encode(realPassword));
        return userRepository.save(tempUser);
    }

    private CompanyUserRole createCompanyUserRole(User user, RoleName roleName) {
        CompanyUserRole companyUserRole = CompanyUserRole.builder()
                .role(roleRepository.getRoleByName(roleName.toString()))
                .user(user)
                .company(testCompany)
                .build();
        return companyUserRoleRepository.save(companyUserRole);
    }

    @AfterEach
    public void cleanTestData() {
        Optional.ofNullable(testCompanyUserRoleOwner).ifPresent(e -> deleteIfExists(companyUserRoleRepository, e.getId()));
        Optional.ofNullable(testCompanyUserRoleWorker).ifPresent(e -> deleteIfExists(companyUserRoleRepository, e.getId()));
        Optional.ofNullable(testProjectCompany).ifPresent(e -> deleteIfExists(projectCompanyRepository, e.getId()));
        Optional.ofNullable(testCompany).ifPresent(e -> deleteIfExists(companyRepository, e.getId()));
        Optional.ofNullable(testUserOwner).ifPresent(e -> deleteIfExists(userRepository, e.getId()));
        Optional.ofNullable(testUserWorker).ifPresent(e -> deleteIfExists(userRepository, e.getId()));
        testCompany = null;
        testUserOwner = null;
        testUserWorker = null;
        testCompanyUserRoleOwner = null;
        testCompanyUserRoleWorker = null;
        testProjectCompany = null;
        ownerEmail = null;
        ownerPassword = null;
    }

}
