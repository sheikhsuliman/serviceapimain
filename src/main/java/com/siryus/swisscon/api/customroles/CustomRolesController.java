package com.siryus.swisscon.api.customroles;


import com.siryus.swisscon.api.customroles.dto.CustomPermissionDTO;
import com.siryus.swisscon.api.customroles.dto.CustomRoleDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rest/custom-roles")
@Api(tags = {"Auth:customRoles"})
class CustomRolesController {

    private final CustomRoleService roleService;

    @Autowired
    CustomRolesController(CustomRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("roles")
    @ApiOperation(
        value = "List all roles defined in the system",
        tags = "Auth:customRoles"
    )
    @PreAuthorize("hasPermission(0, 'COMPANY', 'ROLES_UPDATE')")
    List<CustomRoleDTO> listRoles() {
        return roleService.listRoles();
    }

    @GetMapping("permissions")
    @ApiOperation(
            value = "List all permissions defined in the system",
            tags = "Auth:customRoles"
    )
    @PreAuthorize("hasPermission(0, 'COMPANY', 'ROLES_UPDATE')")
    List<CustomPermissionDTO> listPermissions() {
        return roleService.listPermissions();
    }

    @GetMapping("roles/{roleName}")
    @ApiOperation(
            value = "Get a role by role name",
            tags = "Auth:customRoles"
    )
    @PreAuthorize("hasPermission(0, 'COMPANY', 'ROLES_UPDATE')")
    CustomRoleDTO getRole( @PathVariable String roleName) {
        return roleService.getRoleByName(roleName);
    }

    @GetMapping("role/{roleId}")
    @ApiOperation(
            value = "Get a role by role id",
            tags = "Auth:customRoles"
    )
    @PreAuthorize("hasPermission(0, 'COMPANY', 'ROLES_UPDATE')")
    CustomRoleDTO getRole( @PathVariable Integer roleId) {
        return roleService.getRoleById(roleId);
    }

    @PostMapping("roles/new")
    @ApiOperation(
            value = "Create new role",
            tags = "Auth:customRoles"
    )
    @PreAuthorize("hasPermission(0, 'COMPANY', 'ROLES_UPDATE')")
    CustomRoleDTO createNewRole(@RequestBody CustomRoleDTO newRole) {
        return roleService.createNewRole(newRole);
    }

    @PostMapping("roles/update")
    @ApiOperation(
            value = "Update existing role",
            tags = "Auth:customRoles"
    )
    @PreAuthorize("hasPermission(0, 'COMPANY', 'ROLES_UPDATE')")
    CustomRoleDTO updateRole(@RequestBody CustomRoleDTO role) {
        return roleService.updateRole(role);
    }

    @GetMapping("user/{userId}/company/{companyId}/roles")
    @ApiOperation(
            value = "Retrieve roles given user has in given company",
            tags = "Auth:customRoles"
    )
    @PreAuthorize("hasPermission(#companyId, 'COMPANY', 'COMPANY_ROLES_ASSIGN')")
    List<CustomRoleDTO> getUserCompanyRoles(
            @PathVariable Integer userId,
            @PathVariable Integer companyId
    ) {
        return roleService.getUserCompanyRoles(userId, companyId);
    }

    @GetMapping("user/{userId}/project/{projectId}/roles")
    @ApiOperation(
            value = "Retrieve roles given user has in given project",
            tags = "Auth:customRoles"
    )
    @PreAuthorize("hasPermission(#projectId, 'PROJECT', 'PROJECT_ROLES_ASSIGN')")
    List<CustomRoleDTO> getUserProjectRoles(
            @PathVariable Integer userId,
            @PathVariable Integer projectId
    ) {
        return roleService.getUserProjectRoles(userId, projectId);
    }

    @PostMapping("user/{userId}/company/{companyId}/roles")
    @ApiOperation(
            value = "Set roles for given member in given company",
            tags = "Auth:customRoles"
    )
    @PreAuthorize("hasPermission(#companyId, 'COMPANY', 'COMPANY_ROLES_ASSIGN')")
    List<CustomRoleDTO> setUserCompanyRoles(
            @PathVariable Integer userId,
            @PathVariable Integer companyId,
            @RequestBody List<CustomRoleDTO> roles
    ) {
        return roleService.setUserCompanyRoles(userId, companyId, roles);
    }

    @PostMapping("user/{userId}/project/{projectId}/roles")
    @ApiOperation(
            value = "Set roles for given worker in given project",
            tags = "Auth:customRoles"
    )
    @PreAuthorize("hasPermission(#projectId, 'PROJECT', 'PROJECT_ROLES_ASSIGN')")
    List<CustomRoleDTO> setUserProjectRoles(
            @PathVariable Integer userId,
            @PathVariable Integer projectId,
            @RequestBody List<CustomRoleDTO> roles
    ) {
        return roleService.setUserProjectRoles(userId, projectId, roles);
    }
}
