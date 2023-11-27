package com.siryus.swisscon.api.auth.role;

import com.siryus.swisscon.api.auth.permission.Permission;
import com.siryus.swisscon.api.auth.permission.PermissionName;
import com.siryus.swisscon.api.auth.permission.PermissionRepository;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoleNameTest extends AbstractMvcTestBase {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PermissionRepository permissionRepository;

    @Test
    public void Given_validEnum_When_getRoleByName_Then_success() {
        for(RoleName roleName : RoleName.values()) {
            assertNotNull(roleRepository.getRoleByName(roleName.name()), "Role is not in DB : " + roleName);
        }
    }

    @Test
    public void Given_roleFromTable_When_enum_Then_success() {
        for(Role role : roleRepository.findAll()) {
            RoleName.valueOf(role.getName());
        }
    }

    @Test
    public void Given_roleHasPermission_Then_thisPermissionIsNotDeprecated() throws NoSuchFieldException {
        for(Role role : roleRepository.findAll()) {
            for(Permission permission : permissionRepository.getPermissionsByRoleNames(Collections.singleton(role.getName()))) {
                assertFalse(isPermissionDeprecated(permission.getName()), "Role " + role.getName() + " use deprecated permission " + permission.getName());
            }
        }
    }

    boolean isPermissionDeprecated(String permissionName) throws NoSuchFieldException {
        return PermissionName.class.getField(permissionName).isAnnotationPresent(Deprecated.class);
    }
}
