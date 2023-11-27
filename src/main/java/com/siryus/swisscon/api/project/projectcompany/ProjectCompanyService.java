package com.siryus.swisscon.api.project.projectcompany;

import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.project.ProjectException;
import com.siryus.swisscon.api.project.project.Project;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleRepository;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleService;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.api.util.validator.Reference;
import com.siryus.swisscon.soa.EventsEmitter;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service("projectCompanyService")
@Validated
public class ProjectCompanyService {

    private final ProjectCompanyRepository projectCompanyRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final ProjectUserRoleService projectUserRoleService;
    private final SecurityHelper securityHelper;
    private final EventsEmitter eventsEmitter;

    @Autowired
    public ProjectCompanyService(ProjectCompanyRepository projectCompanyRepository, ProjectUserRoleRepository projectUserRoleRepository, ProjectUserRoleService projectUserRoleService, SecurityHelper securityHelper, EventsEmitter eventsEmitter) {
        this.projectCompanyRepository = projectCompanyRepository;
        this.projectUserRoleRepository = projectUserRoleRepository;
        this.projectUserRoleService = projectUserRoleService;
        this.securityHelper = securityHelper;
        this.eventsEmitter = eventsEmitter;
    }

    @Transactional
    public ProjectCompany addCompanyToProject(Integer projectId, Integer companyId) {

        ProjectCompany existingProjectCompany = projectCompanyRepository.findByProjectAndCompany(projectId, companyId);

        if (existingProjectCompany != null) {
            throw ProjectException.companyAlreadyPartOfProject(companyId, projectId);
        }

        ProjectCompany tempProjectCompany = ProjectCompany.builder()
                .project(Project.builder().id(projectId).build())
                .company(Company.builder().id(companyId).build())
            .build();

        return projectCompanyRepository.save(tempProjectCompany);
    }

    @Transactional
    public void removeCompanyFromProject(
            @Reference(ReferenceType.PROJECT) Integer projectId,
            @Reference(ReferenceType.COMPANY) Integer companyId
    ) {
        ProjectCompany existingProjectCompany = projectCompanyRepository.findByProjectAndCompany(projectId, companyId);

        if (existingProjectCompany == null) {
            throw ProjectException.companyIsNotPartOfProject(companyId, projectId);
        }

        // Delete every user from project first > If he has tasks assigned he cannot be deleted
        projectUserRoleRepository
                .findByProjectCompany(existingProjectCompany.getId())
                .stream()
                .map(pur -> pur.getUser().getId())
                .distinct()
                .forEach(uid -> projectUserRoleService
                        .deleteUserFromProjectTeam(projectId, uid, true)
                );

        projectCompanyRepository.deleteById(existingProjectCompany.getId());

        eventsEmitter.emitNotification(NotificationEvent.builder()
                        .projectId(projectId)
                        .notificationType(NotificationType.PROJECT_COMPANY_REMOVED)
                        .referenceId(projectId)
                        .subjectId(companyId)
                        .senderId(securityHelper.currentUserId())
                .build());
    }

    public List<Company> findCompaniesNotInProject(Integer projectId) {
        return projectCompanyRepository.findCompaniesNotInProject(projectId);
    }

    public ProjectCompany findByProjectAndCompany(Project project, Company company) {
        return projectCompanyRepository.findByProjectAndCompany(project.getId(), company.getId());
    }

    public List<ProjectCompany> findByProjectAndCompanyActive(Integer projectId) {
        return projectCompanyRepository.findActiveByProject(projectId);
    }

}
