package com.siryus.swisscon.api.project.project;

import com.siryus.swisscon.api.auth.permission.Permission;
import com.siryus.swisscon.api.auth.permission.PermissionRepository;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.base.TestHelper;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.siryus.swisscon.api.base.TestHelper.ANOTHER_COMPANY;
import static com.siryus.swisscon.api.base.TestHelper.ANOTHER_PROJECT_OWNER_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.ANOTHER_PROJECT_OWNER_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.ANOTHER_PROJECT_OWNER_LAST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_EMAIL;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectPermissionsIT extends AbstractMvcTestBase {


    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    private static TestHelper.TestProject testProject;
    private static RequestSpecification asProjectOwner;
    private static RequestSpecification anotherProjectManager;

    @Autowired
    public ProjectPermissionsIT(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }


    @BeforeAll
    public void initTest() {
        testProject = testHelper.createProject();
        asProjectOwner = testHelper.login(PROJECT_OWNER_EMAIL);

        testHelper
                .signUp(TestBuilder.testSignupDTO(ANOTHER_COMPANY,
                        ANOTHER_PROJECT_OWNER_FIRST_NAME,
                        ANOTHER_PROJECT_OWNER_LAST_NAME));
        anotherProjectManager = testHelper.login(ANOTHER_PROJECT_OWNER_EMAIL);
    }

    @Test
    public void Given_testProjectOwner_When_getProjectPermissions_Then_getCorrectPermissions() {
        List<Integer> permissionIds = testHelper.getProjectPermissions(asProjectOwner, testProject.ownerCompany.projectId);
        Integer projectOwnerRoleId = roleRepository.getRoleByName(RoleName.PROJECT_OWNER.toString()).getId();

        List<Permission> rolePermissions = permissionRepository.findPermissionsByRole(projectOwnerRoleId);

        assertFalse(permissionIds.isEmpty());
        assertFalse(rolePermissions.isEmpty());
        rolePermissions.forEach(rp -> assertTrue(permissionIds.contains(rp.getId())));
    }

    @Test
    public void Given_nonExistingProject_When_getProjectPermissions_Then_throwNotFound() {
        testHelper.getProjectPermissions(asProjectOwner, -15,
                r -> {
                    r.assertThat().statusCode(HttpStatus.NOT_FOUND.value());
                    return null;
                });
    }

    @Test
    public void Given_userWhichIsNotPartOfProject_When_getProjectPermissions_Then_emptyPermissionList() {
        List<Integer> permissionIds = testHelper.getProjectPermissions(anotherProjectManager, testProject.ownerCompany.projectId);
        assertTrue(permissionIds.isEmpty());
    }

}
