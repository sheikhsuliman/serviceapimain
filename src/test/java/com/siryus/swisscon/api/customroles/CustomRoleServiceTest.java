package com.siryus.swisscon.api.customroles;

import com.siryus.swisscon.api.auth.permission.PermissionRepository;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.customroles.dto.CustomPermissionDTO;
import com.siryus.swisscon.api.customroles.dto.CustomRoleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static com.siryus.swisscon.api.base.TestBuilder.testCustomPermissions;
import static com.siryus.swisscon.api.base.TestBuilder.testCustomRole;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomRoleServiceTest extends AbstractMvcTestBase {

    public static final String NEW_ROLE_NAME = "NEW_ROLE";

    public static final String NEW_ROLE1_NAME = "NEW_ROLE1";

    private RoleRepository roleRepository;
    @Autowired
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    private PermissionRepository permissionRepository;

    @Autowired
    public void setPermissionRepository(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    private TestHelper.TestProjectOwnerCompany company;

    @BeforeEach
    void doBeforeEach() {
        cleanDatabase();
        company = testHelper.createProjectOwnerCompany();
    }

    @Test
    void Given_defaultConfiguration_When_listRoles_Then_returnDefaultRoleList() {

        CustomRoleDTO[] roles = testHelper.getRoles(company.asOwner);

        assertNotNull(roles);
        assertTrue(roles.length > 0);

        assertNotNull(roles[0].getPermissions());
        assertFalse(roles[0].getPermissions().isEmpty());
    }

    @Test
    void Given_defaultConfiguration_When_listPermissions_Then_returnDefaultPermissions() {
        CustomPermissionDTO[] permissions = testHelper.getPermissions(company.asOwner);

        assertNotNull(permissions);
        assertTrue(permissions.length > 0);
    }

    @Test
    void Given_correctRequest_When_newRole_Then_createNewRole() {
        CustomRoleDTO newRole = testHelper.createNewRole(
                company.asOwner,
                testCustomRole(NEW_ROLE_NAME, testCustomPermissions(1, 2, 3))
        );

        assertNotNull(newRole);
        assertNotNull(newRole.getId());
        assertEquals(3, newRole.getPermissions().size());

        CustomRoleDTO retrievedRole = testHelper.getRole(company.asOwner, NEW_ROLE_NAME);
        assertNotNull(newRole);
        assertEquals(newRole.getId(), retrievedRole.getId());
        assertEquals(3, retrievedRole.getPermissions().size());
    }

    @Test
    void Given_notAdmin_When_newRole_Then_notAuthorized() {
        TestHelper.TestProjectContractorCompany contractorCompany = testHelper.createProjectContractorCompany();

        testHelper.createNewRole(
                contractorCompany.asWorker,
                testCustomRole(NEW_ROLE_NAME, testCustomPermissions(1, 2, 3)),
                r -> { r.assertThat().statusCode(HttpStatus.FORBIDDEN.value()); return  null; }

        );
    }

    @Test
    void Given_correctRequest_When_updateRole_Then_updateRole() {
        CustomRoleDTO newRole = testHelper.createNewRole(
                company.asOwner,
                testCustomRole(NEW_ROLE1_NAME, testCustomPermissions(1, 2, 3))
        );
        CustomRoleDTO retrievedNewRole = testHelper.getRole(company.asOwner, newRole.getId());
        assertNotNull(retrievedNewRole);
        assertEquals(NEW_ROLE1_NAME, retrievedNewRole.getName());

        CustomRoleDTO updatedRole = testHelper.updateRole(
                company.asOwner,
                testCustomRole(
                        NEW_ROLE_NAME,
                        testCustomPermissions(1, 2, 3, 4),

                        r -> r.toBuilder().id(newRole.getId()).projectRole(true).build()
                )
        );

        assertNotNull(updatedRole);
        assertEquals(newRole.getId(), updatedRole.getId());
        assertEquals(4, updatedRole.getPermissions().size());

        CustomRoleDTO retrievedRole = testHelper.getRole(company.asOwner, newRole.getId());
        assertNotNull(newRole);
        assertEquals(NEW_ROLE_NAME, retrievedRole.getName());
        assertTrue(retrievedRole.isProjectRole());
        assertEquals(4, retrievedRole.getPermissions().size());
    }

}