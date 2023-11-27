package com.siryus.swisscon.api.auth.permission;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class PermissionNameTest extends AbstractMvcTestBase {

    @Autowired
    PermissionRepository permissionRepository;

    @Test
    public void Given_validEnum_When_getPermissionByName_Then_success() {
        for(PermissionName permissionName : PermissionName.values()) {
           assertNotNull(permissionRepository.getPermissionByName(permissionName.name()), "Permission is not in DB : " + permissionName);
        }
    }

    @Test
    public void Given_permissionFromTable_When_enum_Then_success() {
        for(Permission permission : permissionRepository.findAll()) {
            PermissionName.valueOf(permission.getName());
        }
    }
}