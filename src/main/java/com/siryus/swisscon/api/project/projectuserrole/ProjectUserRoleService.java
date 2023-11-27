package com.siryus.swisscon.api.project.projectuserrole;

import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserService;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleService;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.project.ProjectException;
import com.siryus.swisscon.api.project.project.ProjectReader;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompany;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompanyRepository;
import com.siryus.swisscon.api.tasks.entity.SubTaskUserEntity;
import com.siryus.swisscon.api.tasks.repos.SubTaskUserRepository;
import com.siryus.swisscon.api.util.ValidationUtils;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.api.util.validator.Reference;
import com.siryus.swisscon.soa.EventsEmitter;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class ProjectUserRoleService {

    private final ProjectReader projectReader;
    private final UserService userService;

    private final CompanyUserRoleService companyUserRoleService;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final ProjectCompanyRepository projectCompanyRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final RoleRepository roleRepository;
    private final SubTaskUserRepository subTaskUserRepository;
    private final SecurityHelper securityHelper;
    private final EventsEmitter eventsEmitter;

    @Autowired
    public ProjectUserRoleService(
            ProjectReader projectReader,
            UserService userService, CompanyUserRoleService companyUserRoleService,
            ProjectUserRoleRepository projectUserRoleRepository,
            ProjectCompanyRepository projectCompanyRepository,
            CompanyUserRoleRepository companyUserRoleRepository,
            RoleRepository roleRepository,
            SubTaskUserRepository subTaskUserRepository,
            SecurityHelper securityHelper, EventsEmitter eventsEmitter) {
        this.projectReader = projectReader;
        this.userService = userService;
        this.companyUserRoleService = companyUserRoleService;
        this.projectUserRoleRepository = projectUserRoleRepository;
        this.projectCompanyRepository = projectCompanyRepository;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.roleRepository = roleRepository;
        this.subTaskUserRepository = subTaskUserRepository;
        this.securityHelper = securityHelper;
        this.eventsEmitter = eventsEmitter;
    }

    public List<TeamUserDTO> getProjectTeamByCompany(@Reference(ReferenceType.PROJECT)Integer projectId,
                                                         @Reference(ReferenceType.COMPANY)Integer companyId) {
        ProjectCompany projectCompany = getAndValidateProjectCompany(projectId, companyId);
        List<ProjectUserRole> projectUserRoles = projectUserRoleRepository.findProjectCompanyTeam(projectCompany.getId());
        ValidationUtils.throwIfEmpty(projectUserRoles, () -> ProjectException.companyIsNotPartOfProject(companyId, projectId));

        return new ArrayList<>(projectUserRoles
                .stream()
                .map(TeamUserDTO::fromProjectUserRole)
                .collect(Collectors.toMap(TeamUserDTO::getId, zu -> zu,
                        (existing, replacement) ->
                            existing.toBuilder().roleIds(
                                    ListUtils.union(existing.getRoleIds(), replacement.getRoleIds())
                            ).build()
                )).values());
    }

    @Transactional
    public ProjectUserRole addCompanyOwnerToProject(ProjectCompany projectCompany, Integer roleId) {
        Integer companyId = projectCompany.getCompany().getId();

        User owner = companyUserRoleService.getCompanyOwner(companyId).getUser();

        Role adminRole = roleId == null ? calculateProjectManagerRole(owner.getId()) : roleRepository.findById(roleId).get();

        ProjectUserRole temporaryProjectCompanyAdmin = ProjectUserRole.builder()
                .project(projectCompany.getProject())
                .projectCompany(projectCompany)
                .role(adminRole)
                .user(owner)
                .build();

        return projectUserRoleRepository.save(temporaryProjectCompanyAdmin);
    }

    @Transactional
    @PreAuthorize("hasPermission(#projectId, 'PROJECT', 'PROJECT_TEAM_ADD_USER')")
    public ProjectUserRole addCompanyUserToProject(Integer userId, Integer projectId) {
        // add the user to the project as a worker by default
        final RoleName roleName = RoleName.PROJECT_WORKER;

        Integer useCompanyId = securityHelper.validUserCompanyId(userId);
        validateCurrentUserIsFromSameCompany(useCompanyId);

        return addCompanyUserToProject(userId, projectId, roleName, useCompanyId);
    }

    private void validateCurrentUserIsFromSameCompany(Integer useCompanyId) {
        boolean currentUserIsNotFromSameCompany = companyUserRoleRepository
                .findByUser(securityHelper.currentUserId())
                .stream()
                .noneMatch(cur -> cur.getCompany().getId().equals(useCompanyId));

        if (currentUserIsNotFromSameCompany) {
            throw ProjectException.canNotAddUserFromOtherCompanyToProject();
        }
    }

    private ProjectUserRole addCompanyUserToProject(Integer userId, Integer projectId, RoleName roleName, Integer companyId) {
        ProjectCompany projectCompany = getAndValidateProjectCompany(projectId, companyId);

        ValidationUtils.throwIfNot(projectUserRoleRepository.findByUserAndProject(userId, projectId).isEmpty(),
                ProjectException::userAlreadyPartOfProject);


        ProjectUserRole projectUserRole = projectUserRoleRepository.save(ProjectUserRole.builder()
                .user(userService.getUser(userId))
                .projectCompany(projectCompany)
                .project(projectCompany.getProject())
                .role(roleRepository.getRoleByName(roleName.toString()))
                .build());

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .companyId(companyId)
                .projectId(projectId)
                .notificationType(NotificationType.PROJECT_USER_INVITED)
                .referenceId(projectId)
                .subjectId(userId)
                .senderId(securityHelper.currentUserId())
                .build());

        return projectUserRoleRepository.save(projectUserRole);
    }


    @Transactional
    public void deleteUserFromProjectTeam(Integer projectId, Integer userId) {
        deleteUserFromProjectTeam(projectId, userId, false);
    }

    @Transactional
    public void deleteUserFromProjectTeam(Integer projectId, Integer userId, boolean isRemoveCompany) {
        projectReader.validateProjectId(projectId);
        userService.validateUserId(userId);

        List<ProjectUserRole> projectUserRoles = ValidationUtils.throwIfEmpty(
                projectUserRoleRepository.findByUserAndProject(userId, projectId),
                () -> ProjectException.userIsNotPartOfProject(userId, projectId)
        );

        validateUserHasNoTasksAssigned(projectId, userId);
        Integer userCompanyId = securityHelper.validUserCompanyId(userId);

        if(!isRemoveCompany) {
            validateCurrentUserIsFromSameCompany(userCompanyId);
            validateUserIsNot(userId, projectId, RoleName.PROJECT_OWNER, RoleName.PROJECT_MANAGER);
        }

        projectUserRoles.forEach(pur->projectUserRoleRepository.deleteById(pur.getId()));

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .companyId(userCompanyId)
                .projectId(projectId)
                .notificationType(NotificationType.PROJECT_USER_REMOVED)
                .referenceId(projectId)
                .subjectId(userId)
                .senderId(securityHelper.currentUserId())
                .build());
    }

    private void validateUserHasNoTasksAssigned(Integer projectId, Integer userId) {
        List<SubTaskUserEntity> subTaskUsers = subTaskUserRepository.findByUserAndProject(userId, projectId);
        if (!subTaskUsers.isEmpty()) {
            String taskIds = subTaskUsers.stream()
                    .map(stu -> String.valueOf(stu.getSubTask().getId()))
                    .collect(Collectors.joining(","));
            throw ProjectException.userAssignedToTasksCanNotBeRemoved(userId, taskIds);
        }
    }

    private void validateUserIsNot(Integer userId, Integer projectId, RoleName... roleNames) {
        projectUserRoleRepository.findProjectUsersWithRoles(
                projectId,
                Arrays.stream(roleNames).map(RoleName::name).collect(Collectors.toList())
        ).stream()
                .filter( pur -> userId.equals(pur.getUser().getId()))
                .findFirst()
                .ifPresent(
                        pur -> {
                            if (userId.equals(pur.getUser().getId())) {
                                throw ProjectException.projectUserWithRoleCanNotBeRemoved(userId, pur.getRole().getName());
                            }
                        }
                );
    }

    private void validateUserIsPartOfProject(Integer projectId, Integer userId, ProjectUserRole projectUserRole) {
        if (projectUserRole == null) {
            throw ProjectException.userIsNotPartOfProject(userId, projectId);
        }
    }

    public List<CompanyUserRole> findCompanyUsersNotInProject(Integer projectId) {
        projectReader.validateProjectId(projectId);

        Integer currentUserId = Integer.valueOf(LecwUtils.currentUser().getId());

        Integer companyId = securityHelper.validUserCompanyId(currentUserId);

        ProjectCompany projectCompany = getAndValidateProjectCompany(projectId, companyId);

        return companyUserRoleRepository.findUsersWhichAreNotInProject(projectId, companyId, projectCompany.getId());

    }

    private ProjectCompany getAndValidateProjectCompany(Integer projectId, Integer companyId) {
        ProjectCompany projectCompany = projectCompanyRepository.findByProjectAndCompany(projectId, companyId);
        if(projectCompany == null) {
            throw ProjectException.companyIsNotPartOfProject(companyId, projectId);
        }
        return projectCompany;
    }

    public ProjectUserRole changeAdminRole(@Reference(ReferenceType.PROJECT) Integer projectId,
                                           @Reference(ReferenceType.USER) Integer userId,
                                           @Reference(ReferenceType.ROLE) Integer roleId) {
        ProjectUserRole projectUserRole = ValidationUtils
                .throwIfEmpty(projectUserRoleRepository.findByUserAndProject(userId, projectId),
                              () -> ProjectException.userIsNotPartOfProject(userId, projectId))
                .get(0);

        Role role = getAndValidateChangeRole(projectId, userId, roleId, projectUserRole);

        // you can only downgrade to worker if the user is in your own company
        boolean isWorkerRole = RoleName.PROJECT_WORKER.name().equals(role.getName());
        if(isWorkerRole && isAnotherCompany(projectId, projectUserRole)) {
            throw ProjectException.canNotChangeRoleInOtherCompany();
        }

        // update and persist role
        projectUserRole.setRole(role);
        return projectUserRoleRepository.save(projectUserRole);
    }

    public ProjectUserRole changeNonAdminRole(@Reference(ReferenceType.PROJECT) Integer projectId,
                                              @Reference(ReferenceType.USER) Integer userId,
                                              @Reference(ReferenceType.ROLE) Integer roleId) {
        ProjectUserRole projectUserRole = ValidationUtils
                .throwIfEmpty(projectUserRoleRepository.findByUserAndProject(userId, projectId),
                              () -> ProjectException.userIsNotPartOfProject(userId, projectId))
                .get(0);
        Role role = getAndValidateChangeRole(projectId, userId, roleId, projectUserRole);

        // check if the target role or the existing role is project admin role
        boolean isTargetRoleAdmin = role.isProjectRole() && role.isAdmin();
        boolean isExistingRoleAdmin = projectUserRole.getRole().isAdmin();
        if(isTargetRoleAdmin || isExistingRoleAdmin) {
            throw ProjectException.nonAdminCanNotElevateToAdminRole();
        }

        // you can only change roles within your own company
        if(isAnotherCompany(projectId, projectUserRole)) {
            throw ProjectException.canNotChangeRoleInOtherCompany();
        }

        // update and persist role
        projectUserRole.setRole(role);
        return projectUserRoleRepository.save(projectUserRole);
    }

    private Role getAndValidateChangeRole(Integer projectId, Integer userId, Integer roleId, ProjectUserRole projectUserRole) {
        validateUserIsPartOfProject(projectId, userId, projectUserRole);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> ProjectException.roleDoesNotExists(roleId));

        // check if the role isn't project owner and if it's a valid project role
        boolean isProjectOwnerRole = RoleName.isUniqAndMandatory(role.getName());
        boolean isNotProjectRole = ! role.isProjectRole();
        if(isProjectOwnerRole || isNotProjectRole) {
            throw ProjectException.projectOwnerRoleCanNotBeDowngraded();
        }

        return role;
    }

    private boolean isAnotherCompany(Integer projectId, ProjectUserRole projectUserRole) {
        Integer currentUserId = securityHelper.currentUserId();
        ProjectUserRole currentUserRole = ValidationUtils
                .throwIfEmpty(projectUserRoleRepository.findByUserAndProject(currentUserId, projectId),
                              () -> ProjectException.userIsNotPartOfProject(currentUserId, projectId))
                .get(0);
        return !currentUserRole.getProjectCompany().getId().equals(projectUserRole.getProjectCompany().getId());
    }

    // This is very crude... but as of now, best we can do.
    private Role calculateProjectManagerRole(Integer userId) {
        if (userHasCompanyRole(userId, RoleName.CUSTOMER)) {
            return roleRepository.getRoleByName(RoleName.PROJECT_CUSTOMER);
        }
        else {
            return roleRepository.getRoleByName(RoleName.PROJECT_MANAGER);
        }
    }

    private boolean userHasCompanyRole(Integer userId, RoleName role) {
        return companyUserRoleRepository.findByUser(userId).stream()
                .map(cur -> cur.getRole().getName())
                .anyMatch( n -> n.equals(role.name()));
    } 
}
