package com.siryus.swisscon.api.util.security;

import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.auth.permission.Permission;
import com.siryus.swisscon.api.auth.permission.PermissionName;
import com.siryus.swisscon.api.auth.permission.PermissionRepository;
import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.company.CompanyExceptions;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.contract.ContractExceptions;
import com.siryus.swisscon.api.contract.repos.ContractEntity;
import com.siryus.swisscon.api.contract.repos.ContractRepository;
import com.siryus.swisscon.api.file.FileExceptions;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileRepository;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.project.ProjectException;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompany;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompanyRepository;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleRepository;
import com.siryus.swisscon.api.tasks.exceptions.TaskExceptions;
import com.siryus.swisscon.api.tasks.repos.MainTaskRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskUserRepository;
import com.siryus.swisscon.api.util.ValidationUtils;
import com.siryus.swisscon.security.SecurityException;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SecurityHelper {
    private final SubTaskUserRepository subTaskUserRepository;

    private final MainTaskRepository mainTaskRepository;

    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final ProjectCompanyRepository projectCompanyRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final ContractRepository contractRepository;
    private final FileRepository fileRepository;
    
    @Autowired
    public SecurityHelper(
            SubTaskUserRepository subTaskUserRepository,
            MainTaskRepository mainTaskRepository,
            ProjectUserRoleRepository projectUserRoleRepository,
            ProjectCompanyRepository projectCompanyRepository,
            CompanyUserRoleRepository companyUserRoleRepository,
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            ContractRepository contractRepository,
            FileRepository fileRepository
    ) {
        this.subTaskUserRepository = subTaskUserRepository;
        this.mainTaskRepository = mainTaskRepository;
        this.projectUserRoleRepository = projectUserRoleRepository;
        this.projectCompanyRepository = projectCompanyRepository;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.contractRepository = contractRepository;
        this.fileRepository = fileRepository;
    }

    public Integer currentUserId() {
        return Integer.parseInt(LecwUtils.currentUser().getId());
    }

    public void validateUserIsPartOfMainTaskProject(Integer userId, Integer mainTaskId) {
        validateUserIsPartOfProject(userId,mainTaskRepository.findById(mainTaskId).orElseThrow(
                () -> TaskExceptions.mainTaskNotFound(mainTaskId)
        ).getProjectId());
    }

    public void validateUserIsPartOfProject(Integer userId, Integer projectId) {
        if (!checkUserIsPartOfProject(userId, projectId)) {
            throw SecurityException.userNotPartOfProject(userId, projectId);
        }
    }

    public void validateUserIsPartOfCurrentUserCompany(Integer userId) {
        List<CompanyUserRole> companyUserRoles = companyUserRoleRepository.findByUser(userId);
        List<CompanyUserRole> currentCompanyUserRoles = companyUserRoleRepository.findByUser(currentUserId());

        boolean userIsInSameCompanyThanCurrentUser = companyUserRoles
                .stream()
                .anyMatch(cur -> currentCompanyUserRoles
                        .stream()
                        .anyMatch(currentCur -> currentCur.getCompany().getId().equals(cur.getCompany().getId()))
                );
        ValidationUtils.throwIfNot(userIsInSameCompanyThanCurrentUser, ()->SecurityException.userIsNotPartOfCurrentUserCompany(userId));
    }

    public boolean checkUserIsPartOfProject(Integer userId, Integer projectId) {
        return !projectUserRoleRepository.findByUserAndProject(userId, projectId).isEmpty();
    }

    public void validateUserIsPartOfCompany(Integer userId, Integer companyId) {
        if (!checkUserIsPartOfCompany(userId, companyId)) {
            throw SecurityException.userNotPartOfCompany(userId, companyId);
        }
    }

    public boolean checkUserIsPartOfCompany(Integer userId, Integer companyId) {
        return !companyUserRoleRepository.findByUserAndCompany(userId, companyId).isEmpty();
    }

    public void validateUserIdPartOfSubTasksTeam(Integer userId, Integer... subTaskIds) {
        if (!checkUserIdPartOfSubTasksTeam(userId, subTaskIds)) {
            throw SecurityException.userNotPartOfSubTaskTeam(userId, subTaskIds[0]);
        }
    }

    public boolean checkUserIdPartOfSubTasksTeam(Integer userId, Integer... subTaskIds) {
        return Arrays.stream(subTaskIds)
                .anyMatch(subTaskId -> subTaskUserRepository.findUsersBySubTask(subTaskId).stream()
                        .anyMatch(u->userId.equals(u.getId())));
    }

    public boolean checkIfUserHasProjectPermission(Integer userId, Integer projectId, PermissionName permission) {
        return permissionRepository.findByUserAndProject(userId, projectId).stream().anyMatch(
                p -> p.getName().equals(permission.name())
        );
    }

    public void validateUserHasProjectPermission(Integer userId, Integer projectId, PermissionName permission) {
        if (!checkIfUserHasProjectPermission(userId, projectId, permission)) {
            throw SecurityException.userDoesNotHavePermission(userId, permission);
        }
    }

    public boolean checkIfUserHasCompanyPermission(Integer userId, PermissionName permission) {
        Integer companyId = ValidationUtils.throwIfEmpty(
                companyUserRoleRepository
                        .findByUser(userId),
                () -> CompanyExceptions.userHasNoCompanyUserRole(userId)
        )
                .get(0).getId();

        return permissionRepository.findByUserAndCompany(userId, companyId)
                .stream()
                .anyMatch(p -> p.getName().equals(permission.name()));
    }

    public void validateUserHasCompanyPermission(Integer userId, PermissionName permission) {
        if (!checkIfUserHasCompanyPermission(userId, permission)) {
            throw SecurityException.userDoesNotHavePermission(userId, permission);
        }
    }

    public void validateUserIsPartOfContractCompanies(Integer contractId, Integer userId) {
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> ContractExceptions.contractDoesNotExist(contractId));

        Integer contractorCompanyId = Optional.ofNullable(contract.getContractorId()).orElse(null);
        Integer customerCompanyId = Optional.ofNullable(contract.getCustomerId()).orElse(null);

        if (!checkUserIsPartOfCompany(userId, contractorCompanyId) && !checkUserIsPartOfCompany(userId, customerCompanyId)) {
            throw ContractExceptions.userNotPartOfContractCompanies(userId, contractId);
        }
    }

    public Integer validateAndGetCurrentUserContractCompany(Integer contractId) {
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> ContractExceptions.contractDoesNotExist(contractId));

        if(checkUserIsPartOfCompany(currentUserId(), contract.getContractorId())) {
            return contract.getContractorId();
        } else if(checkUserIsPartOfCompany(currentUserId(), contract.getCustomerId())){
            return contract.getCustomerId();
        }
        throw ContractExceptions.userNotPartOfContractCompanies(currentUserId(), contractId);
    }

    public Integer getProjectCustomerCompanyId(Integer projectId) {
        return projectUserRoleRepository.findProjectTeam(projectId)
                .stream()
                .filter(pur -> pur.getRole().getId()
                        .equals(roleRepository.getRoleByName(RoleName.PROJECT_CUSTOMER.name()).getId()))
                .map(pur -> pur.getProjectCompany().getCompany().getId())
                .findFirst()
                .orElse(null);
    }

    public void validateUserOwnsTemporaryFile(Integer userId, Integer fileId) {
        if (fileId == null) {
            throw FileExceptions.noFileInForm();
        }
        
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> TaskExceptions.attachmentNotFound(fileId));

        if (!file.getCreatedBy().equals(userId) || file.getDisabled() != null) {            
            throw FileExceptions.fileIsNotOwnBy(userId, fileId);
        }

        if (!file.getReferenceType().equals(ReferenceType.TEMPORARY.name())) {
            throw FileExceptions.fileIsNotAssignable(fileId);
        }
    }

    public boolean isCompanyPartOfProject(Integer projectId, Integer companyId) {
        return projectCompanyRepository.findByProjectAndCompany(projectId, companyId) != null;
    }

    public List<TeamUserDTO> getProjectMembersFromCompanyWithPermissions(Integer projectId, Integer companyId, List<PermissionName> permissions) {
        ProjectCompany projectCompany = projectCompanyRepository.findByProjectAndCompany(projectId, companyId);

        return projectCompany == null
                ? Collections.emptyList()
                : new ArrayList<>(projectUserRoleRepository.findProjectCompanyTeam(projectCompany.getId()).stream()
                    .filter(r -> hasAllPermissions(r, permissions))
                    .collect(Collectors.toMap(
                            pur -> pur.getUser().getId(),
                            TeamUserDTO::fromProjectUserRole,
                            (existing, replacement) -> existing.toBuilder().roleIds(
                                    ListUtils.union(existing.getRoleIds(), replacement.getRoleIds())
                            ).build()
                    ))
                    .values()
        );
    }

    public static List<Integer> uniqUserIdsFromRoles(List<TeamUserDTO> projectUserRoles) {
        return projectUserRoles.stream()
                .map(TeamUserDTO::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    public Integer roleId(RoleName roleName) {
        return roleId(roleName.name());
    }

    public Integer roleId(String roleName) {
        return Optional.ofNullable(roleRepository.getRoleByName(roleName)).map(Role::getId).orElse(null);
    }

    private boolean hasAllPermissions(ProjectUserRole projectUserRole, List<PermissionName> permissions) {
        return projectUserRole.getRole().getPermissions().stream()
                .map(Permission::getName)
                .map(PermissionName::valueOf)
                .collect(Collectors.toSet())
                .containsAll(permissions);
    }

    public TeamUserDTO companyOwner(Integer companyId) {
        return companyUserRoleRepository.findByCompanyAndRoles(
                companyId,
                Arrays.asList(RoleName.COMPANY_OWNER.name(), RoleName.CUSTOMER.name())
        ).stream()
                .map(TeamUserDTO::fromCompanyUserRole)
                .findFirst()
                .orElseThrow(() -> CompanyExceptions.oneOwnerPerCompany(companyId));
    }

    public TeamUserDTO user(Integer userId) {
        return TeamUserDTO.fromCompanyUserRole(companyUserRoleRepository.findByUser(userId).get(0));
    }

    public Integer userCompanyId(Integer userId) {
        return companyUserRoleRepository.findByUser(userId)
                .stream().findFirst().map(e -> e.getCompany().getId()).orElse(null);
    }

    public Integer validUserCompanyId(Integer userId) {
        return Optional.ofNullable(userCompanyId(userId)).orElseThrow(
                () -> ProjectException.invalidCompanyOrUser()
        );
    }
}
