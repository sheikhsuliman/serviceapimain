package com.siryus.swisscon.api.project.project;

import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.auth.permission.Permission;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.auth.user.UserService;
import com.siryus.swisscon.api.base.PageableUtil;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyDirectoryDTO;
import com.siryus.swisscon.api.company.company.SimpleCompanyDTO;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleService;
import com.siryus.swisscon.api.contract.ContractPublicService;
import com.siryus.swisscon.api.file.file.FileService;
import com.siryus.swisscon.api.general.country.CountryService;
import com.siryus.swisscon.api.general.favorite.FavoriteService;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompany;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompanyService;
import com.siryus.swisscon.api.project.projecttype.ProjectTypeService;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleService;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.soa.EventsEmitter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@RestController("projectController")
@Api(
        tags = "Projects"
)
public class ProjectController {

    private static final int MINIMUM_PAGE_SIZE = 100;
    private static final String DEFAULT_ACTIVE_PROJECT_SORT = "name";
    private static final String DEFAULT_ARCHIVED_PROJECT_SORT = "-disabled";

    private final ProjectService projectService;
    private final ProjectReader projectReader;
    private final ProjectTypeService projectTypeService;
    private final FavoriteService favoriteService;
    private final CountryService countryService;
    private final FileService fileService;
    private final UserService userService;
    private final ProjectCompanyService projectCompanyService;
    private final CompanyUserRoleService companyUserRoleService;
    private final ProjectUserRoleService projectUserRoleService;
    private final ContractPublicService contractPublicService;
    private final SecurityHelper securityHelper;
    private final EventsEmitter eventsEmitter;

    @Autowired
    public ProjectController(
            ProjectService projectService,
            ProjectReader projectReader,
            ProjectTypeService projectTypeService,
            FavoriteService favoriteService,
            CountryService countryService,
            FileService fileService,
            UserService userService,
            ProjectCompanyService projectCompanyService,
            CompanyUserRoleService companyUserRoleService,
            ProjectUserRoleService projectUserRoleService,
            ContractPublicService contractPublicService,
            SecurityHelper securityHelper,
            EventsEmitter eventsEmitter) {
        this.projectService = projectService;
        this.projectReader = projectReader;
        this.projectTypeService = projectTypeService;
        this.favoriteService = favoriteService;
        this.countryService = countryService;
        this.fileService = fileService;
        this.userService = userService;
        this.projectCompanyService = projectCompanyService;
        this.companyUserRoleService = companyUserRoleService;
        this.projectUserRoleService = projectUserRoleService;
        this.contractPublicService = contractPublicService;
        this.securityHelper = securityHelper;
        this.eventsEmitter = eventsEmitter;
    }

    @GetMapping(value="/api/rest/projects")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(
            value = "Search for resources (paginated).",
            notes = "Find all projects paginated. Properties are _pn (page number), _ps (page size) and sort."
    )
    //@PreAuthorize("hasPermission(0, 'com.siryus.swisscon.api.company.company.Company', 'PROJECT_READ_LIST')")
    // TODO Project List > is a permission on company level, not on project level. Also it could be possible to remove permissions completely because all users can load their projects
    public Page<ProjectDTO> getProjects(
            @ApiParam(name = "_pn", value = "The page number", allowableValues = "range[0, infinity]", defaultValue = "0")
            @RequestParam(value = "_pn", required = false, defaultValue = "0") Integer page,
            @ApiParam(name = "_ps", value = "The page size", allowableValues = "range[1, infinity]")
            @RequestParam(value = "_ps", required = false, defaultValue = "100") Integer size,
            @ApiParam(name = "sort", value = "Comma separated list of attribute names, descending for each one prefixed with a dash, ascending otherwise")
            @RequestParam(value = "sort", required = false, defaultValue = "name") String sort) {

        Pageable pageable = PageableUtil.buildPageable(
                page, Math.max(size, MINIMUM_PAGE_SIZE), DEFAULT_ACTIVE_PROJECT_SORT
        );
        return projectService.findProjectsPaginated(pageable);
    }

    @GetMapping(value="/api/rest/archived-projects")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(
            value = "Search for all archived projects (paginated)."
    )
    //@PreAuthorize("hasPermission(0, 'com.siryus.swisscon.api.company.company.Company', 'PROJECT_READ_LIST')")
    // TODO Project List > is a permission on company level, not on project level. Also it could be possible to remove permissions completely because all users can load their projects
    public Page<ProjectDTO> getArchivedProjects(
            @ApiParam(name = "_pn", value = "The page number", allowableValues = "range[0, infinity]", defaultValue = "0")
            @RequestParam(value = "_pn", required = false, defaultValue = "0") Integer page,
            @ApiParam(name = "_ps", value = "The page size", allowableValues = "range[1, infinity]")
            @RequestParam(value = "_ps", required = false, defaultValue = "100") Integer size,
            @ApiParam(name = "sort", value = "Comma separated list of attribute names, descending for each one prefixed with a dash, ascending otherwise")
            @RequestParam(value = "sort", required = false, defaultValue = "disabled") String sort) {

        Pageable pageable = PageableUtil.buildPageable(
                page, Math.max(size, MINIMUM_PAGE_SIZE), DEFAULT_ARCHIVED_PROJECT_SORT
        );
        return projectService.getArchivedProjectsPaginated(pageable);
    }

    @GetMapping(value="/api/rest/projects/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#id, 'PROJECT', 'PROJECT_READ_DETAILS')")
    public ProjectDTO getProject(@ApiParam(name = "id", required = true, value = "string") @PathVariable Integer id) {
        final Project project = projectService.findActiveProjectById(id);
        return projectService.dtoFrom(project, Integer.parseInt(LecwUtils.currentUser().getId()));
    }

    @Deprecated
    @PostMapping(value="/api/rest/projects/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Archive the project")
    @PreAuthorize("hasPermission(#id, 'PROJECT', 'PROJECT_ARCHIVE')")
    public void deleteProject(@ApiParam(name = "id", required = true, value = "string") @PathVariable Integer id) {
        projectService.archiveProject(id);
    }

    @PostMapping(value="/api/rest/projects/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Archive the project")
    @PreAuthorize("hasPermission(#id, 'PROJECT', 'PROJECT_ARCHIVE')")
    public void archiveProject(@ApiParam(name = "id", required = true, value = "string") @PathVariable Integer id) {
        projectService.archiveProject(id);
    }

    @PostMapping(value="/api/rest/projects/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Restore the project")
    @PreAuthorize("hasPermission(0, 'COMPANY', 'COMPANY_PROJECT_RESTORE')")
    public void restoreProject(@ApiParam(name = "id", required = true, value = "string") @PathVariable Integer id) {
        projectService.restoreProject(id);
    }

    @GetMapping(value="/api/rest/project-board/{id}", name = "getProject")
    @ApiOperation(value = "Loads data for a project", notes = "Loads data for a project based on its identifier. ", httpMethod = "GET")
    @PreAuthorize("hasPermission(#id, 'PROJECT', 'PROJECT_READ_DETAILS')")
    public ProjectBoardDTO get(@ApiParam(name = "id", required = true, value = "string") @PathVariable Integer id) {
        Project project = projectService.findActiveProjectById(id);

        Integer userId = Integer.valueOf(LecwUtils.currentUser().getId());
        Boolean starred = favoriteService.userStarred(userId, project.getId(), ReferenceType.PROJECT);

        return ProjectBoardDTO.from(project,
                projectService.getProjectOwnerCompanyId(project),
                securityHelper.getProjectCustomerCompanyId(id),
                starred,
                contractPublicService.projectCustomerIsReassignable(project.getId()));
    }

    @PostMapping(value = "/api/rest/projects/create")
    @PreAuthorize("hasPermission(0, 'COMPANY', 'COMPANY_PROJECT_CREATE')")
    public ProjectBoardDTO createProject(
            @RequestBody @Valid @NotNull NewProjectDTO projectData
    ) {
        return projectService.createProject(projectData);
    }

    @PostMapping("/api/rest/projects/{id}/edit")
    @ApiOperation(value = "Edit project properties")
    @PreAuthorize("hasPermission(#id, 'PROJECT', 'PROJECT_UPDATE')")
    public ProjectBoardDTO editProject(
            @ApiParam(name = "id", required = true, value = "string") @PathVariable Integer id,
            @RequestBody @Valid @NotNull EditProjectDTO editProjectDTO) {
        return projectService.editProject(id, editProjectDTO,
                contractPublicService.projectCustomerIsReassignable(id) );
    }

    @GetMapping("/api/rest/projects/{id}/companies")
    @ApiOperation(value = "Get all the companies which are assigned to the project")
    @PreAuthorize("hasPermission(#id, 'PROJECT', 'PROJECT_READ_DETAILS')")
    public List<CompanyDirectoryDTO> getProjectCompanies(
            @ApiParam(name = "id", required = true, value = "string") @PathVariable Integer id) {

        projectReader.validateProjectId(id);

        List<ProjectCompany> activeCompanies = projectCompanyService.findByProjectAndCompanyActive(id);
        List<Integer> companyIds = activeCompanies.stream().map(pc -> pc.getCompany().getId()).collect(Collectors.toList());

        List<CompanyUserRole> companyOwners = companyUserRoleService.getCompanyOwners(companyIds);

        return companyOwners
                .stream()
                .map(CompanyDirectoryDTO::from)
                .collect(Collectors.toList());
    }

    @GetMapping(value = {"/api/rest/projects/{id}/team"})
    @ApiOperation(value = "Team of a company involved in a project", notes = "Get the team of a company which is selected for a given project", httpMethod = "GET")
    @PreAuthorize("hasPermission(#id, 'PROJECT', 'PROJECT_READ_DETAILS')")
    public List<TeamUserDTO> getProjectTeamByCompany(
            @ApiParam(name = "id", required = true, value = "string") @PathVariable Integer id,
            @RequestParam(value = "company") Integer companyId) {
        return projectUserRoleService.getProjectTeamByCompany(id, companyId);
    }

    @GetMapping("/api/rest/projects/{id}/available-companies")
    @ApiOperation(value = "Show all the companies which are not assigned to the project yet")
    @PreAuthorize("hasPermission(#id, 'PROJECT', 'PROJECT_TEAM_ADD_COMPANY')")
    public List<SimpleCompanyDTO> availableCompaniesToAdd(
            @ApiParam(name = "id", required = true, value = "string") @PathVariable Integer id) {

        projectReader.validateProjectId(id);

        List<Company> companiesNotInProject = projectCompanyService.findCompaniesNotInProject(id);

        return companiesNotInProject.stream()
                .map(SimpleCompanyDTO::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/rest/projects/{id}/available-users")
    @ApiOperation(value = "Show all the users which are not assigned to the project yet (only the users of the signed in user's company")
    @PreAuthorize("hasPermission(#id, 'PROJECT', 'PROJECT_TEAM_ADD_USER')")
    public List<TeamUserDTO> availableUsersToAdd(
            @ApiParam(name = "id", required = true, value = "string") @PathVariable Integer id) {

        projectReader.validateProjectId(id);

        List<CompanyUserRole> availableUsers = projectUserRoleService.findCompanyUsersNotInProject(id);

        return availableUsers.stream()
                .map(TeamUserDTO::fromCompanyUserRole)
                .collect(Collectors.toList());
    }

    @PostMapping("/api/rest/projects/{id}/add-company")
    @ApiOperation(value = "Add a new company to the team (company owner will be project manager, but not project owner")
    @PreAuthorize("hasPermission(#id, 'PROJECT', 'PROJECT_TEAM_ADD_COMPANY')")
    public CompanyDirectoryDTO addCompanyWithRole(
            @ApiParam(name = "id", required = true, value = "string") @PathVariable Integer id,
            @RequestParam(value = "company") Integer companyId,
            @RequestParam(value = "roleId", required = false) Integer roleId) {

        return projectService.addCompanyToProjectAndAddOwner(id, companyId, roleId);
    }

    /**
     * This controller method adds a user identified by userId to a project identified by projectId.
     * NOTE: The user will get the PROJECT_WORKER role by default
     *
     * @param id project ID
     * @param userId The id of the user to be added
     * @return Team User DTO
     */
    @PostMapping("/api/rest/projects/{id}/add-user")
    @ApiOperation(value = "Adds a user from a company to an existing project", httpMethod = "POST")
    public TeamUserDTO addUserToProject(@PathVariable Integer id,
                                                        @RequestParam(value="user") Integer userId) {

        return TeamUserDTO.fromProjectUserRole(projectUserRoleService.addCompanyUserToProject(userId, id));
    }

    @PostMapping("/api/rest/projects/{id}/remove-user")
    @ApiOperation(value = "Remove a user from the project company team")
    @PreAuthorize("hasPermission(#id, 'PROJECT', 'PROJECT_TEAM_ARCHIVE_USER')")
    public void removeUser(
            @ApiParam(name = "id", required = true, value = "string") @PathVariable Integer id,
            @RequestParam(value = "user") Integer userId
    ) {
        projectReader.validateProjectId(id);
        userService.validateUserId(userId);

        projectUserRoleService.deleteUserFromProjectTeam(id, userId);
    }

    @PostMapping("/api/rest/projects/{id}/remove-company")
    @ApiOperation(value = "Remove a company from a project (only possible if no tasks are assigned to the users)")
    @PreAuthorize("hasPermission(#id, 'PROJECT', 'PROJECT_TEAM_ARCHIVE_COMPANY')")
    public void removeCompany(
            @ApiParam(name = "id", required = true, value = "string") @PathVariable Integer id,
            @RequestParam(value = "company") Integer companyId
    ) {
        projectCompanyService.removeCompanyFromProject(id, companyId);
    }

    @PostMapping("/api/rest/projects/{projectId}/change-admin-role")
    @ApiOperation(value = "Change to any role (target role or existing role have to be admin) (only for project owners)")
    public TeamUserDTO changeAdminRole(
            @ApiParam(name = "projectId", required = true, value = "string") @PathVariable Integer projectId,
            @RequestParam(value = "user") Integer userId,
            @RequestParam(value = "role") Integer roleId

    ) {
        //TODO permission system > you need the permission PROJECT_TEAM_CHANGE_ADMIN_ROLE

        ProjectUserRole projectUserRole = projectUserRoleService.changeAdminRole(projectId, userId, roleId);
        return TeamUserDTO.fromProjectUserRole(projectUserRole);
    }

    @PostMapping("/api/rest/projects/{id}/change-non-admin-role")
    @ApiOperation(value = "Change to any role (target role or existing role cannot be admin)(it has to be in the logged in users company)")
    public TeamUserDTO changeNonAdminRole(
            @ApiParam(name = "id", required = true, value = "string") @PathVariable Integer id,
            @RequestParam(value = "user") Integer userId,
            @RequestParam(value = "role") Integer roleId

    ) {
        //TODO permission system > you need the permission PROJECT_TEAM_CHANGE_NON_ADMIN_ROLE
        ProjectUserRole projectUserRole = projectUserRoleService.changeNonAdminRole(id, userId, roleId);
        return TeamUserDTO.fromProjectUserRole(projectUserRole);
    }

    @GetMapping("/api/rest/projects/{projectId}/permissions")
    @ApiOperation(value = "Get a list of all permissions the user has for this particular project")
    public List<Integer> getPermissions(
            @ApiParam(name = "projectId", required = true, value = "string") @PathVariable Integer projectId
    ) {
        final Integer userId = Integer.valueOf(LecwUtils.currentUser().getId());
        return this.projectService.loadUserPermissions(userId, projectId)
                .stream()
                .map(Permission::getId).collect(Collectors.toList());
    }

    @PostMapping("/api/rest/projects/{projectId}/customer/{customerCompanyId}")
    public void assignCustomerToProject(@PathVariable Integer projectId, @PathVariable Integer customerCompanyId) {
        projectService.assignCustomerToProject(projectId, customerCompanyId);
    }
}
