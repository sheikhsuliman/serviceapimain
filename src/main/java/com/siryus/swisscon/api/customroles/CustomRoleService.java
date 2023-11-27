package com.siryus.swisscon.api.customroles;

import com.siryus.swisscon.api.auth.permission.PermissionRepository;
import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.customroles.dto.CustomPermissionDTO;
import com.siryus.swisscon.api.customroles.dto.CustomRoleDTO;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompany;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompanyRepository;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleRepository;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.api.util.validator.Reference;
import com.siryus.swisscon.soa.EventsEmitter;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
class CustomRoleService implements CustomRoleReader {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final ProjectCompanyRepository projectCompanyRepository;
    private final EventsEmitter eventsEmitter;
    private final SecurityHelper securityHelper;

    @Autowired
    CustomRoleService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            CompanyUserRoleRepository companyUserRoleRepository,
            ProjectUserRoleRepository projectUserRoleRepository,
            ProjectCompanyRepository projectCompanyRepository,
            EventsEmitter eventsEmitter, SecurityHelper securityHelper) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.projectUserRoleRepository = projectUserRoleRepository;
        this.projectCompanyRepository = projectCompanyRepository;
        this.eventsEmitter = eventsEmitter;
        this.securityHelper = securityHelper;
    }

    @Override
    public List<CustomRoleDTO> listRoles() {
        return roleRepository.findAll().stream()
                .filter( r -> ! r.isDeprecated())
                .filter(r->!RoleName.COMPANY_BOOTSTRAP.name().equals(r.getName()))
                .map(CustomRoleDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomPermissionDTO> listPermissions() {
        return permissionRepository.findAll().stream()
                .filter( p -> ! p.isDeprecated())
                .map(CustomPermissionDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public CustomRoleDTO getRoleByName(String roleName) {
        return roleRepository.findRoleByName(roleName)
                .map(CustomRoleDTO::from)
                .orElseThrow(() -> CustomRoleExceptions.roleDoesNotExist(roleName));
    }

    public CustomRoleDTO getRoleById(Integer roleId) {
        return roleRepository.findById(roleId)
                .map(CustomRoleDTO::from)
                .orElseThrow(() -> CustomRoleExceptions.roleDoesNotExist(roleId));
    }

    @Transactional
    public CustomRoleDTO createNewRole(@Valid CustomRoleDTO role) {
        validateNewRole(role);

        return materializeRole(role);
    }

    @Transactional
    public CustomRoleDTO updateRole(@Valid CustomRoleDTO role) {
        validateExistingRoleCanBeUpdated(role);

        return updateExistingRole(role);
    }

    List<CustomRoleDTO> getUserCompanyRoles(
            @NotNull
            @Reference(ReferenceType.USER)
            Integer userId,

            @NotNull
            @Reference(ReferenceType.COMPANY)
            Integer companyId
    ) {
        return companyUserRoleRepository.findByUserAndCompany(userId, companyId).stream()
                .map(CompanyUserRole::getRole)
                .map(CustomRoleDTO::from)
                .collect(Collectors.toList());
    }

    List<CustomRoleDTO> getUserProjectRoles(
            @NotNull
            @Reference(ReferenceType.USER)
            Integer userId,

            @NotNull
            @Reference(ReferenceType.PROJECT)
            Integer projectId
    ) {
        return projectUserRoleRepository.findByUserAndProject(userId, projectId).stream()
                .map(ProjectUserRole::getRole)
                .map(CustomRoleDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<CustomRoleDTO> setUserCompanyRoles(
            @NotNull
            @Reference(ReferenceType.USER)
            Integer userId,

            @NotNull
            @Reference(ReferenceType.COMPANY)
            Integer companyId,

            List<CustomRoleDTO> roles
    ) {
        List<Role> validRoles = validateRolesAre(roles, false);
        List<CustomRoleDTO> userRoles = this.getUserCompanyRoles(userId, companyId);

        if (evaluateRolesChange(userRoles, validRoles)) {
            companyUserRoleRepository.deleteAllUserPermissionsForCompany(userId, companyId);
            companyUserRoleRepository.saveUserRolesForCompany(
                    userId, companyId,
                    validRoles.stream().map(Role::getId).collect(Collectors.toList())
            );
        }

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .companyId(companyId)
                .notificationType(NotificationType.COMPANY_USER_ROLE_UPDATED)
                .referenceId(companyId)
                .subjectId(userId)
                .senderId(securityHelper.currentUserId())
                .build());

        return validRoles.stream().map(CustomRoleDTO::from).collect(Collectors.toList());
    }

    @Transactional
    public List<CustomRoleDTO> setUserProjectRoles(
            @NotNull
            @Reference(ReferenceType.USER)
            Integer userId,

            @NotNull
            @Reference(ReferenceType.PROJECT)
            Integer projectId,

            List<CustomRoleDTO> roles
    ) {
        List<Role> validRoles = validateRolesAre(roles, true);
        final List<Integer> validRoleIds = validRoles.stream()
                .map(Role::getId)
                .collect(Collectors.toList());

        final Integer companyId = calculateUserCompanyId(userId);
        final Integer projectCompanyId = calculateProjectCompanyId(userId, companyId, projectId);

        projectUserRoleRepository.deleteAllUserRolesForProject(userId, projectId, projectCompanyId);
        projectUserRoleRepository.saveUserRolesForProject(
                userId, projectId, projectCompanyId,
                validRoleIds
        );

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .companyId(companyId)
                .projectId(projectId)
                .notificationType(NotificationType.PROJECT_USER_ROLE_UPDATED)
                .referenceId(projectId)
                .subjectId(userId)
                .senderId(securityHelper.currentUserId())
                .build());

        return validRoles.stream().map(CustomRoleDTO::from).collect(Collectors.toList());
    }

    private Integer calculateUserCompanyId(Integer userId) {
        List<CompanyUserRole> companyRoles = companyUserRoleRepository.findByUser(userId);

        if (companyRoles.isEmpty()) {
            throw CustomRoleExceptions.userIsNotMemberOfAnyCompany(userId);
        }
        if (companyRoles.stream().map( cur -> cur.getCompany().getId() ).distinct().count() > 1 ) {
            throw CustomRoleExceptions.userIsMemberOfMoreThanOneCompany(userId);
        }
        return companyRoles.get(0).getCompany().getId();
    }

    private Integer calculateProjectCompanyId(Integer userId, Integer userCompanyId, Integer projectId) {
        return Optional.ofNullable(projectCompanyRepository.findByProjectAndCompany(projectId, userCompanyId))
                .map(ProjectCompany::getId)
                .orElseThrow(() -> CustomRoleExceptions.userCompanyIsNotPartOfTheProject(userId, userCompanyId, projectId));
    }

    private void validateNewRole(CustomRoleDTO role) {
        validateRoleHasUniqueName(role.getName());
        validatePermissions(role.getPermissions());
    }

    private void validateExistingRoleCanBeUpdated(CustomRoleDTO role) {
        if (role.getId() == null) {
            throw CustomRoleExceptions.roleHasToHaveId();
        }
        if (role.isSystemRole()) {
            throw CustomRoleExceptions.canNotMutateSystemRole(role.getName());
        }
        validateRoleHasUniqueName(role.getName(), role.getId());
        validatePermissions(role.getPermissions());
    }

    private void validateRoleHasUniqueName(String name) {
        if (roleRepository.findRoleByName(name).isPresent()) {
            throw CustomRoleExceptions.roleWithNameAlreadyExists(name);
        }
    }

    private void validateRoleHasUniqueName(String name, Integer roleId) {
        if (roleRepository.findRoleByName(name).filter(r -> ! r.getId().equals(roleId)).isPresent()) {
            throw CustomRoleExceptions.roleWithNameAlreadyExists(name);
        }
    }

    private void validatePermissions(List<CustomPermissionDTO> permissions) {
        if (permissions.isEmpty()) {
            throw CustomRoleExceptions.roleHasToHaveAtLeastOnePermission();
        }
        permissions.forEach(
            p -> {
                if (! permissionRepository.existsById(p.getId())) {
                    throw CustomRoleExceptions.permissionDoesNotExist(p.getName(), p.getId());
                }
            }
        );
    }

    private List<Role> validateRolesAre(List<CustomRoleDTO> roles, boolean projectRoles) {
        validateRolesNotEmpty(roles, projectRoles);

        return validateAllRolesExist(roles).stream()
                .map(r -> validateRoleIsOfRightKind(r, projectRoles))
                .collect(Collectors.toList());
    }

    private void validateRolesNotEmpty(List<CustomRoleDTO> roles, boolean projectRoles) {
        if (roles.isEmpty()) {
            throw projectRoles ? CustomRoleExceptions.canNotUnAssignAllProjectRoles() : CustomRoleExceptions.canNotUnAssignAllCompanyRoles();
        }
    }

    private Role validateRoleIsOfRightKind(Role role, boolean projectRole) {
        if ( role.isProjectRole() ^ projectRole ) {
            throw projectRole ? CustomRoleExceptions.roleIsNotProjectRole(role.getName()) : CustomRoleExceptions.roleIsNotCompanyRole(role.getName());
        }
        return role;
    }

    private List<Role> validateAllRolesExist(List<CustomRoleDTO> roles) {
        return roles.stream()
                .map(
                    dto -> roleRepository.findById(dto.getId())
                        .orElseThrow(() -> CustomRoleExceptions.roleDoesNotExist(dto.getName()))
                )
                .collect(Collectors.toList());
    }

    private boolean evaluateRolesChange(List<CustomRoleDTO> userRoles, List<Role> requestRoles) {
        if (same(userRoles, requestRoles)) {
            return false;
        }

        var uniqAndMandatoryUserRoles = userRoles.stream()
                .filter(r -> RoleName.isUniqAndMandatory(r.getName()))
                .map(CustomRoleDTO::getName)
                .collect(Collectors.toSet());

        var uniqAndMandatoryRequestRoles = requestRoles.stream()
                .filter(r -> RoleName.isUniqAndMandatory(r.getName()))
                .map(Role::getName)
                .collect(Collectors.toSet());

        if ( ! uniqAndMandatoryRequestRoles.equals(uniqAndMandatoryUserRoles)) {
            throw CustomRoleExceptions.canNotReassignUniqAndMandatoryRoles();
        }

        return true;
    }

    private boolean same(List<CustomRoleDTO> userRoles, List<Role> requestRoles) {
        var userRolesSet = userRoles.stream().map(CustomRoleDTO::getId).collect(Collectors.toCollection(HashSet::new));
        var requestRolesSet = requestRoles.stream().map(Role::getId).collect(Collectors.toCollection(HashSet::new));

        return userRolesSet.equals(requestRolesSet);
    }

    private CustomRoleDTO materializeRole(CustomRoleDTO role) {
        clearDefaultsIfNeeded(role);

        Role savedRole = roleRepository.save(
            new Role(
                null,
                role.getName(),
                role.getDescription(),

                role.isDeprecated(),
                role.isMemberDefault(),
                role.isCompanyDefault(),
                role.isProjectRole(),
                false,
                Collections.emptyList()
            )
        );

        materializeRolePermissions(savedRole.getId(), role.getPermissions());

        return role.toBuilder().id(savedRole.getId()).build();
    }

    private CustomRoleDTO updateExistingRole(CustomRoleDTO role) {
        clearDefaultsIfNeeded(role);

        Role existingRole = roleRepository.findById(role.getId())
                .orElseThrow(() -> CustomRoleExceptions.roleDoesNotExist(role.getId()));

        updateRoleInstance(existingRole, role);

        materializeRolePermissions(existingRole.getId(), role.getPermissions());

        return role;
    }

    private void updateRoleInstance(Role existingRole, CustomRoleDTO role) {
        existingRole.setName(role.getName());
        existingRole.setDescription(role.getDescription());

        existingRole.setDeprecated(role.isDeprecated());
        existingRole.setMemberDefault(role.isMemberDefault());
        existingRole.setOwnerDefault(role.isCompanyDefault());
        existingRole.setProjectRole(role.isProjectRole());
    }

    private void clearDefaultsIfNeeded(CustomRoleDTO role) {
        if (role.isMemberDefault()) {
            roleRepository.clearMemberDefault();
        }
        if (role.isCompanyDefault()) {
            roleRepository.clearCompanyOwnerDefault();
        }
    }

    private void materializeRolePermissions(Integer roleId, List<CustomPermissionDTO> permissions) {
        permissionRepository.deleteAllRolePermissions(roleId);
        permissionRepository.saveRolePermissions(
                roleId,
                permissions.stream().map(CustomPermissionDTO::getId).collect(Collectors.toList())
        );
    }
}
