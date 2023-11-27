package com.siryus.swisscon.security;

import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleService;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.project.project.Project;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleRepository;

import java.util.List;

public class DefaultContextResolver implements SiryusPermissionChecker.UserDefaultResolver {

    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final CompanyUserRoleService companyUserRoleService;

    public DefaultContextResolver(ProjectUserRoleRepository projectUserRoleRepository, CompanyUserRoleService companyUserRoleService) {
        this.projectUserRoleRepository = projectUserRoleRepository;
        this.companyUserRoleService = companyUserRoleService;
    }

    @Override
    public AuthorizationTarget resolveDefaultTarget(Integer userId, String targetType) {
        final Class targetClass;
        final List<ProjectUserRole> roles;
        final CompanyUserRole companyUserRole;
        final AuthorizationTarget authorizationTarget = new AuthorizationTarget();

        try {
            roles = this.projectUserRoleRepository.findProjectOwnerRoles(userId);
            if (roles.size() != 1) { //meaning the user does not own a project, or owns multiple ones
                companyUserRole = this.companyUserRoleService.findCompanyRoleByUser(userId);
                if (null != companyUserRole) {
                    authorizationTarget.setCompanyId(companyUserRole.getCompany().getId());
                }
            } else {
                // todo: the db does not currently provide protection against ProjectUserRole and the associated
                //       ProjectCompany relating to different projects, although this would not make sense. We write
                //       our code to not get that situation, but it's technically possible and might have confusing
                //       consequences if it were to occur. (HF, 2020-01-04, SIR-629)
                authorizationTarget.setCompanyId(roles.get(0).getProjectCompany().getCompany().getId());
                authorizationTarget.setProjectId(roles.get(0).getProject().getId());
            }
            if (ReferenceType.isValidReferenceType(targetType)) {
                targetClass = ReferenceType.valueOf(targetType).getReferencedClass();
            }
            else {
                targetClass = Class.forName(targetType);
            }
            if (Company.class.isAssignableFrom(targetClass)) {
                if (!authorizationTarget.hasCompanyId()) {
                    throw SecurityException.requiredResolutionFailed(targetType, null);
                }
            } else if (Project.class.isAssignableFrom(targetClass)) {
                if (!authorizationTarget.hasProjectId()) {
                    throw SecurityException.requiredResolutionFailed(targetType, null);
                }
            } else {
                throw SecurityException.unsupportedTargetType(targetType);
            }
        } catch (ClassNotFoundException e) {
            throw SecurityException.unknownTargetType(targetType);
        }

        return authorizationTarget;
    }
}
