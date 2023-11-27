package com.siryus.swisscon.api.customroles;

import com.siryus.swisscon.api.customroles.dto.CustomPermissionDTO;
import com.siryus.swisscon.api.customroles.dto.CustomRoleDTO;

import java.util.List;

public interface CustomRoleReader {
    List<CustomRoleDTO> listRoles();
    List<CustomPermissionDTO> listPermissions();

    CustomRoleDTO getRoleByName(String roleName);
    CustomRoleDTO getRoleById(Integer roleId);
}
