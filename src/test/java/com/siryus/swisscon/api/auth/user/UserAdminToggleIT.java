package com.siryus.swisscon.api.auth.user;

import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleRepository;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserAdminToggleIT extends AbstractMvcTestBase {

    private final UserRepository userRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;

    private static User testUser;
    private static CompanyUserRole testCompanyUserRole;
    private static ProjectUserRole testProjectUserRole;

    @Autowired
    public UserAdminToggleIT(UserRepository userRepository, CompanyUserRoleRepository companyUserRoleRepository, ProjectUserRoleRepository projectUserRoleRepository, CompanyRepository companyRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.projectUserRoleRepository = projectUserRoleRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
    }

    @Test
    public void testToggleAdminTrue() {
        createTestCompanyUserRole(RoleName.COMPANY_WORKER);

        TeamUserDTO teamUserDTO = given()
                .spec(loginSpec())
                .contentType(ContentType.JSON)
                .queryParam("make-admin", true)
                .post(BASE_PATH + "/auth/users/" + testUser.getId() + "/toggle-admin")
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.OK.value()))
                .extract().as(TeamUserDTO.class);

        assertTrue(teamUserDTO.getIsAdmin());
        assertEquals(testUser.getId(), teamUserDTO.getId());

        CompanyUserRole savedCompanyUserRole = companyUserRoleRepository.findByUser(testUser.getId()).get(0);
        assertEquals(RoleName.COMPANY_ADMIN.toString(), savedCompanyUserRole.getRole().getName());
    }


    @Test
    public void testToggleAdminFalse() {
        createTestCompanyUserRole(RoleName.COMPANY_ADMIN);

        TeamUserDTO teamUserDTO = given()
                .spec(loginSpec())
                .contentType(ContentType.JSON)
                .queryParam("make-admin", false)
                .post(BASE_PATH + "/auth/users/" + testUser.getId() + "/toggle-admin")
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.OK.value()))
                .extract().as(TeamUserDTO.class);

        assertFalse(teamUserDTO.getIsAdmin());
        assertEquals(testUser.getId(), teamUserDTO.getId());

        List<CompanyUserRole> savedCompanyUserRole = companyUserRoleRepository.findByUser(testUser.getId());
        assertTrue(savedCompanyUserRole.stream().anyMatch(cur->cur.getRole().getName().equals(RoleName.COMPANY_WORKER.name())));
    }

    @Test
    public void testToggleAdminFalseWithOwner() {
        createTestCompanyUserRole(RoleName.COMPANY_OWNER);

        TeamUserDTO teamUserDTO = given()
                .spec(loginSpec())
                .contentType(ContentType.JSON)
                .queryParam("make-admin", false)
                .post(BASE_PATH + "/auth/users/" + testUser.getId() + "/toggle-admin")
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.OK.value()))
                .extract().as(TeamUserDTO.class);

        assertTrue(teamUserDTO.getIsAdmin());
        assertEquals(testUser.getId(), teamUserDTO.getId());

        List<CompanyUserRole> savedCompanyUserRole = companyUserRoleRepository.findByUser(testUser.getId());
        assertTrue(savedCompanyUserRole.stream().anyMatch(cur->cur.getRole().getName().equals(RoleName.COMPANY_OWNER.name())));
    }


    @BeforeEach
    public void initTestData() {
        // create user
        String mail = "test@" + RandomStringUtils.randomAlphanumeric(5) + ".com";
        User user = User.builder().username(mail).build();
        user.setPassword("{noop}" + RandomStringUtils.randomAlphanumeric(5));
        user.setEmail(mail);
        testUser = userRepository.save(user);
    }

    private void createTestCompanyUserRole(RoleName roleName) {
        Optional<Company> companyOpt = companyRepository.findById(1);
        assert companyOpt.isPresent();
        Role roleWorker = roleRepository.getRoleByName(roleName.toString());
        CompanyUserRole companyUserRole = CompanyUserRole.builder().role(roleWorker).company(companyOpt.get()).user(testUser).build();
        testCompanyUserRole = companyUserRoleRepository.save(companyUserRole);
    }

    @AfterEach
    public void cleanTestData() {
        Optional.ofNullable(testCompanyUserRole).ifPresent(cur -> deleteIfExists(companyUserRoleRepository, cur.getId()));
        Optional.ofNullable(testProjectUserRole).ifPresent(pur -> deleteIfExists(projectUserRoleRepository, pur.getId()));
        Optional.ofNullable(testUser).ifPresent(u -> deleteIfExists(userRepository, u.getId()));

        testCompanyUserRole = null;
        testProjectUserRole = null;
        testUser = null;
    }
}
