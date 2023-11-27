package com.siryus.swisscon.api.company;

import com.siryus.swisscon.api.auth.permission.Permission;
import com.siryus.swisscon.api.auth.permission.PermissionRepository;
import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompanyPermissionsIT extends AbstractMvcTestBase {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final PermissionRepository permissionRepository;

    private static Company testCompany;
    private static CompanyUserRole testCompanyUserRole;
    private static Role companyOwnerRole;

    private static final String PATH = BASE_PATH + "/companies/";

    @Autowired
    public CompanyPermissionsIT(
            CompanyRepository companyRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            CompanyUserRoleRepository companyUserRoleRepository,
            PermissionRepository permissionRepository
    ) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Test
    public void testGetCompanyDirectory() {
        // execute
        Integer[] permissionIdsArr = getResponse(loginSpec(), PATH + testCompany.getId() + "/permissions")
                .as(Integer[].class);

        List<Integer> permissionIds = Arrays.asList(permissionIdsArr);

        List<Permission> rolePermissions = permissionRepository.findPermissionsByRole(companyOwnerRole.getId());

        assertFalse(permissionIds.isEmpty());
        assertFalse(rolePermissions.isEmpty());

        for(Permission rolePermission : rolePermissions) {
            assertTrue(permissionIds.contains(rolePermission.getId()));
        }
    }


    @BeforeEach
    public void initTestData() {
        testCompany = companyRepository.save(Company.builder().name("permissionTest").build());

        Optional<User> userOneOpt = userRepository.findById(1);
        assert userOneOpt.isPresent();

        companyOwnerRole = roleRepository.getRoleByName(RoleName.COMPANY_OWNER.toString());
        testCompanyUserRole = companyUserRoleRepository.save(CompanyUserRole.builder().role(companyOwnerRole).company(testCompany).user(userOneOpt.get()).build());
    }

    @AfterEach
    public void deleteTestData() {
        Optional.ofNullable(testCompanyUserRole).ifPresent(cur -> deleteIfExists(companyUserRoleRepository, cur.getId()));
        Optional.ofNullable(testCompany).ifPresent(c -> deleteIfExists(companyRepository, c.getId()));
        testCompanyUserRole = null;
        testCompany = null;
        companyOwnerRole = null;
    }
}
