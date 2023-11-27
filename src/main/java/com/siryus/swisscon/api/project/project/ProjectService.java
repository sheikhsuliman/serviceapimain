package com.siryus.swisscon.api.project.project;

import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.auth.permission.Permission;
import com.siryus.swisscon.api.auth.permission.PermissionRepository;
import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.company.CompanyExceptions;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyDirectoryDTO;
import com.siryus.swisscon.api.company.company.CompanyService;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.contract.ContractPublicService;
import com.siryus.swisscon.api.contract.dto.ContractState;
import com.siryus.swisscon.api.customroles.CustomRoleExceptions;
import com.siryus.swisscon.api.customroles.CustomRoleReader;
import com.siryus.swisscon.api.customroles.dto.CustomRoleDTO;
import com.siryus.swisscon.api.event.ContractStateChangeEvent;
import com.siryus.swisscon.api.event.EventPublisher;
import com.siryus.swisscon.api.event.ProjectContractorCompanyEvent;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileRepository;
import com.siryus.swisscon.api.file.file.FileService;
import com.siryus.swisscon.api.general.country.Country;
import com.siryus.swisscon.api.general.country.CountryService;
import com.siryus.swisscon.api.general.favorite.FavoriteRepository;
import com.siryus.swisscon.api.general.favorite.FavoriteService;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.location.location.LocationService;
import com.siryus.swisscon.api.mediawidget.MediaWidgetService;
import com.siryus.swisscon.api.project.ProjectException;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompany;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompanyRepository;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompanyService;
import com.siryus.swisscon.api.project.projectstatus.ProjectStatus;
import com.siryus.swisscon.api.project.projectstatus.ProjectStatusRepository;
import com.siryus.swisscon.api.project.projecttype.ProjectType;
import com.siryus.swisscon.api.project.projecttype.ProjectTypeService;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleRepository;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleService;
import com.siryus.swisscon.api.util.DateConverter;
import com.siryus.swisscon.api.util.ValidationUtils;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.api.util.validator.Reference;
import com.siryus.swisscon.soa.EventsEmitter;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("projectService")
@Validated
public class ProjectService {

    private static final int DUMMY_PROGRESS_PERCENTAGE = 30;

    private final FavoriteRepository favoriteRepository;
    private final LocationService locationService;
    private final FileService fileService;
    private final ProjectStatusRepository projectStatusRepository;
    private final ProjectRepository projectRepository;
    private final FileRepository fileRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final ProjectCompanyRepository projectCompanyRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final PermissionRepository permissionRepository;
    private final CustomRoleReader roleReader;
    private final ProjectCompanyService projectCompanyService;
    private final ProjectUserRoleService projectUserRoleService;
    private final MediaWidgetService mediaWidgetService;
    private final CompanyService companyService;
    private final ProjectTypeService projectTypeService;
    private final CountryService countryService;
    private final FavoriteService favoriteService;
    
    private final ProjectReader projectReader;
    private final EventPublisher eventPublisher;

    private final SecurityHelper securityHelper;
    private final ContractPublicService contractService;
    private final EventsEmitter eventsEmitter;

	@Autowired
    public ProjectService(
            FavoriteRepository favoriteRepository,
            LocationService locationService,
            FileService fileService,
            ProjectStatusRepository projectStatusRepository,
            ProjectRepository projectRepository,
            FileRepository fileRepository,
            CompanyUserRoleRepository companyUserRoleRepository,
            ProjectCompanyRepository projectCompanyRepository,
            ProjectUserRoleRepository projectUserRoleRepository,
            PermissionRepository permissionRepository,
            CustomRoleReader roleReader,
            ProjectCompanyService projectCompanyService,
            ProjectUserRoleService projectUserRoleService,
            MediaWidgetService mediaWidgetService,
            ProjectReader projectReader,
            CompanyService companyService,
            ProjectTypeService projectTypeService, CountryService countryService, FavoriteService favoriteService, EventPublisher eventPublisher,
            SecurityHelper securityHelper,
            ContractPublicService contractService,
            EventsEmitter eventsEmitter) {
        this.favoriteRepository = favoriteRepository;
        this.locationService = locationService;
        this.fileService = fileService;
        this.projectStatusRepository = projectStatusRepository;
        this.projectRepository = projectRepository;
        this.fileRepository = fileRepository;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.projectCompanyRepository = projectCompanyRepository;
        this.projectUserRoleRepository = projectUserRoleRepository;
        this.permissionRepository = permissionRepository;
        this.roleReader = roleReader;
        this.projectCompanyService = projectCompanyService;
        this.projectUserRoleService = projectUserRoleService;
        this.mediaWidgetService = mediaWidgetService;
        this.projectReader = projectReader;
        this.companyService = companyService;
        this.projectTypeService = projectTypeService;
        this.countryService = countryService;
        this.favoriteService = favoriteService;
        this.eventPublisher = eventPublisher;
        this.securityHelper = securityHelper;
        this.contractService = contractService;
        this.eventsEmitter = eventsEmitter;
    }

    public Page<ProjectDTO> findProjectsPaginated(Pageable pageable) {
        Integer currentUserId = Integer.valueOf(LecwUtils.currentUser().getId());
        Page<Project> paginated = projectUserRoleRepository.findActiveProjectsUserAssociatedWith(currentUserId, pageable);

        return paginated.map(p -> dtoFrom(p, currentUserId));
    }

    public Page<ProjectDTO> getArchivedProjectsPaginated(Pageable pageable) {
        Integer currentUserId = Integer.valueOf(LecwUtils.currentUser().getId());
        Page<Project> paginated = projectUserRoleRepository.findArchivedProjectsUserAssociatedWith(currentUserId, pageable);

        return paginated.map(p -> dtoFrom(p, currentUserId));
    }
    
    @Transactional
    public void archiveProject(Integer id) {
        final ProjectDTO projectDTO = findProjectById(id);

        projectRepository.archiveProject(id);
        favoriteRepository.removeForAllUsers(id, ReferenceType.PROJECT.toString());

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .companyId(projectDTO.getProjectOwnerCompanyId())
                .projectId(id)
                .notificationType(NotificationType.PROJECT_ARCHIVED)
                .referenceId(id)
                .subjectId(id)
                .senderId(securityHelper.currentUserId())
                .build());
    }

    @Transactional
    public void restoreProject(Integer id) {
        projectRepository.restoreProject(id);
    }

    public ProjectDTO dtoFrom(@NotNull Project project, Integer userId) {
        boolean projectIsFavorite = favoriteRepository.exists(userId, project.getId(), ReferenceType.PROJECT.toString());

        return dtoFrom(project, projectIsFavorite);
    }

    public ProjectDTO dtoFrom(@NotNull Project project, Boolean isFavorite) {
        Integer progressPercentage = DUMMY_PROGRESS_PERCENTAGE; //TODO how do we calculate the percentage?

        ProjectUserRole owner = projectUserRoleRepository.findProjectOwner(project.getId())
                .orElseThrow(()-> ProjectException.projectHasNoOwner(project.getId()));
        Integer projectCustomerId = securityHelper.getProjectCustomerCompanyId(project.getId());

        return ProjectDTO.fromProject(project, owner, projectCustomerId, isFavorite, progressPercentage);
    }

    Integer getProjectOwnerCompanyId(Project project) {
        ProjectUserRole owner = projectUserRoleRepository.findProjectOwner(project.getId())
                .orElseThrow(()-> ProjectException.projectHasNoOwner(project.getId()));

        return owner.getProjectCompany().getCompany().getId();
    }


    public Project createProjectInternal(NewProjectDTO projectData, Integer userId) {
        Project savedProject = materializeProject(projectData);

        materializeDefaultImage(savedProject, projectData.getDefaultImageId());

        materializeTeams(savedProject, userId);

        locationService.createTopLocation(savedProject, savedProject.getName());

        mediaWidgetService.createDefaultFolders(ReferenceType.PROJECT, savedProject.getId());

        return savedProject;
    }

    @Transactional
    public ProjectBoardDTO createProject(@Valid NewProjectDTO projectData) {
	    Integer userId = securityHelper.currentUserId();

        projectTypeService.validateProjectTypeId(projectData.getTypeId());

        if (projectData.getAddress() != null && projectData.getAddress().getCountryId() != null ) {
            countryService.validateCountryId(projectData.getAddress().getCountryId());
        }

        // check if the file was created by the same user
        if(projectData.getDefaultImageId() != null) {
            fileService.validateFileOwner(userId, projectData.getDefaultImageId());
        }

        Project project = createProjectInternal(projectData, userId);

        final ProjectBoardDTO projectBoardDTO = ProjectBoardDTO.from(project,
                getProjectOwnerCompanyId(project),
                securityHelper.getProjectCustomerCompanyId(project.getId()),
                false,
                true);

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .companyId(projectBoardDTO.getProjectOwnerCompanyId())
                .projectId(project.getId())
                .notificationType(NotificationType.PROJECT_CREATED)
                .referenceId(project.getId())
                .subjectId(project.getId())
                .senderId(securityHelper.currentUserId())
                .build());

        return projectBoardDTO;
    }

    @Transactional
    public  ProjectBoardDTO editProject(Integer projectId, EditProjectDTO editProjectDTO, boolean customerIsReassignable) {
        Integer userId = securityHelper.currentUserId();

        Project project = findActiveProjectById(projectId);

        projectTypeService.validateProjectTypeId(editProjectDTO.getTypeId());

        if (editProjectDTO.getAddress() != null && editProjectDTO.getAddress().getCountryId() != null ) {
            countryService.validateCountryId(editProjectDTO.getAddress().getCountryId());
        }

        Project editedProject = editProjectInternal(project, editProjectDTO);

        final ProjectBoardDTO projectBoardDTO = ProjectBoardDTO.from(editedProject,
                getProjectOwnerCompanyId(project),
                securityHelper.getProjectCustomerCompanyId(project.getId()),
                favoriteService.userStarred(userId, project.getId(), ReferenceType.PROJECT),
                customerIsReassignable);

        eventsEmitter.emitCacheUpdate(ReferenceType.PROJECT, projectId);
        eventsEmitter.emitNotification(NotificationEvent.builder()
                .companyId(projectBoardDTO.getProjectOwnerCompanyId())
                .projectId(project.getId())
                .notificationType(NotificationType.PROJECT_UPDATED)
                .referenceId(project.getId())
                .subjectId(project.getId())
                .senderId(securityHelper.currentUserId())
                .build());

        return projectBoardDTO;
    }

    @Transactional
    public Project editProjectInternal(Project project, EditProjectDTO editProjectDTO) {

        // update picture and delete old one
        if(editProjectDTO.getDefaultImageId() != null) {
            File previousPicture = project.getDefaultImage();
            File newPicture = fileService.findById(editProjectDTO.getDefaultImageId());
            if(fileService.isNewPicture(previousPicture, newPicture)) {
                newPicture.setReferenceType(ReferenceType.PROJECT.toString());
                newPicture.setReferenceId(project.getId());
                File updatedNewPicture = fileService.update(newPicture);
                project.setDefaultImage(updatedNewPicture);
                Optional.ofNullable(previousPicture).ifPresent(p -> fileService.disable(previousPicture));
            }
        }

        // set simple properties
        project.setName(editProjectDTO.getName());
        project.setDescription(editProjectDTO.getDescription());
        project.setLongitude(editProjectDTO.getLongitude());
        project.setLatitude(editProjectDTO.getLatitude());
        project.setStartDate(DateConverter.toUtcLocalDateTime(editProjectDTO.getStartDate()));
        project.setEndDate(DateConverter.toUtcLocalDateTime(editProjectDTO.getEndDate()));

        // set address properties
        AddressDTO addressDTO = editProjectDTO.getAddress() == null ? new AddressDTO() : editProjectDTO.getAddress();
        project.setStreet(addressDTO.getAddress());
        project.setCode(addressDTO.getPostalCode());
        project.setCity(addressDTO.getCity());
        project.setCountry(
                Optional.ofNullable(addressDTO.getCountryId())
                .map(
                    id -> Country.builder().id(id).build()
                )
                .orElse(null)
        );

        // set project type
        ProjectType projectType = null;
        if(editProjectDTO.getTypeId() != null) {
            projectType = new ProjectType();
            projectType.setId(editProjectDTO.getTypeId());
        }
        project.setType(projectType);

        final Project savedProject = projectRepository.save(project);
        eventsEmitter.emitCacheUpdate(ReferenceType.PROJECT, project.getId());
        return savedProject;
    }

    @Transactional
    public void assignCustomerToProject(
            @Reference(ReferenceType.PROJECT) Integer projectId,
            @Reference(ReferenceType.COMPANY) Integer customerCompanyId
    ) {
	    validateCanChangeCustomer(projectId);
	    assignCustomer(projectId, customerCompanyId);
	    contractService.updateProjectContractsCustomers(projectId, customerCompanyId);

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .projectId(projectId)
                .notificationType(NotificationType.PROJECT_CUSTOMER_ASSIGNED)
                .referenceId(projectId)
                .subjectId(customerCompanyId)
                .senderId(securityHelper.currentUserId())
                .build());
    }

    private void validateCanChangeCustomer(Integer projectId) {
	    contractService.listContractsForProject(projectId).stream()
            .filter(
                c -> ! ( c.getContractState().equals(ContractState.CONTRACT_DRAFT) || c.getContractState().isDeclined())
            )
            .findFirst()
            .ifPresent( c -> {
                throw ProjectException.canNotChangeCustomerActiveContracts(projectId);
            });
    }

    private void assignCustomer(Integer projectId, Integer customerCompanyId) {
	    var companyOwner = securityHelper.companyOwner(customerCompanyId).getId();

	    var customerToByProjectRole = projectUserRoleRepository.findProjectUsersWithRoles(projectId, Arrays.asList(
	            RoleName.PROJECT_OWNER.name(), RoleName.PROJECT_MANAGER.name(),RoleName.PROJECT_CUSTOMER.name()
        )).stream()
                .filter( pur -> companyOwner.equals(pur.getUser().getId()))
                .findFirst()
                .orElseThrow(() -> ProjectException.companyIsNotPartOfProject(customerCompanyId, projectId));

        projectUserRoleRepository.deleteAllUsersWithRoleFromProject(projectId, securityHelper.roleId(RoleName.PROJECT_CUSTOMER));

        projectUserRoleRepository.saveUserRolesForProject(
                customerToByProjectRole.getUser().getId(),
                projectId,
                customerToByProjectRole.getProjectCompany().getId(),
                Collections.singletonList(securityHelper.roleId(RoleName.PROJECT_CUSTOMER))
        );
    }

    public List<Permission> loadUserPermissions(Integer userId, Integer projectId) {
	    projectReader.validateProjectId(projectId);
	    return this.permissionRepository.findByUserAndProject(userId, projectId);
    }

    @Transactional
    public CompanyDirectoryDTO addCompanyToProjectAndAddOwner(Integer projectId, Integer companyId, Integer roleId) {
        projectReader.validateProjectId(projectId);
        
        if (roleId != null) {
            validateProjectUserRole(roleId);
        }
        
        Company company = companyService.getValidCompany(companyId);
        
        ProjectCompany projectCompany = projectCompanyService.addCompanyToProject(projectId, companyId);

        final CompanyDirectoryDTO companyDirectoryDTO = CompanyDirectoryDTO.from(projectUserRoleService.addCompanyOwnerToProject(projectCompany, roleId), company);

        eventsEmitter.emitNotification(NotificationEvent.builder()
                .projectId(projectId)
                .notificationType(NotificationType.PROJECT_COMPANY_ADDED)
                .referenceId(projectId)
                .subjectId(companyId)
                .senderId(securityHelper.currentUserId())
                .build());

        return companyDirectoryDTO;
    }

    public ProjectDTO findProjectById(Integer projectId) {
        return dtoFrom(projectRepository.findById(projectId)
                .orElseThrow(() -> ProjectException.projectNotFound(projectId)), false);
    }

    public Project findActiveProjectById(Integer projectId) {
        return projectRepository.findActiveProjectById(projectId)
                .orElseThrow(() -> ProjectException.projectNotFound(projectId));
    }

    private Project materializeProject(NewProjectDTO projectData) {
        Project project = Project.builder()
                .name(projectData.getName())
                .description(projectData.getDescription())
                .latitude(projectData.getLatitude())
                .longitude(projectData.getLongitude())
                .startDate(DateConverter.toUtcLocalDateTime(projectData.getStartDate()))
                .endDate(DateConverter.toUtcLocalDateTime(projectData.getEndDate()))
                .defaultImage(
                        Optional.ofNullable(projectData.getDefaultImageId()).map(
                                id -> File.builder().id(id).build()
                        ).orElse(null)
                )
                .type(
                        Optional.ofNullable(projectData.getTypeId()).map(
                                id -> ProjectType.builder().id(id).build()
                        ).orElse(null)
                )
                .build();

        // set address
        if (projectData.getAddress() != null) {
            project.setStreet(projectData.getAddress().getAddress());
            project.setCode(projectData.getAddress().getPostalCode());
            project.setCity(projectData.getAddress().getCity());
            if (projectData.getAddress().getCountryId() != null) {
                project.setCountry(Country.builder().id(projectData.getAddress().getCountryId()).build());
            }
        }

        // Set project status (phase) to pre-planned
        Optional<ProjectStatus> projectStatusOpt = this.projectStatusRepository.findById(1);
        projectStatusOpt.ifPresent(project::setStatus);

        // Save the project
        return projectRepository.save(project);
    }

    private void materializeDefaultImage(Project project, Integer imageId) {
        if (imageId != null) {
            File defaultImage = fileService.findById(imageId);
            defaultImage.setReferenceId(project.getId());
            defaultImage.setReferenceType(ReferenceType.PROJECT.toString());
            fileRepository.save(defaultImage);
        }
    }

    private void materializeTeams(Project project, Integer userId) {
        var companyUserRole = ValidationUtils.throwIfEmpty(
                companyUserRoleRepository.findByUser(userId),
                () -> CompanyExceptions.userHasNoCompanyUserRole(userId)
        ).get(0);

        materializeProjectUserRoles(
                project,
                materializeProjectCompany(project, companyUserRole.getCompany()),
                companyUserRole.getUser()
        );
    }

    private ProjectCompany materializeProjectCompany(Project project, Company company) {
        return projectCompanyRepository.save(
                ProjectCompany.builder()
                        .project(project)
                        .company(company)
                        .build()
        );
    }

    private void materializeProjectUserRoles(Project project, ProjectCompany company, User user) {
        var projectUserRoles = new ArrayList<ProjectUserRole>();

        projectUserRoles.add(materializeProjectUserRole(project, company, user, RoleName.PROJECT_OWNER));

        if (userHasRole(user.getId(), RoleName.CUSTOMER)) {
            projectUserRoles.add(materializeProjectUserRole(project, company, user, RoleName.PROJECT_CUSTOMER));
        }

        project.setProjectUserRoles( projectUserRoles );
    }

    private ProjectUserRole materializeProjectUserRole(Project project, ProjectCompany company, User user, RoleName roleName) {
	    return projectUserRoleRepository.save(
                ProjectUserRole.builder()
                        .user(user)
                        .project(project)
                        .projectCompany(company)
                        .role(Role.ref(securityHelper.roleId(roleName)))
                        .build()
        );
    }

    private boolean userHasRole(Integer userId, RoleName role) {
        return companyUserRoleRepository.findByUser(userId).stream()
                .map(cur -> cur.getRole().getName())
                .anyMatch( n -> n.equals(role.name()));
    }
    
    private CustomRoleDTO validateProjectUserRole(Integer roleId) {
        CustomRoleDTO result = roleReader.getRoleById(roleId);

        if (!result.isProjectRole()) {
            throw CustomRoleExceptions.roleIsNotProjectRole(result.getName());
        }

        return result;
    }

    private Project getValidProject(Integer projectId) {
	    return getValidProject(projectId, false);
    }

    public Project getValidProject(Integer projectId, boolean canBeDeleted) {
	    var existingProject = projectRepository.findById(projectId)
            .orElseThrow(() -> ProjectException.projectNotFound(projectId));

	    if ((!canBeDeleted) && (existingProject.getDisabled() != null)) {
	        throw ProjectException.projectNotFound(projectId);
        }

	    return existingProject;
    }
    // Events

    @EventListener
    @Transactional
    public void onApplicationEvent(ContractStateChangeEvent event) {
        switch(event.getEvent()) {
            case OFFER_SELF_ACCEPTED:
            case OFFER_MADE:
                addCustomerToProjectIfNeeded(event.getProjectId(), event.getRecipientCompanyId()); break;
            case INVITATION_SENT:
                addContractorToProjectIfNeeded(event.getProjectId(), event.getRecipientCompanyId(), event.getContractTaskIds()); break;
            case OFFER_ACCEPTED:
                announceContractContractorCompany(event.getProjectId(), event.getRecipientCompanyId(), event.getContractTaskIds()); break;
            default:
                // do nothing
        }
    }

    private void addCustomerToProjectIfNeeded(Integer projectId, Integer customerCompanyId) {
        var projectCustomers = ValidationUtils.throwIf(
                    projectUserRoleRepository.findProjectUsersWithRole(projectId, RoleName.PROJECT_CUSTOMER.name()),
                    l -> l.stream().anyMatch(pur -> !pur.getProjectCompany().getCompany().getId().equals(customerCompanyId)),
                    l -> ProjectException.projectAlreadyHasCustomerSet()
        );

        if (projectCustomers.isEmpty()) {
            addCompanyToProjectAndAddOwner(
                    projectId,
                    customerCompanyId,
                    roleReader.getRoleByName(RoleName.PROJECT_CUSTOMER.name()).getId()
            );
        }
    }

    private void addContractorToProjectIfNeeded(
            Integer projectId,
            Integer contractorCompanyId,
            List<Integer> contractTaskIds
    ) {
        var projectContractors = projectUserRoleRepository.findProjectUsersWithRole(projectId, RoleName.PROJECT_MANAGER.name())
                .stream().filter(pur -> pur.getProjectCompany().getCompany().getId().equals(contractorCompanyId))
                .collect(Collectors.toList());

        if (projectContractors.isEmpty()) {
            addCompanyToProjectAndAddOwner(
                    projectId,
                    contractorCompanyId,
                    roleReader.getRoleByName(RoleName.PROJECT_MANAGER.name()).getId()
            );
        }

        announceContractContractorCompany(projectId, contractorCompanyId, contractTaskIds);
    }

    private void announceContractContractorCompany(Integer projectId, Integer contractorCompanyId, List<Integer> contractTaskIds) {
        eventPublisher.publishEvent(new ProjectContractorCompanyEvent(
                projectId,
                contractorCompanyId,
                contractTaskIds
        ));
    }
}
