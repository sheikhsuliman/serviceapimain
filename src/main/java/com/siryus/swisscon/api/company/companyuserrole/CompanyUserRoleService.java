package com.siryus.swisscon.api.company.companyuserrole;

import com.siryus.swisscon.api.auth.AuthException;
import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.company.CompanyExceptions;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyDetailsDTO;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleRepository;
import com.siryus.swisscon.api.util.ValidationUtils;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service("companyUserRoleService")
@Validated
public class CompanyUserRoleService {

    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final RoleRepository roleRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final UserRepository userRepository;
    private final SecurityHelper securityHelper;

    @Autowired
    public CompanyUserRoleService(
            CompanyUserRoleRepository companyUserRoleRepository,
            RoleRepository roleRepository,
            ProjectUserRoleRepository projectUserRoleRepository,
            UserRepository userRepository,
            SecurityHelper securityHelper
    ) {
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.roleRepository = roleRepository;
        this.projectUserRoleRepository = projectUserRoleRepository;
        this.userRepository = userRepository;
        this.securityHelper = securityHelper;
    }

    public CompanyDetailsDTO getCompanyTeam(Integer companyId) {
        Integer currentUserId = securityHelper.currentUserId();

        CompanyUserRole currentCompanyUserRole = findCompanyRoleByUser(currentUserId);

        List<CompanyUserRole> companyUserRoles = companyUserRoleRepository.findCompanyUsersRoleByCompany(companyId);

        // if it's not the users company > we show only the owner
        if (!currentCompanyUserRole.getCompany().getId().equals(companyId)) {
            companyUserRoles = Collections.singletonList(getCompanyOwner(companyId));
        }

        ValidationUtils.throwIfEmpty(companyUserRoles, () -> CompanyExceptions.userHasNoCompanyUserRole(currentUserId));

        ArrayList<TeamUserDTO> team = new ArrayList<>(companyUserRoles
                .stream()
                .map(TeamUserDTO::fromCompanyUserRole)
                .collect(Collectors.toMap(TeamUserDTO::getId, zu -> zu,
                        (existing, replacement) ->
                                existing.toBuilder()
                                        .roleIds(ListUtils.union(existing.getRoleIds(), replacement.getRoleIds()))
                                        .build()
                )).values());
        return CompanyDetailsDTO.from(companyUserRoles.get(0).getCompany(), team);
    }

    @Transactional
    public CompanyUserRole linkUserToCurrentUserCompany(Integer currentUserId, User user, @NotNull Integer roleId) {
        CompanyUserRole currentCompanyUserRole = findCompanyRoleByUser(currentUserId);
        CompanyUserRole companyUserRole = new CompanyUserRole();
        companyUserRole.setCompany(currentCompanyUserRole.getCompany());
        companyUserRole.setRole(getRole(roleId));
        companyUserRole.setUser(user);
        return companyUserRoleRepository.save(companyUserRole);
    }

    @Transactional
    public CompanyUserRole create(CompanyUserRole companyUserRole) {
        return companyUserRoleRepository.save(companyUserRole);
    }

    @Transactional
    public CompanyUserRole addUserToCompanyTeam(User u, Company company, String roleName) {
        CompanyUserRole companyUserRole = CompanyUserRole.builder()
                .user(u)
                .company(company)
                .role(roleRepository.getRoleByName(roleName))
                .build();

        return companyUserRoleRepository.save(companyUserRole);
    }

    @Transactional()
    public void removeUserFromCompanyTeam(Integer companyId, Integer userId) {
        ValidationUtils.throwIfNot(securityHelper.checkUserIsPartOfCompany(userId, companyId),
                                   () -> CompanyExceptions.userIsNotFromCompany(userId, companyId));

        List<CompanyUserRole> allCompanyUserRoles = companyUserRoleRepository.findByUser(userId);

        List<ProjectUserRole> projectUserRoles = projectUserRoleRepository.findByCompanyAndUser(companyId, userId);
        if(!projectUserRoles.isEmpty()) {
            List<Integer> projectIds = projectUserRoles.stream().map(pur -> pur.getProject().getId()).collect(Collectors.toList());
            throw CompanyExceptions.userInProjectsCanNotBeRemoved(projectIds);
        }

        final List<CompanyUserRole> companyUserRoles = allCompanyUserRoles.stream()
                .filter(cur -> cur.getCompany().getId().equals(companyId))
                .collect(Collectors.toList());

        companyUserRoles.forEach(cur-> companyUserRoleRepository.deleteById(cur.getId()));

        // if user is only part of one company, he can be disabled
        if(companyUserRoles.size() == allCompanyUserRoles.size()) {
            userRepository.disable(userId);
        }
    }

    public List<CompanyUserRole> getCompanyOwners(List<Integer> companyIds) {
        return companyIds.stream().map(this::getCompanyOwner).collect(Collectors.toList());
    }

    /** TODO as soon as we allow multiple companies per user > this needs to be removed **/
    public CompanyUserRole findCompanyRoleByUser(Integer userId) {
        return ValidationUtils.throwIfEmpty(
                companyUserRoleRepository.findByUser(userId),
                () -> CompanyExceptions.userHasNoCompanyUserRole(userId))
                .get(0);
    }

    public CompanyUserRole getCompanyOwner(Integer companyId) {
        List<CompanyUserRole> companyOwnerRoles = companyUserRoleRepository
                .findByCompanyAndRoles(companyId, Arrays.asList(RoleName.COMPANY_OWNER.name(), RoleName.CUSTOMER.name()));

        ValidationUtils.throwIf(companyOwnerRoles.size() > 1, () -> CompanyExceptions.oneOwnerPerCompany(companyId));

        // TODO as soon as "invite company" only allow CUSTOMER or COMPANY_OWNER Role > this can be removed.
        if (companyOwnerRoles.isEmpty()) {
            return companyUserRoleRepository.findCompanyUsersRoleByCompany(companyId)
                    .stream()
                    .filter(cur -> cur.getCompany().getCreatedBy().equals(cur.getUser().getId()))
                    .findFirst()
                    .orElseThrow(() -> CompanyExceptions.oneOwnerPerCompany(companyId));
        }
        return companyOwnerRoles.get(0);
    }

    private Role getRole(Integer roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> AuthException.roleDoesNotExist(roleId));
    }
}
