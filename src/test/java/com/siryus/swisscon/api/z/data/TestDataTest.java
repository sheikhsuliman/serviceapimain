package com.siryus.swisscon.api.z.data;

import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileRepository;
import com.siryus.swisscon.api.general.reference.ReferenceService;
import com.siryus.swisscon.api.project.project.Project;
import com.siryus.swisscon.api.project.project.ProjectRepository;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompany;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompanyRepository;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleRepository;
import com.siryus.swisscon.api.tasks.entity.MainTaskEntity;
import com.siryus.swisscon.api.tasks.entity.SubTaskEntity;
import com.siryus.swisscon.api.tasks.entity.SubTaskUserEntity;
import com.siryus.swisscon.api.tasks.repos.MainTaskRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskRepository;
import com.siryus.swisscon.api.tasks.repos.SubTaskUserRepository;
import com.siryus.swisscon.api.taskworklog.entity.TaskWorklogEntity;
import com.siryus.swisscon.api.taskworklog.repos.TaskWorkLogRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Other then the {@link com.siryus.swisscon.api.base.AbstractMvcTestBase} This tests uses the real flyway scripts of the production environment
 * This is to test if the provided test data makes sense and can be used without issues
 *
 * The package is named z.** so that the test can run at the end because the spring context isn't freshly loaded after this test
 * TODO find a better solution to reload the spring context after this test than execute the test at the end
 */
public class TestDataTest extends AbstractMvcTestBase  {

    private final ProjectRepository projectRepository;
    private final ProjectCompanyRepository projectCompanyRepository;
    private final CompanyRepository companyRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final SubTaskUserRepository subTaskUserRepository;
    private final MainTaskRepository mainTaskRepository;
    private final SubTaskRepository subTaskRepository;
    private final TaskWorkLogRepository taskWorkLogRepository;
    private final ReferenceService referenceService;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final RoleRepository roleRepository;
    private final Flyway flyway;

    @Autowired
    public TestDataTest(
            ProjectRepository projectRepository,
            ProjectCompanyRepository projectCompanyRepository,
            CompanyRepository companyRepository,
            CompanyUserRoleRepository companyUserRoleRepository,
            ProjectUserRoleRepository projectUserRoleRepository,
            SubTaskUserRepository subTaskUserRepository,
            MainTaskRepository mainTaskRepository,
            SubTaskRepository subTaskRepository,
            TaskWorkLogRepository taskWorkLogRepository,
            ReferenceService referenceService,
            UserRepository userRepository,
            FileRepository fileRepository,
            RoleRepository roleRepository,
            Flyway flyway
    ) {
        this.projectRepository = projectRepository;
        this.projectCompanyRepository = projectCompanyRepository;
        this.companyRepository = companyRepository;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.projectUserRoleRepository = projectUserRoleRepository;
        this.subTaskUserRepository = subTaskUserRepository;
        this.mainTaskRepository = mainTaskRepository;
        this.subTaskRepository = subTaskRepository;
        this.taskWorkLogRepository = taskWorkLogRepository;
        this.referenceService = referenceService;
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.roleRepository = roleRepository;
        this.flyway = flyway;
    }
    @Override
    protected Flyway customizeFlyWay(Flyway flyway) {
        return Flyway.configure().configuration(flyway.getConfiguration())
                .locations(
                        "classpath:db/migrations/common",
                        "classpath:db/migrations/data/common",
                        "classpath:db/migrations/data/dev"
                )
                .load();
    }

    @Override
    protected boolean doMockLogin() {
        return false;
    }

    @Test
    void testAllProjectsHaveACompanyAssigned() {
        List<Project> allProjects = projectRepository.findAll();

        assertFalse(allProjects.isEmpty());
        allProjects.forEach(p->assertFalse(projectCompanyRepository.findActiveByProject(p.getId()).isEmpty()));
    }

    @Test
    void testAllCompaniesHaveOneCompanyOwner() {
        List<Company> allCompanies = companyRepository.findAll();
        assertFalse(allCompanies.isEmpty());

        allCompanies.forEach(c->{
            List<CompanyUserRole> companyUserRoles = companyUserRoleRepository.findCompanyUsersRoleByCompany(c.getId());

            boolean isBootstrapCompany = companyUserRoles
                    .stream()
                    .anyMatch(cur -> cur.getRole().getName().equals(RoleName.COMPANY_BOOTSTRAP.toString()));

            if(!isBootstrapCompany) {
                List<CompanyUserRole> companyOwners = companyUserRoles
                        .stream()
                        .filter(cur -> roleIsOneOf(cur.getRole().getName(), RoleName.COMPANY_OWNER, RoleName.CUSTOMER))
                        .collect(Collectors.toList());
                assertEquals(1, companyOwners.size(), "per company we need exactly one company owner");
            }
        });
    }

    boolean roleIsOneOf(String actualRoleName, RoleName... expectedRoles) {
        return Arrays.asList(expectedRoles).stream().anyMatch( r -> r.name().equals(actualRoleName));
    }

    @Test
    void testAllProjectsHaveOneProjectOwner() {
        List<Project> allProjects = projectRepository.findAll();
        assertFalse(allProjects.isEmpty());

        allProjects.forEach(p -> {
            List<ProjectUserRole> projectUserRoles = projectUserRoleRepository.findProjectTeam(p.getId());
            List<ProjectUserRole> projectOwners = projectUserRoles
                    .stream()
                    .filter(pur -> RoleName.PROJECT_OWNER.toString().equals(pur.getRole().getName()))
                    .collect(Collectors.toList());
            assertEquals(1, projectOwners.size(), "per project we need exactly one project owner");
        });
    }

    @Test
    void testAllProjectsCompanyTeamsHaveManagerAndWorker() {
        List<ProjectCompany> allProjectCompanies = projectCompanyRepository.findAll();
        assertFalse(allProjectCompanies.isEmpty());

        allProjectCompanies.forEach(pc-> {
            List<ProjectUserRole> projectUserRoles = projectUserRoleRepository.findProjectCompanyTeam(pc.getId());
            assertFalse(projectUserRoles.isEmpty(), "every project company team needs to have team users");

            boolean hasProjectManager = projectUserRoles.stream()
                    .anyMatch(pur -> RoleName.PROJECT_MANAGER.toString().equals(pur.getRole().getName()));

            boolean hasProjectWorker = projectUserRoles.stream()
                    .anyMatch(pur -> RoleName.PROJECT_WORKER.toString().equals(pur.getRole().getName()));
            if(!hasProjectManager || !hasProjectWorker) {
                System.out.print("x");
            }

            assertTrue(hasProjectManager, "per project company we need at least one project manager");
            assertTrue(hasProjectWorker, "per project company team we need at least one project worker");

        });
    }

    @Test
    void testProjectCompanyTeamUserIsPartOfCompany() {
        List<ProjectCompany> allProjectCompanies = projectCompanyRepository.findAll();
        assertFalse(allProjectCompanies.isEmpty());

        allProjectCompanies.forEach(pc-> {
            List<ProjectUserRole> projectUserRoles = projectUserRoleRepository.findProjectCompanyTeam(pc.getId());
            assertFalse(projectUserRoles.isEmpty(), "every project company team needs to have team users");

            boolean hasCompanyUser = projectUserRoles.stream()
                    .anyMatch(pur -> !companyUserRoleRepository.findByUserAndCompany(pur.getUser().getId(), pc.getCompany().getId()).isEmpty());

            assertTrue(hasCompanyUser, "every project user should belong the right company");
        });
    }

    @Test
    void testTaskUserBelongsToProjectAndCompany() {
        List<SubTaskUserEntity> subTaskUsers = subTaskUserRepository.findAll();

        subTaskUsers.forEach(stu-> {
            Optional<SubTaskEntity> subTaskOpt = subTaskRepository.findById(stu.getSubTask().getId());
            assertTrue(subTaskOpt.isPresent());

            Optional<MainTaskEntity> mainTaskOpt = mainTaskRepository.findById(subTaskOpt.get().getMainTask().getId());
            assertTrue(mainTaskOpt.isPresent());

            Integer projectId = mainTaskOpt.get().getProjectId();
            Integer companyId = mainTaskOpt.get().getSpecification().getCompany().getId();

            List<ProjectUserRole> projectUserRoles = projectUserRoleRepository.findByUserAndProject(stu.getUser().getId(), projectId);

            assertFalse(projectUserRoles.isEmpty(), "the task user should be part of the project");

            ProjectCompany projectCompany = projectCompanyRepository.findByProjectAndCompany(projectId, companyId);
            assertNotNull(projectCompany, "company of the user should be part of the project");

            List<CompanyUserRole> companyUserRoles = companyUserRoleRepository.findByUserAndCompany(stu.getUser().getId(), companyId);

            assertFalse(companyUserRoles.isEmpty(), "task user should be part of the company of the specification");
        });
    }

    @Test
    void testAllFilesHaveValidReferences() {
        List<File> allFiles = fileRepository.findAll();

        assertFalse(allFiles.isEmpty());

        allFiles.stream().filter( f -> f.getId() % 100 == 0).forEach(f-> referenceService.validateForeignKey(f.getReferenceType(), f.getReferenceId()));
    }

    @Test
    void testTaskWorkLogUserIsPartOfTask() {
        List<TaskWorklogEntity> allTaskWorkLogs = taskWorkLogRepository.findAll();
        assertFalse(allTaskWorkLogs.isEmpty());

        allTaskWorkLogs.forEach(tw-> {
            Integer subTaskWorkLogId = tw.getSubTask().getId();
            Optional<SubTaskUserEntity> subTaskUserOpt = subTaskUserRepository.findByUserAndSubTask(tw.getWorker().getId(), subTaskWorkLogId);
            assertTrue(subTaskUserOpt.isPresent(), "User of worklog has to be part of the task team");
        });
    }

    @Test
    void testUserHaveNoDuplicateMails() {
        List<User> allUsers = userRepository.findAll();

        for(User user : allUsers) {
            int userWithSameMailSize = (int) allUsers.stream().filter(u -> u.getEmail().equals(user.getEmail())).count();
            assertEquals(1, userWithSameMailSize);
        }

    }

    @Test
    void testNumberOfBootstrapUsersIsOne() {
        List<CompanyUserRole> companyUserRoles = companyUserRoleRepository.findAll();
        Role companyBootstrapUser = roleRepository.getRoleByName(RoleName.COMPANY_BOOTSTRAP.toString());
        long numberOfBootstrapUsers = companyUserRoles.stream().filter(cur -> cur.getRole().getId().equals(companyBootstrapUser.getId())).count();
        assertEquals(1, numberOfBootstrapUsers, "There should be exactly one bootstrap user");
    }

}
