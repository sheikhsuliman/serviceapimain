package com.siryus.swisscon.api.base;

import com.naturalprogrammer.spring.lemon.commons.domain.ResetPasswordForm;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.signup.SignupDTO;
import com.siryus.swisscon.api.auth.signup.SignupResponseDTO;
import com.siryus.swisscon.api.auth.user.CompanyInviteDTO;
import com.siryus.swisscon.api.auth.user.TeamUserAddDTO;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.auth.user.UserProfileDTO;
import com.siryus.swisscon.api.base.helpers.Auth;
import com.siryus.swisscon.api.base.helpers.Catalog;
import com.siryus.swisscon.api.base.helpers.Companies;
import com.siryus.swisscon.api.base.helpers.Contract;
import com.siryus.swisscon.api.base.helpers.CustomRoles;
import com.siryus.swisscon.api.base.helpers.Locations;
import com.siryus.swisscon.api.base.helpers.Media;
import com.siryus.swisscon.api.base.helpers.Projects;
import com.siryus.swisscon.api.base.helpers.Tasks;
import com.siryus.swisscon.api.base.helpers.WorkLogs;
import com.siryus.swisscon.api.catalog.dto.CatalogImportReportDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogNodeDTO;
import com.siryus.swisscon.api.company.bankaccount.BankAccountDTO;
import com.siryus.swisscon.api.company.company.CompanyDetailsDTO;
import com.siryus.swisscon.api.company.company.CompanyDirectoryDTO;
import com.siryus.swisscon.api.company.company.SimpleCompanyDTO;
import com.siryus.swisscon.api.contract.dto.ContractAddTasksRequest;
import com.siryus.swisscon.api.contract.dto.ContractCommentDTO;
import com.siryus.swisscon.api.contract.dto.ContractDTO;
import com.siryus.swisscon.api.contract.dto.ContractEventLogDTO;
import com.siryus.swisscon.api.contract.dto.ContractSummaryDTO;
import com.siryus.swisscon.api.contract.dto.ContractTaskDTO;
import com.siryus.swisscon.api.contract.dto.ContractUpdateTaskRequest;
import com.siryus.swisscon.api.contract.dto.CreateContractCommentRequest;
import com.siryus.swisscon.api.contract.dto.CreateContractRequest;
import com.siryus.swisscon.api.contract.dto.ListContractsRequest;
import com.siryus.swisscon.api.contract.dto.SendMessageRequest;
import com.siryus.swisscon.api.contract.dto.UpdateContractRequest;
import com.siryus.swisscon.api.customroles.dto.CustomPermissionDTO;
import com.siryus.swisscon.api.customroles.dto.CustomRoleDTO;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.init.InitDTO;
import com.siryus.swisscon.api.location.location.LocationCreateDTO;
import com.siryus.swisscon.api.location.location.LocationDetailsDTO;
import com.siryus.swisscon.api.location.location.LocationTreeDTO;
import com.siryus.swisscon.api.location.location.LocationUpdateDTO;
import com.siryus.swisscon.api.mediawidget.MediaWidgetFileDTO;
import com.siryus.swisscon.api.project.project.EditProjectDTO;
import com.siryus.swisscon.api.project.project.NewProjectDTO;
import com.siryus.swisscon.api.project.project.ProjectBoardDTO;
import com.siryus.swisscon.api.project.project.ProjectDTO;
import com.siryus.swisscon.api.tasks.dto.AddTaskChecklistItemRequest;
import com.siryus.swisscon.api.tasks.dto.AssignCompanyToTaskRequest;
import com.siryus.swisscon.api.tasks.dto.CommentDTO;
import com.siryus.swisscon.api.tasks.dto.CreateCommentDTO;
import com.siryus.swisscon.api.tasks.dto.CreateContractualTaskRequest;
import com.siryus.swisscon.api.tasks.dto.CreateDirectorialTaskRequest;
import com.siryus.swisscon.api.tasks.dto.CreateSubTaskRequest;
import com.siryus.swisscon.api.tasks.dto.IdResponse;
import com.siryus.swisscon.api.tasks.dto.ListTaskIdsRequest;
import com.siryus.swisscon.api.tasks.dto.ListTasksRequest;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import com.siryus.swisscon.api.tasks.dto.SubTaskDTO;
import com.siryus.swisscon.api.tasks.dto.TaskChecklistItem;
import com.siryus.swisscon.api.tasks.dto.UpdateDirectorialTaskRequest;
import com.siryus.swisscon.api.taskworklog.dto.EventHistoryDTO;
import com.siryus.swisscon.api.taskworklog.dto.MainTaskDurationDTO;
import com.siryus.swisscon.api.taskworklog.dto.RejectTaskRequest;
import com.siryus.swisscon.api.taskworklog.dto.SubTaskDurationDTO;
import com.siryus.swisscon.api.taskworklog.dto.TaskStatusDTO;
import com.siryus.swisscon.api.taskworklog.dto.TaskWorklogRequest;
import com.siryus.swisscon.api.taskworklog.dto.WorkLogEventType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.siryus.swisscon.api.base.AbstractMvcTestBase.endPoint;
import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;

@SuppressWarnings({"WeakerAccess, UnusedReturnValue, unused"})
public class TestHelper {

    public static final String COMPANY_INVITATION_MAIL = "invited_company@siryus.com";
    public static final String PASSWORD = "Siryus2020";
    public static final String COMPANY_NAME = "Build Anything Anywhere";

    public static final String TEST_LANGUAGE = "en_US";

    public static final String PROJECT_OWNER_FIRST_NAME = "Project";
    public static final String PROJECT_OWNER_LAST_NAME = "Owner";
    public static final String PROJECT_OWNER_EMAIL = companyEmail(COMPANY_NAME, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME);
    public static final Integer PROJECT_OWNER_COUNTRY_CODE = 44;
    public static final String PROJECT_OWNER_PHONE = "733361724";
    public static final String PROJECT_OWNER_FULL_PHONE = PROJECT_OWNER_COUNTRY_CODE + PROJECT_OWNER_PHONE;

    public static final String PROJECT_ADMIN_FIRST_NAME = "Project";
    public static final String PROJECT_ADMIN_LAST_NAME = "Admin";
    public static final String PROJECT_ADMIN_EMAIL = companyEmail(COMPANY_NAME, PROJECT_ADMIN_FIRST_NAME, PROJECT_ADMIN_LAST_NAME);

    public static final String CONTRACTOR_COMPANY_NAME = "Paint R US";
    public static final String PROJECT_MANAGER_FIRST_NAME = "Project";
    public static final String PROJECT_MANAGER_LAST_NAME = "Manager";
    public static final String PROJECT_MANAGER_EMAIL = companyEmail(CONTRACTOR_COMPANY_NAME, PROJECT_MANAGER_FIRST_NAME, PROJECT_MANAGER_LAST_NAME);

    public static final String PROJECT_WORKER_FIRST_NAME = "Project";
    public static final String PROJECT_WORKER_LAST_NAME = "Worker";
    public static final String PROJECT_WORKER_EMAIL = companyEmail(CONTRACTOR_COMPANY_NAME, PROJECT_WORKER_FIRST_NAME, PROJECT_WORKER_LAST_NAME);

    public static final String ANOTHER_PROJECT_WORKER_FIRST_NAME = "Another";
    public static final String ANOTHER_PROJECT_WORKER_LAST_NAME = "Worker";
    public static final String ANOTHER_PROJECT_WORKER_EMAIL = companyEmail(CONTRACTOR_COMPANY_NAME, ANOTHER_PROJECT_WORKER_FIRST_NAME, ANOTHER_PROJECT_WORKER_LAST_NAME);

    public static final String ANOTHER_COMPANY = "Another";
    public static final String ANOTHER_PROJECT_OWNER_FIRST_NAME = "Another";
    public static final String ANOTHER_PROJECT_OWNER_LAST_NAME = "Owner";
    public static final String ANOTHER_PROJECT_OWNER_EMAIL = companyEmail(ANOTHER_COMPANY, ANOTHER_PROJECT_OWNER_FIRST_NAME, ANOTHER_PROJECT_OWNER_LAST_NAME);

    public static final String CUSTOMER_COMPANY_NAME ="Customer Company";
    public static final String CUSTOMER_FIRST_NAME = "Project";
    public static final String CUSTOMER_LAST_NAME = "Customer";
    public static final String CUSTOMER_EMAIL = companyEmail(CUSTOMER_LAST_NAME, CUSTOMER_FIRST_NAME, CUSTOMER_LAST_NAME);
    public static final Integer CUSTOMER_COUNTRY_CODE = 49;
    public static final String CUSTOMER_MOBILE = "522468798";


    private final ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

    private final AbstractMvcTestBase testBase;
    private final Auth auth;
    private final Companies companies;
    private final Projects projects;
    private final Locations locations;
    private final Tasks tasks;
    private final WorkLogs workLogs;
    private final Media media;
    private final Catalog catalog;
    private final CustomRoles customRoles;
    private final Contract contract;

    public TestHelper(AbstractMvcTestBase testBase) {
        this.testBase = testBase;
        this.auth = new Auth(
                testBase,
                testBase.lemonService,
                testBase.greenTokenService,
                testBase.extendedTokenService,
                testBase.defaultSignupToken,
                testBase.roleReader
        );
        this.companies = new Companies();
        this.projects = new Projects(testBase);
        this.locations = new Locations();
        this.tasks = new Tasks();
        this.workLogs = new WorkLogs();
        this.media = new Media();
        this.catalog = new Catalog();
        this.customRoles = new CustomRoles();
        this.contract = new Contract(testBase);
    }

    // ========================== Auth

    @Deprecated //TODO remove after SI-177
    public void signUp(SignupDTO request, HttpStatus status) {
        auth.signUp(request, status);
    }

    @Deprecated //TODO remove after SI-177
    public SignupResponseDTO signUpWithRole(SignupDTO request, RoleName roleName) {
        return auth.signUpWithRole(request, roleName);
    }

    @Deprecated //TODO remove after SI-177
    public SignupResponseDTO signUp(SignupDTO request) {
        return auth.signUp(request);
    }

    public SignupResponseDTO signupFromInvite(SignupDTO request) {
        return auth.signupFromInvite(request);
    }

    public SignupResponseDTO signupFromInvite(SignupDTO request, Function<ValidatableResponse, SignupResponseDTO> responseValidator) {
        return auth.signupFromInvite(request, responseValidator);
    }

    public UserProfileDTO updateUserProfile(RequestSpecification spec, UserProfileDTO request) {
        return auth.updateUserProfile(spec, request);
    }

    public UserProfileDTO updateUserProfile(RequestSpecification spec, UserProfileDTO request, Function<ValidatableResponse, UserProfileDTO> responseValidator) {
        return auth.updateUserProfile(spec, request, responseValidator);
    }

    public InitDTO init(RequestSpecification spec) {
        return auth.init(spec);
    }

    public InitDTO init(RequestSpecification spec, Function<ValidatableResponse, InitDTO> responseValidator) {
        return auth.init(spec, responseValidator);
    }

    public void verifyUser(Integer userId, String email) {
        auth.verifyUser(userId, email);
    }

    public RequestSpecification login(String email) {
        return auth.login(email);
    }

    public RequestSpecification login(String email, String password) {
        return auth.login(email, password);
    }

    public void login(String email, String password, Consumer<ValidatableResponse> responseValidator) {
        auth.login(email, password, responseValidator);
    }

    public void logout(RequestSpecification spec) {
        auth.logout(spec);
    }

    public void logout(RequestSpecification spec, Consumer<ValidatableResponse> responseValidator) {
        auth.logout(spec, responseValidator);
    }

    public void resetPassword(RequestSpecification spec, ResetPasswordForm resetPasswordForm) {
        auth.resetPassword(spec, resetPasswordForm);
    }

    public void resetPassword(RequestSpecification spec, ResetPasswordForm resetPasswordForm, Consumer<ValidatableResponse> responseValidator) {
        auth.resetPassword(spec, resetPasswordForm, responseValidator);
    }

    public void resetPasswordWithOwnToken(Integer userId) {
        auth.resetPasswordWithOwnToken(userId);
    }

    public void resetPasswordWithOwnToken(Integer userId, String newPassword) {
        auth.resetPasswordWithOwnToken(userId, newPassword);
    }

    public TeamUserDTO inviteUser(RequestSpecification spec, TeamUserAddDTO request) {
        return auth.inviteUser(spec, request);
    }

    public TeamUserDTO inviteUser(RequestSpecification spec, TeamUserAddDTO request, Function<ValidatableResponse, TeamUserDTO> responseValidator) {
        return auth.inviteUser(spec, request, responseValidator);
    }

    public TeamUserDTO inviteUserAndResetPassword(RequestSpecification spec, TeamUserAddDTO request) {
        return auth.inviteUserAndResetPassword(spec, request);
    }

    public void resendInvitation(RequestSpecification spec, Integer userId) {
        auth.resendInvitation(spec, userId);
    }

    public void resendInvitation(RequestSpecification spec,Integer userId, Consumer<ValidatableResponse> responseValidator) {
        auth.resendInvitation(spec, userId, responseValidator);
    }

    public TeamUserDTO toggleAdmin(RequestSpecification spec, Integer userId, boolean toggleAdmin) {
        return auth.toggleAdmin(spec, userId, toggleAdmin);
    }

    public TeamUserDTO toggleAdmin(RequestSpecification spec, Integer userId, boolean toggleAdmin, Function<ValidatableResponse, TeamUserDTO> responseValidator) {
        return auth.toggleAdmin(spec, userId, toggleAdmin, responseValidator);
    }

    @Deprecated //TODO remove after SI-177
    public void verifySignupCode(String email, String code, HttpStatus status) {
        auth.verifySignupCode(email, code, status);
    }

    public SignupDTO verifySignup(Integer companyId, String code) {
        return auth.verifySignup(companyId, code);
    }

    public SignupDTO verifySignup(Integer companyId, String code, Function<ValidatableResponse, SignupDTO> responseValidator) {
        return auth.verifySignup(companyId, code, responseValidator);
    }

    public void sendForgotPasswordSms(String phoneNumber, HttpStatus status) {
        auth.sendForgotPasswordSms(phoneNumber, status);
    }

    public void sendChangeMobileSms(RequestSpecification spec, Integer mobileCountryCode, String mobile) {
        auth.sendChangeMobileSms(spec, mobileCountryCode, mobile);
    }
    
    public void sendChangeMobileSms(RequestSpecification spec, Integer mobileCountryCode, String mobile, Consumer<ValidatableResponse> responseValidator) {
        auth.sendChangeMobileSms(spec, mobileCountryCode, mobile, responseValidator);
    }    

    public void updateMobile(RequestSpecification spec, String token) {
        auth.updateMobile(spec, token);
    }
    
    public void updateMobile(RequestSpecification spec, String token, Consumer<ValidatableResponse> responseValidator) {
        auth.updateMobile(spec, token, responseValidator);
    }    
    
    public void changeLanguage(RequestSpecification spec, String languageId) {
        auth.changeLanguage(spec, languageId);
    }
    
    public void changeLanguage(RequestSpecification spec, String languageId, Consumer<ValidatableResponse> responseValidator) {
        auth.changeLanguage(spec, languageId, responseValidator);
    }        
    
    // ========================== Companies

    public void removeUserFromCompany(RequestSpecification spec, Integer companyId, Integer userId) {
        companies.removeUserFromCompany(spec, companyId, userId);
    }

    public void removeUserFromCompany(RequestSpecification spec, Integer companyId, Integer userId, Consumer<ValidatableResponse> responseValidator) {
        companies.removeUserFromCompany(spec, companyId, userId, responseValidator);
    }

    @Deprecated //TODO remove after SI-177
    public void inviteCompanyDeprecated(RequestSpecification spec, CompanyInviteDTO companyInviteDTO, HttpStatus httpStatus) {
        companies.inviteCompanyDeprecated(spec, companyInviteDTO, httpStatus);
    }

    @Deprecated //TODO remove after SI-177
    public void inviteCompanyDeprecated(RequestSpecification spec, CompanyInviteDTO companyInviteDTO) {
        companies.inviteCompanyDeprecated(spec, companyInviteDTO, HttpStatus.OK);
    }

    public Integer inviteCompany(RequestSpecification spec, CompanyInviteDTO companyInviteDTO) {
        return companies.inviteCompany(spec, companyInviteDTO);
    }

    public Integer inviteCompany(RequestSpecification spec, CompanyInviteDTO companyInviteDTO, Function<ValidatableResponse, Integer> responseValidator) {
        return companies.inviteCompany(spec, companyInviteDTO, responseValidator);
    }

    public SignupResponseDTO inviteCompanyAndSignup(RequestSpecification spec, CompanyInviteDTO companyInviteDTO) {
        Mockito.doReturn(RandomStringUtils.randomAlphabetic(5))
                .when(testBase.emailLinkUtil)
                .getSignupLink(any(), tokenCaptor.capture());
        Integer companyId = companies.inviteCompany(spec, companyInviteDTO);
        return signupFromInvite(TestBuilder.testSignupDTO(companyId, companyInviteDTO.getCompanyName(),
                RandomStringUtils.randomAlphabetic(5), RandomStringUtils.randomAlphabetic(5), tokenCaptor.getValue()));
    }

    public CompanyDetailsDTO getDetailsTeam(RequestSpecification spec, Integer companyId) {
        return companies.getDetailsTeam(spec, companyId);
    }

    public CompanyDetailsDTO getDetailsTeam(RequestSpecification spec, Integer companyId, Function<ValidatableResponse, CompanyDetailsDTO> responseValidator) {
        return companies.getDetailsTeam(spec, companyId, responseValidator);
    }

    public List<BankAccountDTO> getBankAccounts(RequestSpecification spec, Integer companyId) {
        return companies.getBankAccounts(spec, companyId);
    }

    public List<BankAccountDTO> getBankAccounts(RequestSpecification spec, Integer companyId, Function<ValidatableResponse, List<BankAccountDTO>> responseValidator) {
        return companies.getBankAccounts(spec, companyId, responseValidator);
    }

    public BankAccountDTO addBankAccount(RequestSpecification spec, Integer companyId, BankAccountDTO bankAccountDTO) {
        return companies.addBankAccount(spec, companyId, bankAccountDTO);
    }

    public BankAccountDTO addBankAccount(RequestSpecification spec, Integer companyId, BankAccountDTO bankAccountDTO, Function<ValidatableResponse, BankAccountDTO> responseValidator) {
        return companies.addBankAccount(spec, companyId, bankAccountDTO, responseValidator);
    }

    public BankAccountDTO editBankAccount(RequestSpecification spec, Integer companyId, BankAccountDTO bankAccountDTO) {
        return companies.editBankAccount(spec, companyId, bankAccountDTO);
    }

    public BankAccountDTO editBankAccount(RequestSpecification spec, Integer companyId, BankAccountDTO bankAccountDTO, Function<ValidatableResponse, BankAccountDTO> responseValidator) {
        return companies.editBankAccount(spec, companyId, bankAccountDTO, responseValidator);
    }

    public void deleteBankAccount(RequestSpecification spec, Integer companyId, Integer bankAccountId) {
        companies.deleteBankAccount(spec, companyId, bankAccountId);
    }

    public void deleteBankAccount(RequestSpecification spec, Integer companyId, Integer bankAccountId, Consumer<ValidatableResponse> responseValidator) {
        companies.deleteBankAccount(spec, companyId, bankAccountId, responseValidator);
    }

    // ========================== Projects

    public ProjectBoardDTO createProject(RequestSpecification spec, NewProjectDTO request) {
        return projects.createProject(spec, request);
    }

    public ProjectBoardDTO createProject(RequestSpecification spec, NewProjectDTO request, Function<ValidatableResponse, ProjectBoardDTO> responseValidator) {
        return projects.createProject(spec, request, responseValidator);
    }

    public ProjectBoardDTO editProject(RequestSpecification spec, Integer projectId, EditProjectDTO request) {
        return projects.editProject(spec, projectId, request);
    }

    public ProjectBoardDTO editProject(RequestSpecification spec, Integer projectId, EditProjectDTO request, Function<ValidatableResponse, ProjectBoardDTO> responseValidator) {
        return projects.editProject(spec, projectId, request, responseValidator);
    }

    public TeamUserDTO addUserToProject(RequestSpecification spec, Integer projectId, Integer userId) {
        return projects.addUserToProject(spec, projectId, userId);
    }

    public TeamUserDTO addUserToProject(RequestSpecification spec, Integer projectId, Integer userId, Function<ValidatableResponse, TeamUserDTO> responseValidator) {
        return projects.addUserToProject(spec, projectId, userId, responseValidator);
    }

    public void removeUserFromProject(RequestSpecification spec, Integer projectId, Integer userId) {
        projects.removeUserFromProject(spec, projectId, userId);
    }

    public void removeUserFromProject(RequestSpecification spec, Integer projectId, Integer userId, Consumer<ValidatableResponse> responseValidator) {
        projects.removeUserFromProject(spec, projectId, userId, responseValidator);
    }

    public TeamUserDTO changeProjectAdminRole(RequestSpecification spec, Integer projectId, Integer userId, Integer roleId) {
        return projects.changeAdminRole(spec, projectId, userId, roleId);
    }

    public TeamUserDTO changeProjectAdminRole(RequestSpecification spec, Integer projectId, Integer userId, Integer roleId, Function<ValidatableResponse, TeamUserDTO> responseValidator) {
        return projects.changeAdminRole(spec, projectId, userId, roleId, responseValidator);
    }

    public TeamUserDTO changeProjectNonAdminRole(RequestSpecification spec, Integer projectId, Integer userId, Integer roleId) {
        return projects.changeNonAdminRole(spec, projectId, userId, roleId);
    }

    public TeamUserDTO changeProjectNonAdminRole(RequestSpecification spec, Integer projectId, Integer userId, Integer roleId, Function<ValidatableResponse, TeamUserDTO> responseValidator) {
        return projects.changeNonAdminRole(spec, projectId, userId, roleId, responseValidator);
    }

    public List<TeamUserDTO> getAvailableUsersToAddToProject(RequestSpecification spec, Integer projectId) {
        return projects.getAvailableUsersToAddToProject(spec, projectId);
    }

    public List<TeamUserDTO> getAvailableUsersToAddToProject(RequestSpecification spec, Integer projectId, Function<ValidatableResponse, List<TeamUserDTO>> responseValidator) {
        return projects.getAvailableUsersToAddToProject(spec, projectId, responseValidator);
    }

    public List<SimpleCompanyDTO> getAvailableCompaniesToAddToProject(RequestSpecification spec, Integer projectId) {
        return projects.getAvailableCompaniesToAddToProject(spec, projectId);
    }

    public List<SimpleCompanyDTO> getAvailableCompaniesToAddToProject(RequestSpecification spec, Integer projectId, Function<ValidatableResponse, List<SimpleCompanyDTO>> responseValidator) {
        return projects.getAvailableCompaniesToAddToProject(spec, projectId, responseValidator);
    }

    public List<CompanyDirectoryDTO> getProjectCompanies(RequestSpecification spec, Integer projectId) {
        return projects.getProjectCompanies(spec, projectId);
    }

    public List<CompanyDirectoryDTO> getProjectCompanies(RequestSpecification spec, Integer projectId, Function<ValidatableResponse, List<CompanyDirectoryDTO>> responseValidator) {
        return projects.getProjectCompanies(spec, projectId, responseValidator);
    }

    public CompanyDirectoryDTO addCompanyToProject(RequestSpecification spec, Integer projectId, Integer companyId, Integer roleId) {
        return projects.addCompanyToProject(spec, projectId, companyId, roleId);
    }
    
    public CompanyDirectoryDTO addCompanyToProject(RequestSpecification spec, Integer projectId, Integer companyId) {
        return projects.addCompanyToProject(spec, projectId, companyId, null);
    }    

    public CompanyDirectoryDTO addCompanyToProject(RequestSpecification spec, Integer projectId, Integer companyId, Integer roleId, Function<ValidatableResponse, CompanyDirectoryDTO> responseValidator) {
        return projects.addCompanyToProject(spec, projectId, companyId, roleId, responseValidator);
    }    
    
    public CompanyDirectoryDTO addCompanyToProject(RequestSpecification spec, Integer projectId, Integer companyId, Function<ValidatableResponse, CompanyDirectoryDTO> responseValidator) {
        return projects.addCompanyToProject(spec, projectId, companyId, null, responseValidator);
    }
    
    public void removeCompanyFromProject(RequestSpecification spec, Integer projectId, Integer companyId) {
        projects.removeCompanyFromProject(spec, projectId, companyId);
    }

    public void removeCompanyFromProject(RequestSpecification spec, Integer projectId, Integer companyId, Consumer<ValidatableResponse> responseValidator) {
        projects.removeCompanyFromProject(spec, projectId, companyId, responseValidator);
    }

    public CompanyDirectoryDTO inviteCompanyToProject(RequestSpecification spec, Integer projectId, Integer companyId, Function<ValidatableResponse, CompanyDirectoryDTO> responseValidator) {
        return projects.inviteCompanyToProject(spec, projectId, companyId, responseValidator);
    }

    public CompanyDirectoryDTO inviteCompanyToProject(RequestSpecification spec, Integer projectId, Integer companyId) {
        return projects.inviteCompanyToProject(spec, projectId, companyId);
    }

    public void assignCustomerToProject(RequestSpecification spec, Integer projectId, Integer customerCompanyId) {
        projects.assignCustomerToProject(spec, projectId, customerCompanyId);
    }

    public void assignCustomerToProject(RequestSpecification spec, Integer projectId, Integer customerCompanyId, Consumer<ValidatableResponse> responseValidator) {
        projects.assignCustomerToProject(spec, projectId, customerCompanyId, responseValidator);
    }

    public TeamUserDTO[] getProjectTeam(RequestSpecification spec, Integer projectId, Integer companyId) {
        return projects.getProjectTeam(spec, projectId, companyId);
    }

    public TeamUserDTO[] getProjectTeam(RequestSpecification spec, Integer projectId, Integer companyId, Function<ValidatableResponse, TeamUserDTO[]> responseValidator) {
        return projects.getProjectTeam(spec, projectId, companyId, responseValidator);
    }

    public List<Integer> getProjectPermissions(RequestSpecification spec, Integer projectId) {
        return projects.getPermissions(spec, projectId);
    }

    public List<Integer> getProjectPermissions(RequestSpecification spec, Integer projectId, Function<ValidatableResponse, List<Integer>> responseValidator) {
        return projects.getPermissions(spec, projectId, responseValidator);
    }

    public List<ProjectDTO> getProjects(RequestSpecification spec) {
        return projects.getProjects(spec);
    }

    public List<ProjectDTO> getProjects(RequestSpecification spec, Function<ValidatableResponse, List<ProjectDTO>> responseValidator) {
        return projects.getProjects(spec, responseValidator);
    }

    public List<ProjectDTO> getArchivedProjects(RequestSpecification spec) {
        return projects.getArchivedProjects(spec);
    }

    public List<ProjectDTO> getArchivedProjects(RequestSpecification spec, Function<ValidatableResponse, List<ProjectDTO>> responseValidator) {
        return projects.getArchivedProjects(spec, responseValidator);
    }

    public void archiveProject(RequestSpecification spec, Integer projectId) {
        projects.archiveProject(spec, projectId);
    }

    public void archiveProject(
            RequestSpecification spec,
            Integer projectId,
            Consumer<ValidatableResponse> responseValidator
    ) {
        projects.archiveProject(spec, projectId, responseValidator);
    }

    public void restoreProject(RequestSpecification spec, Integer projectId) {
        projects.restoreProject(spec, projectId);
    }

    public void restoreProject(
            RequestSpecification spec,
            Integer projectId,
            Consumer<ValidatableResponse> responseValidator
    ) {
        projects.restoreProject(spec, projectId, responseValidator);
    }

    public ProjectBoardDTO getProjectBoard(RequestSpecification spec, Integer projectId) {
        return projects.getProjectBoard(spec, projectId);
    }

    public ProjectBoardDTO getProjectBoard(RequestSpecification spec, Integer projectId, Function<ValidatableResponse, ProjectBoardDTO> responseValidator) {
        return projects.getProjectBoard(spec, projectId, responseValidator);
    }

    public ProjectDTO getProject(RequestSpecification spec, Integer projectId) {
        return projects.getProject(spec, projectId);
    }

    public ProjectDTO getProject(RequestSpecification spec, Integer projectId, Function<ValidatableResponse, ProjectDTO> responseValidator) {
        return projects.getProject(spec, projectId, responseValidator);
    }

    // ========================== Locations
    
    public LocationDetailsDTO createLocation(RequestSpecification spec, LocationCreateDTO request) {
        return locations.createLocation(spec, request);
    }

    public LocationDetailsDTO createLocation(RequestSpecification spec, LocationCreateDTO request, Function<ValidatableResponse, LocationDetailsDTO> responseValidator) {
        return locations.createLocation(spec, request, responseValidator);
    }

    public List<LocationDetailsDTO> getChildren(RequestSpecification spec, Integer projectId, Integer parentLocationId) {
        return locations.getChildren(spec, projectId, parentLocationId);
    }

    public List<LocationDetailsDTO> getChildren(
            RequestSpecification spec,
            Integer projectId,
            Integer parentLocationId,
            Function<ValidatableResponse, List<LocationDetailsDTO>> responseValidator
    ) {
        return locations.getChildren(spec, projectId, parentLocationId, responseValidator);
    }
    
    public LocationDetailsDTO updateLocation(RequestSpecification spec, LocationUpdateDTO request) {
        return locations.updateLocation(spec, request);
    }

    public LocationDetailsDTO updateLocation(RequestSpecification spec, LocationUpdateDTO request, Function<ValidatableResponse, LocationDetailsDTO> responseValidator) {
        return locations.updateLocation(spec, request, responseValidator);
    }

    public List<LocationTreeDTO> getExpandedTree(RequestSpecification spec, Integer locationId) {
        return locations.getExpandedTree(spec, locationId);
    }    
    
    public List<LocationTreeDTO> getExpandedTree(RequestSpecification spec, Integer locationId, Function<ValidatableResponse, List<LocationTreeDTO>> responseValidator) {
        return locations.getExpandedTree(spec, locationId, responseValidator);
    }    
    
    public void deleteLocation(RequestSpecification spec, Integer locationId) {
        locations.deleteLocation(spec, locationId);
    }

    public void deleteLocation(RequestSpecification spec, Integer locationId, Consumer<ValidatableResponse> responseValidator) {
        locations.deleteLocation(spec, locationId, responseValidator);
    }
    
    public TeamUserDTO[] getLocationTeam(RequestSpecification spec, Integer locationId) {
        return locations.getLocationTeam(spec, locationId);
    }

    public TeamUserDTO[] getLocationTeam(RequestSpecification spec, Integer locationId, Function<ValidatableResponse, TeamUserDTO[]> responseValidator) {
        return locations.getLocationTeam(spec, locationId, responseValidator);
    }

    public LocationDetailsDTO copyLocation(
            RequestSpecification spec,
            Integer srcLocationId,
            Integer desLocationId
    ) {
        return locations.copyLocation(spec, srcLocationId, desLocationId);
    }

    public LocationDetailsDTO copyLocation(
            RequestSpecification spec,
            Integer srcLocationId,
            Integer desLocationId,
            Function<ValidatableResponse, LocationDetailsDTO> responseValidator
    ) {
        return locations.copyLocation(spec, srcLocationId, desLocationId, responseValidator);
    }

    public LocationDetailsDTO moveLocation(
            RequestSpecification spec,
            Integer srcLocationId,
            Integer desLocationId
    ) {
        return locations.moveLocation(spec, srcLocationId, desLocationId);
    }

    public LocationDetailsDTO moveLocation(
            RequestSpecification spec,
            Integer srcLocationId,
            Integer desLocationId,
            Function<ValidatableResponse, LocationDetailsDTO> responseValidator
    ) {
        return locations.moveLocation(spec, srcLocationId, desLocationId, responseValidator);
    }

    public List<LocationDetailsDTO> moveLocationOrder(RequestSpecification spec, Integer locationId, Integer order) {
        return locations.moveLocationOrder(spec, locationId, order);
    }

    public List<LocationDetailsDTO> moveLocationOrder(RequestSpecification spec, Integer locationId, Integer order, Function<ValidatableResponse, List<LocationDetailsDTO>> responseValidator) {
        return locations.moveLocationOrder(spec, locationId, order, responseValidator);

    }

    // ========================== Tasks

    public MainTaskDTO addContractualTask(RequestSpecification asAdmin, CreateContractualTaskRequest request) {
        return tasks.addContractualTask(asAdmin, request);
    }

    public MainTaskDTO addContractualTask(RequestSpecification spec, CreateContractualTaskRequest request, Function<ValidatableResponse, MainTaskDTO> responseValidator) {
        return tasks.addContractualTask(spec, request, responseValidator);
    }

    public MainTaskDTO addDirectorialTask(RequestSpecification asAdmin, CreateDirectorialTaskRequest request) {
        return tasks.addDirectorialTask(asAdmin, request);
    }

    public MainTaskDTO addDirectorialTask(RequestSpecification spec, CreateDirectorialTaskRequest request, Function<ValidatableResponse, MainTaskDTO> responseValidator) {
        return tasks.addDirectorialTask(spec, request, responseValidator);
    }

    public MainTaskDTO updateDirectorialTask(
            RequestSpecification asAdmin,
            Integer taskId,
            UpdateDirectorialTaskRequest request
    ) {
        return tasks.updateDirectorialTask(asAdmin, taskId, request);
    }

    public MainTaskDTO updateDirectorialTask(
            RequestSpecification spec,
            Integer taskId,
            UpdateDirectorialTaskRequest request,
            Function<ValidatableResponse, MainTaskDTO> responseValidator
    ) {
        return tasks.updateDirectorialTask(spec, taskId, request, responseValidator);
    }

    public MainTaskDTO getMainTask(RequestSpecification asAdmin, Integer taskId) {
        return tasks.getMainTask(asAdmin, taskId);
    }

    public MainTaskDTO getMainTask(
            RequestSpecification spec,
            Integer taskId,
            Function<ValidatableResponse, MainTaskDTO> responseValidator
    ) {
        return tasks.getMainTask(spec, taskId, responseValidator);
    }

    public SubTaskDTO addSubTask(RequestSpecification spec, CreateSubTaskRequest request) {
        return tasks.addSubTask(spec, request);
    }

    public SubTaskDTO addSubTask(RequestSpecification spec, CreateSubTaskRequest request, Function<ValidatableResponse, SubTaskDTO> responseValidator) {
        return tasks.addSubTask(spec, request, responseValidator);
    }

    public MainTaskDTO assignCompanyToMainTask(
            RequestSpecification spec,
            Integer mainTaskId,
            AssignCompanyToTaskRequest request
    ) {
        return tasks.assignCompanyToMainTask(spec, mainTaskId, request);
    }

    public MainTaskDTO assignCompanyToMainTask(
            RequestSpecification spec,
            Integer mainTaskId,
            AssignCompanyToTaskRequest request,
            Function<ValidatableResponse, MainTaskDTO> responseValidator
    ) {
        return tasks.assignCompanyToMainTask(spec, mainTaskId, request, responseValidator);
    }

    public MainTaskDTO unAssignCompanyFromMainTask(RequestSpecification spec, Integer mainTaskId) {
        return tasks.unAssignCompanyFromMainTask(spec, mainTaskId);
    }

    public MainTaskDTO unAssignCompanyFromMainTask(
            RequestSpecification spec,
            Integer mainTaskId,
            Function<ValidatableResponse, MainTaskDTO> responseValidator
    ) {
        return tasks.unAssignCompanyFromMainTask(spec, mainTaskId, responseValidator);
    }

    public void addUserToMainTask(RequestSpecification spec, Integer subTaskId, Integer workerId) {
        tasks.addUserToMainTask(spec, subTaskId, workerId);
    }

    public void addUserToMainTask(RequestSpecification spec, Integer mainTaskId, Integer workerId, Consumer<ValidatableResponse> responseValidator) {
        tasks.addUserToMainTask(spec, mainTaskId, workerId, responseValidator);
    }

    public List<TeamUserDTO> getSubTaskTeam(RequestSpecification spec, Integer subTaskId) {
        return tasks.getSubTaskTeam(spec, subTaskId);
    }

    public List<TeamUserDTO> getSubTaskTeam(RequestSpecification spec, Integer subTaskId, Function<ValidatableResponse, List<TeamUserDTO>> responseValidator) {
        return tasks.getSubTaskTeam(spec, subTaskId, responseValidator);
    }

    public List<TeamUserDTO> getMainTaskTeam(RequestSpecification spec, Integer mainTaskId) {
        return tasks.getMainTaskTeam(spec, mainTaskId);
    }

    public List<TeamUserDTO> getMainTaskTeam(RequestSpecification spec, Integer mainTaskId, Function<ValidatableResponse, List<TeamUserDTO>> responseValidator) {
        return tasks.getMainTaskTeam(spec, mainTaskId, responseValidator);
    }

    public void removeUserFromMainTask(RequestSpecification spec, Integer mainTask, Integer workerId) {
        tasks.removeUserFromMainTask(spec, mainTask, workerId);
    }

    public void removeUserFromMainTask(RequestSpecification spec, Integer mainTaskId, Integer workerId, Consumer<ValidatableResponse> responseValidator) {
        tasks.removeUserFromMainTask(spec, mainTaskId, workerId, responseValidator);
    }

    public void addUserToSubTask(RequestSpecification spec, Integer subTaskId, Integer workerId) {
        tasks.addUserToSubTask(spec, subTaskId, workerId);
    }

    public void addUserToSubTask(RequestSpecification spec, Integer subTaskId, Integer workerId, Consumer<ValidatableResponse> responseValidator) {
        tasks.addUserToSubTask(spec, subTaskId, workerId, responseValidator);
    }

    public IdResponse addMainTaskChecklistItem(RequestSpecification spec, Integer mainTaskId, AddTaskChecklistItemRequest request) {
        return tasks.addMainTaskChecklistItem(spec, mainTaskId, request);
    }

    public IdResponse addMainTaskChecklistItem(RequestSpecification spec, Integer mainTaskId, AddTaskChecklistItemRequest request, Function<ValidatableResponse, IdResponse> responseValidator) {
        return tasks.addMainTaskChecklistItem(spec, mainTaskId, request, responseValidator);
    }

    public IdResponse addSubTaskChecklistItem(RequestSpecification spec, Integer subTaskId) {
        return tasks.addSubTaskChecklistItem(spec, subTaskId);
    }

    public IdResponse addSubTaskChecklistItem(RequestSpecification spec, Integer subTaskId, Function<ValidatableResponse, IdResponse> responseValidator) {
        return tasks.addSubTaskChecklistItem(spec, subTaskId, responseValidator);
    }

    public TaskChecklistItem[] getMainTaskCheckList(RequestSpecification spec, Integer mainTaskId) {
        return tasks.getMainTaskCheckList(spec, mainTaskId);
    }

    public TaskChecklistItem[] getMainTaskCheckList(RequestSpecification spec, Integer mainTaskId, Function<ValidatableResponse, TaskChecklistItem[]> responseValidator) {
        return tasks.getMainTaskCheckList(spec, mainTaskId, responseValidator);
    }

    public TaskChecklistItem[] getSubTaskCheckList(RequestSpecification spec, Integer subTaskId) {
        return tasks.getSubTaskCheckList(spec, subTaskId);
    }

    public TaskChecklistItem[] getSubTaskCheckList(RequestSpecification spec, Integer subTaskId, Function<ValidatableResponse, TaskChecklistItem[]> responseValidator) {
        return tasks.getSubTaskCheckList(spec, subTaskId, responseValidator);
    }

    public void onCheckListItem(RequestSpecification spec, Integer checklistItemId) {
        tasks.onCheckListItem(spec, checklistItemId);
    }

    public void onCheckListItem(RequestSpecification spec, Integer checklistItemId, Consumer<ValidatableResponse> responseValidator) {
        tasks.onCheckListItem(spec, checklistItemId, responseValidator);
    }

    public void addCommentToSubTask(RequestSpecification spec, Integer subTaskId, CreateCommentDTO request) {
        tasks.addCommentToSubTask(spec, subTaskId, request);
    }

    public void addCommentToSubTask(RequestSpecification spec, Integer subTaskId, CreateCommentDTO request, Consumer<ValidatableResponse> responseValidator) {
        tasks.addCommentToSubTask(spec, subTaskId, request, responseValidator);
    }

    public CommentDTO[] getSubTaskComments(RequestSpecification spec, Integer subTaskId) {
        return tasks.getSubTaskComments(spec, subTaskId);
    }

    public CommentDTO[] getSubTaskComments(RequestSpecification spec, Integer subTaskId, Function<ValidatableResponse, CommentDTO[]> responseValidator) {
        return tasks.getSubTaskComments(spec, subTaskId, responseValidator);
    }

    public CommentDTO updateSubTaskComment(
            RequestSpecification spec,
            Integer commentId,
            CreateCommentDTO request
    ) {
        return tasks.updateSubTaskComment(spec, commentId, request);
    }

    public CommentDTO updateSubTaskComment(
            RequestSpecification spec,
            Integer commentId,
            CreateCommentDTO request,
            Function<ValidatableResponse, CommentDTO> responseValidator
    ) {
        return tasks.updateSubTaskComment(spec, commentId, request, responseValidator);
    }

    public void archiveSubTaskComment(RequestSpecification spec, Integer commentId) {
        tasks.archiveSubTaskComment(spec, commentId);
    }

    public void archiveSubTaskComment(
            RequestSpecification spec,
            Integer commentId,
            Consumer<ValidatableResponse> responseValidator
    ) {
        tasks.archiveSubTaskComment(spec, commentId, responseValidator);
    }

    public MainTaskDTO[] listMainTasksForProject(RequestSpecification spec, Integer projectId, ListTasksRequest request) {
        return tasks.listMainTasksForProject(spec, projectId, request);
    }

    public MainTaskDTO[] listMainTasksForProject(RequestSpecification spec, Integer projectId, ListTasksRequest request, Function<ValidatableResponse, MainTaskDTO[]> responseValidator) {
        return tasks.listMainTasksForProject(spec, projectId, request, responseValidator);
    }

    public Map<Integer, List<Integer>> listMainAndSubTasksForProject(RequestSpecification spec, Integer projectId, ListTaskIdsRequest request) {
        return tasks.listMainAndSubTasksForProject(spec, projectId, request);
    }

    public Map<Integer, List<Integer>> listMainAndSubTasksForProject(RequestSpecification spec, Integer projectId, ListTaskIdsRequest request, Function<ValidatableResponse, Map<Integer, List<Integer>>> responseValidator) {
        return tasks.listMainAndSubTasksForProject(spec, projectId, request, responseValidator);
    }

    public MainTaskDTO[] listMainTasksForLocation(RequestSpecification spec, Integer locationId) {
        return tasks.listMainTasksForLocation(spec, locationId);
    }

    public MainTaskDTO[] listMainTasksForLocation(RequestSpecification spec, Integer locationId, Function<ValidatableResponse, MainTaskDTO[]> responseValidator) {
        return tasks.listMainTasksForLocation(spec, locationId, responseValidator);
    }

    public Map<Integer, List<Integer>> listMainAndSubTasksForLocations(RequestSpecification spec, Integer locationId, ListTaskIdsRequest request) {
        return tasks.listMainAndSubTasksForLocations(spec, locationId, request);
    }

    public Map<Integer, List<Integer>> listMainAndSubTasksForLocations(RequestSpecification spec, Integer locationId, ListTaskIdsRequest request, Function<ValidatableResponse, Map<Integer, List<Integer>>> responseValidator) {
        return tasks.listMainAndSubTasksForLocations(spec, locationId, request, responseValidator);
    }

    public List<Integer> listSubTaskIds(RequestSpecification spec, Integer mainTaskId) {
        return tasks.listSubTaskIds(spec, mainTaskId);
    }

    public List<Integer> listSubTaskIds(RequestSpecification spec, Integer mainTaskId, Function<ValidatableResponse, List<Integer>> responseValidator) {
        return tasks.listSubTaskIds(spec, mainTaskId, responseValidator);
    }

    public List<TeamUserDTO> listMainTaskAvailableUsers(RequestSpecification spec, Integer taskId) {
        return tasks.listMainTaskAvailableUsers(spec, taskId);
    }

    public List<TeamUserDTO> listMainTaskAvailableUsers(RequestSpecification spec, Integer taskId, Function<ValidatableResponse, List<TeamUserDTO>> responseValidator) {
        return tasks.listMainTaskAvailableUsers(spec, taskId, responseValidator);
    }

    public List<TeamUserDTO> listMainTaskAvailableUsersForCreation(RequestSpecification spec, Integer projectId, Integer companyId) {
        return tasks.listMainTaskAvailableUsersForCreation(spec, projectId, companyId);
    }

    public List<TeamUserDTO> listMainTaskAvailableUsersForCreation(RequestSpecification spec, Integer projectId, Integer companyId, Function<ValidatableResponse, List<TeamUserDTO>> responseValidator) {
        return tasks.listMainTaskAvailableUsersForCreation(spec, projectId, companyId, responseValidator);
    }

    public List<TeamUserDTO> listSubTaskAvailableUsers(RequestSpecification spec, Integer subTaskId) {
        return tasks.listSubTaskAvailableUsers(spec, subTaskId);
    }

    public List<TeamUserDTO> listSubTaskAvailableUsers(RequestSpecification spec, Integer subTaskId, Function<ValidatableResponse, List<TeamUserDTO>> responseValidator) {
        return tasks.listSubTaskAvailableUsers(spec, subTaskId, responseValidator);
    }

    // ========================== WorkLogs


    public TaskStatusDTO startTimer(RequestSpecification spec, TaskWorklogRequest request) {
        return workLogs.startTimer(spec, request);
    }

    public TaskStatusDTO startTimer(RequestSpecification spec, TaskWorklogRequest request, Function<ValidatableResponse, TaskStatusDTO> responseValidator) {
        return workLogs.startTimer(spec, request, responseValidator);
    }

    public TaskStatusDTO stopTimer(RequestSpecification spec, TaskWorklogRequest request) {
        return workLogs.stopTimer(spec, request);
    }

    public TaskStatusDTO stopTimer(RequestSpecification spec, TaskWorklogRequest request, Function<ValidatableResponse, TaskStatusDTO> responseValidator) {
        return workLogs.stopTimer(spec, request, responseValidator);
    }

    public TaskStatusDTO completeSubTask(RequestSpecification spec, TaskWorklogRequest request) {
        return workLogs.completeSubTask(spec, request);
    }

    public TaskStatusDTO completeSubTask(RequestSpecification spec, TaskWorklogRequest request, Function<ValidatableResponse, TaskStatusDTO> responseValidator) {
        return workLogs.completeSubTask(spec, request, responseValidator);
    }

    public TaskStatusDTO completeTask(RequestSpecification spec, TaskWorklogRequest request) {
        return workLogs.completeTask(spec, request);
    }

    public TaskStatusDTO completeTask(RequestSpecification spec, TaskWorklogRequest request, Function<ValidatableResponse, TaskStatusDTO> responseValidator) {
        return workLogs.completeTask(spec, request, responseValidator);
    }

    public TaskStatusDTO approveTask(RequestSpecification spec, TaskWorklogRequest request) {
        return workLogs.approveTask(spec, request);
    }

    public TaskStatusDTO approveTask(RequestSpecification spec, TaskWorklogRequest request, Function<ValidatableResponse, TaskStatusDTO> responseValidator) {
        return workLogs.approveTask(spec, request, responseValidator);
    }

    public TaskStatusDTO rejectTask(RequestSpecification spec, RejectTaskRequest request) {
        return workLogs.rejectTask(spec, request);
    }

    public TaskStatusDTO rejectTaskContractor(RequestSpecification spec, TaskWorklogRequest request) {
        return workLogs.rejectTaskContractor(spec, request);
    }

    public TaskStatusDTO rejectTaskContractor(RequestSpecification spec, TaskWorklogRequest request, Function<ValidatableResponse, TaskStatusDTO> responseValidator) {
        return workLogs.rejectTaskContractor(spec, request, responseValidator);
    }

    public MainTaskDurationDTO getMainTaskDuration(RequestSpecification spec, Integer mainTaskId) {
        return workLogs.getMainTaskDuration(spec, mainTaskId);
    }

    public MainTaskDurationDTO getMainTaskDuration(RequestSpecification spec, Integer mainTaskId, Function<ValidatableResponse, MainTaskDurationDTO> responseValidator) {
        return workLogs.getMainTaskDuration(spec, mainTaskId, responseValidator);
    }

    public SubTaskDurationDTO getSubTaskDuration(RequestSpecification spec, Integer SubTaskId) {
        return workLogs.getSubTaskDuration(spec, SubTaskId);
    }

    public SubTaskDurationDTO getSubTaskDuration(RequestSpecification spec, Integer SubTaskId, Function<ValidatableResponse, SubTaskDurationDTO> responseValidator) {
        return workLogs.getSubTaskDuration(spec, SubTaskId, responseValidator);
    }

    public SubTaskDurationDTO getSubTaskWorkerDuration(RequestSpecification spec, Integer subTaskId, Integer workerId) {
        return workLogs.getSubTaskWorkerDuration(spec, subTaskId, workerId);
    }

    public SubTaskDurationDTO getSubTaskWorkerDuration(RequestSpecification spec, Integer subTaskId, Integer workerId, Function<ValidatableResponse, SubTaskDurationDTO> responseValidator) {
        return workLogs.getSubTaskWorkerDuration(spec, subTaskId, workerId, responseValidator);
    }

    public void cancelSubTaskTimerWorkLog(RequestSpecification spec, Integer subTaskId, Integer startTimerEventId) {
        workLogs.cancelSubTaskTimerWorkLog(spec, subTaskId, startTimerEventId);
    }

    public void cancelSubTaskTimerWorkLog(RequestSpecification spec, Integer subTaskId, Integer startTimerEventId, Consumer<ValidatableResponse> responseConsumer) {
        workLogs.cancelSubTaskTimerWorkLog(spec, subTaskId, startTimerEventId, responseConsumer);
    }

    public List<EventHistoryDTO> mainTaskWorkLogHistory(RequestSpecification spec, Integer mainTaskId) {
        return workLogs.mainTaskWorkLogHistory(spec, mainTaskId);
    }

    // ========================== Media

    public MediaWidgetFileDTO createWorklogAttachment(RequestSpecification spec, Integer mainTaskId, WorkLogEventType event) {
        return media.createWorklogAttachment(spec, mainTaskId, event);
    }

    public List<MediaWidgetFileDTO> listFiles(RequestSpecification spec, ReferenceType referenceType, Integer referenceId) {
        return media.listFiles(spec, referenceType, referenceId);
    }

    public List<MediaWidgetFileDTO> listFiles(RequestSpecification spec, ReferenceType referenceType, Integer referenceId, Function<ValidatableResponse, List<MediaWidgetFileDTO>> responseValidator) {
        return media.listFiles(spec, referenceType, referenceId, responseValidator);

    }

    public File fileUploadTemporary(RequestSpecification spec) {
        return media.fileUploadTemporary(spec);
    }

    public File fileUploadTemporary(RequestSpecification spec, Function<ValidatableResponse, File> responseValidator) {
        return media.fileUploadTemporary(spec, responseValidator);
    }

    public File fileUpload(RequestSpecification spec, ReferenceType referenceType, Integer referenceId) {
        return media.fileUpload(spec, referenceType, referenceId);
    }

    public void deleteMediaWidgetNode(RequestSpecification spec, Integer nodeId) {
        media.deleteMediaWidgetNode(spec, nodeId);
    }

    public void deleteMediaWidgetNode(RequestSpecification spec, Integer nodeId, Consumer<ValidatableResponse> responseValidator) {
        media.deleteMediaWidgetNode(spec, nodeId, responseValidator);
    }

    public MediaWidgetFileDTO updateMediaWidgetNode(RequestSpecification spec, Integer nodeId, String name) {
        return media.updateMediaWidgetNode(spec, nodeId, name);
    }

    public MediaWidgetFileDTO updateMediaWidgetNode(RequestSpecification spec, Integer nodeId, String name, Function<ValidatableResponse, MediaWidgetFileDTO> responseValidator) {
        return media.updateMediaWidgetNode(spec, nodeId, name, responseValidator);
    }
    
    public MediaWidgetFileDTO createMediaWidgetFolder(RequestSpecification spec, ReferenceType referenceType, Integer referenceId, Integer parentNodeId, String name, Function<ValidatableResponse, MediaWidgetFileDTO> responseValidator) {
        return media.createMediaWidgetFolder(spec, referenceType, referenceId, parentNodeId, name, responseValidator);
    }

    public MediaWidgetFileDTO createMediaWidgetFile(RequestSpecification spec, ReferenceType referenceType, Integer referenceId, Integer parentNodeId, String name, Function<ValidatableResponse, MediaWidgetFileDTO> responseValidator) {
        return media.createMediaWidgetFile(spec, referenceType, referenceId, parentNodeId, name, responseValidator);
    }

    public File fileUpload(RequestSpecification spec, ReferenceType referenceType, Integer referenceId, Function<ValidatableResponse, File> responseValidator) {
        return media.fileUpload(spec, referenceType, referenceId, responseValidator);
    }

    // ========================== Catalog

    public CatalogImportReportDTO catalogImport(RequestSpecification spec, byte[] fileContent) {
        return catalog.globalCatalogImport(spec, fileContent);
    }

    public CatalogImportReportDTO catalogImport(RequestSpecification spec, byte[] fileContent, Function<ValidatableResponse, CatalogImportReportDTO> responseValidator) {
        return catalog.globalCatalogImport(spec, fileContent, responseValidator);
    }

    public String catalogExport(RequestSpecification spec, String scope) {
        return catalog.globalCatalogExport(spec, scope);
    }

    public String catalogExport(RequestSpecification spec, String scope, Function<ValidatableResponse, String> responseValidator) {
        return catalog.globalCatalogExport(spec, scope, responseValidator);
    }

    public CatalogImportReportDTO companyCatalogImport(RequestSpecification spec, Integer companyId, byte[] fileContent) {
        return catalog.companyCatalogImport(spec, companyId, fileContent);
    }

    public CatalogImportReportDTO companyCatalogImport(RequestSpecification spec, Integer companyId, byte[] fileContent, Function<ValidatableResponse, CatalogImportReportDTO> responseValidator) {
        return catalog.companyCatalogImport(spec, companyId, fileContent, responseValidator);
    }

    public String companyCatalogExport(RequestSpecification spec, Integer companyId, String scope, boolean showGlobal) {
        return catalog.companyCatalogExport(spec, companyId, scope, showGlobal);
    }

    public String companyCatalogExport(RequestSpecification spec, Integer companyId, String scope, boolean showGlobal, Function<ValidatableResponse, String> responseValidator) {
        return catalog.companyCatalogExport(spec, companyId, scope, showGlobal, responseValidator);

    }

    public CatalogNodeDTO deleteCatalogNode(RequestSpecification spec, String snp) {
        return catalog.deleteCatalogNode(spec, snp);
    }

    public CatalogNodeDTO deleteCatalogNode(RequestSpecification spec, String snp, Function<ValidatableResponse, CatalogNodeDTO> responseValidator) {
        return catalog.deleteCatalogNode(spec, snp, responseValidator);
    }

    // ========================== Custom Roles

    public CustomRoleDTO[] getRoles(RequestSpecification spec) {
        return customRoles.getRoles(spec);
    }

    public CustomRoleDTO[] getRoles(
            RequestSpecification spec,
            Function<ValidatableResponse, CustomRoleDTO[]> responseValidator
    ) {
        return customRoles.getRoles(spec, responseValidator);
    }

    public CustomRoleDTO[] getUserProjectRoles(RequestSpecification spec, Integer projectId, Integer userId) {
        return customRoles.getUserProjectRoles(spec, projectId, userId);
    }

    public CustomRoleDTO[] getUserProjectRoles(
            RequestSpecification spec, 
            Integer projectId, 
            Integer userId,
            Function<ValidatableResponse, CustomRoleDTO[]> responseValidator) {
        return customRoles.getUserProjectRoles(spec, projectId, userId, responseValidator);
    }    

    public CustomPermissionDTO[] getPermissions(RequestSpecification spec) {
        return customRoles.getPermissions(spec);
    }

    public CustomPermissionDTO[] getPermissions(
            RequestSpecification spec,
            Function<ValidatableResponse, CustomPermissionDTO[]> responseValidator
    ) {
        return customRoles.getPermissions(spec, responseValidator);
    }

    public CustomRoleDTO getRole(
            RequestSpecification spec,
            String roleName
    ) {
        return customRoles.getRole(spec, roleName);
    }

    public CustomRoleDTO getRole(
            RequestSpecification spec,
            String roleName,
            Function<ValidatableResponse, CustomRoleDTO> responseValidator
    ) {
        return customRoles.getRole(spec, roleName, responseValidator);
    }

    public CustomRoleDTO getRole(RequestSpecification spec, Integer roleId) {
        return customRoles.getRole(spec, roleId);
    }

    public CustomRoleDTO getRole(
            RequestSpecification spec,
            Integer roleId,
            Function<ValidatableResponse, CustomRoleDTO> responseValidator
    ) {
        return customRoles.getRole(spec, roleId, responseValidator);
    }

    public CustomRoleDTO createNewRole(
            RequestSpecification spec,
            CustomRoleDTO role
    ) {
        return customRoles.createNewRole(spec, role);
    }

    public CustomRoleDTO createNewRole(
            RequestSpecification spec,
            CustomRoleDTO role,
            Function<ValidatableResponse, CustomRoleDTO> responseValidator
    ) {
        return customRoles.createNewRole(spec, role, responseValidator);
    }

    public CustomRoleDTO updateRole(RequestSpecification spec, CustomRoleDTO role) {
        return customRoles.updateRole(spec, role);
    }

    public CustomRoleDTO updateRole(
            RequestSpecification spec,
            CustomRoleDTO role,
            Function<ValidatableResponse, CustomRoleDTO> responseValidator
    ) {
        return customRoles.updateRole(spec, role, responseValidator);
    }

    // ========================== Contracts

    public ContractDTO getContract(RequestSpecification spec, Integer contractId) {
        return contract.getContract(spec, contractId);
    }

    public ContractDTO getContract(RequestSpecification spec, Integer contractId, Function<ValidatableResponse, ContractDTO> responseValidator) {
        return contract.getContract(spec, contractId, responseValidator);
    }

    public List<ContractSummaryDTO> listContracts(RequestSpecification spec, ListContractsRequest request) {
        return contract.listContracts(spec, request);
    }

    public List<ContractSummaryDTO> listContracts(RequestSpecification spec, ListContractsRequest request, Function<ValidatableResponse, List<ContractSummaryDTO>> responseValidator) {
        return contract.listContracts(spec, request, responseValidator);
    }

    public ContractDTO createContract(
            RequestSpecification spec,
            CreateContractRequest request
    ) {
        return contract.createContract(spec, request);
    }

    public ContractDTO createContract(
            RequestSpecification spec,
            CreateContractRequest request,
            Function<ValidatableResponse, ContractDTO> responseValidator
    ) {
        return contract.createContract(spec, request, responseValidator);
    }
    
    public void createContractComment(
            RequestSpecification spec,
            CreateContractCommentRequest request
    ) {
        contract.createContractComment(spec, request);
    }
    
    public void createContractComment(
            RequestSpecification spec,
            CreateContractCommentRequest request,
            Consumer<ValidatableResponse> responseValidator            
    ) {
        contract.createContractComment(spec, request, responseValidator);
    }
    
    public ContractCommentDTO[] listContractComments(
            RequestSpecification spec,
            Integer contractId
    ) {
        return contract.listContractComments(spec, contractId);
    }
    
    public ContractCommentDTO[] listContractComments(
            RequestSpecification spec,
            Integer contractId,
            Function<ValidatableResponse, ContractCommentDTO[]> responseValidator
    ) {
        return contract.listContractComments(spec, contractId, responseValidator);
    }    

    public void removeContractComment(
            RequestSpecification spec,
            Integer commentId
    ) {
        contract.removeContractComment(spec, commentId);
    }
    
    public void removeContractComment(
            RequestSpecification spec,
            Integer commentId,
            Consumer<ValidatableResponse> responseValidator
    ) {
        contract.removeContractComment(spec, commentId, responseValidator);
    }    
    
    public ContractDTO updateContract(
            RequestSpecification spec,
            Integer contractId,
            UpdateContractRequest request
    ) {
        return contract.updateContract(spec, contractId, request);
    }

    public ContractDTO updateContract(
            RequestSpecification spec,
            Integer contractId,
            UpdateContractRequest request,
            Function<ValidatableResponse, ContractDTO> responseValidator
    ) {
        return contract.updateContract(spec, contractId, request, responseValidator);
    }

    public List<ContractTaskDTO> addContractTasks(RequestSpecification spec, ContractAddTasksRequest addTasksRequest) {
        return contract.addContractTasks(spec, addTasksRequest);
    }

    public List<ContractTaskDTO> addContractTasks(RequestSpecification spec, ContractAddTasksRequest addTasksRequest, Function<ValidatableResponse, List<ContractTaskDTO>> responseValidator) {
        return contract.addContractTasks(spec, addTasksRequest, responseValidator);
    }

    public ContractTaskDTO updateContractTask(RequestSpecification spec, ContractUpdateTaskRequest request) {
        return contract.updateContractTask(spec, request);
    }

    public ContractTaskDTO updateContractTask(RequestSpecification spec, ContractUpdateTaskRequest request, Function<ValidatableResponse, ContractTaskDTO> responseValidator) {
        return contract.updateContractTask(spec, request, responseValidator);
    }

    public void removeContractTask(RequestSpecification spec, Integer contractTaskId) {
        contract.removeContractTask(spec, contractTaskId);
    }

    public void removeContractTask(RequestSpecification spec, Integer contractTaskId, Consumer<ValidatableResponse> responseValidator) {
        contract.removeContractTask(spec, contractTaskId, responseValidator);
    }

    public ContractTaskDTO negateContractTask(
            RequestSpecification spec,
            Integer contractId,
            Integer contractTaskIdToNegate
    ) {
        return contract.negateContractTask(spec, contractId, contractTaskIdToNegate);
    }

    public ContractTaskDTO negateContractTask(
            RequestSpecification spec,
            Integer contractId,
            Integer contractTaskIdToNegate,
            Function<ValidatableResponse, ContractTaskDTO> responseValidator
    ) {
        return contract.negateContractTask(spec, contractId, contractTaskIdToNegate, responseValidator);
    }

    public String quickLinkContractAction(Integer contractId, String action, Integer userId) {
        return contract.quickLinkContractAction(contractId, action, userId);
    }

    public String quickLinkContractAction(
            Integer contractId,
            String action,
            Integer userId,
            Function<ValidatableResponse, String> responseValidator
    ) {
        return contract.quickLinkContractAction(contractId, action, userId, responseValidator);
    }

    public List<ContractTaskDTO> listContractTasks(RequestSpecification spec, Integer contractId) {
        return contract.listContractTasks(spec, contractId);
    }

    public List<ContractTaskDTO> listContractTasks(RequestSpecification spec, Integer contractId, Function<ValidatableResponse, List<ContractTaskDTO>> responseValidator) {
        return contract.listContractTasks(spec, contractId, responseValidator);
    }

    public List<MainTaskDTO> availableContractTasksToAdd(RequestSpecification spec, Integer contractId) {
        return contract.availableContractTasksToAdd(spec, contractId);
    }

    public List<MainTaskDTO> availableContractTasksToAdd(RequestSpecification spec, Integer contractId, Function<ValidatableResponse, List<MainTaskDTO>> responseValidator) {
        return contract.availableContractTasksToAdd(spec, contractId, responseValidator);
    }

    public Map<Integer, List<Integer>> listTaskAndSubTaskIdsByContract(RequestSpecification spec, Integer contractId, ListTaskIdsRequest request) {
        return contract.listTaskAndSubTaskIdsByContract(spec, contractId, request);
    }

    public Map<Integer, List<Integer>> listTaskAndSubTaskIdsByContract(RequestSpecification spec, Integer contractId, ListTaskIdsRequest request, Function<ValidatableResponse, Map<Integer, List<Integer>>> responseValidator) {
        return contract.listTaskAndSubTaskIdsByContract(spec, contractId, request, responseValidator);
    }

    public ContractEventLogDTO contractSendOffer(RequestSpecification spec, Integer contractId, SendMessageRequest request) {
        return contract.contractSendOffer(spec, contractId, request);
    }

    public ContractEventLogDTO contractSendOffer(RequestSpecification spec, Integer contractId, SendMessageRequest request, Function<ValidatableResponse, ContractEventLogDTO> responseValidator) {
        return contract.contractSendOffer(spec, contractId, request, responseValidator);
    }

    public ContractEventLogDTO contractSendInvitation(RequestSpecification spec, Integer contractId, SendMessageRequest request) {
        return contract.contractSendInvitation(spec, contractId, request);
    }

    public ContractEventLogDTO contractSendInvitation(RequestSpecification spec, Integer contractId, SendMessageRequest request, Function<ValidatableResponse, ContractEventLogDTO> responseValidator) {
        return contract.contractSendInvitation(spec, contractId, request, responseValidator);
    }

    public ContractEventLogDTO selfAcceptOffer(RequestSpecification spec, Integer contractId, SendMessageRequest request) {
        return contract.selfAcceptOffer(spec, contractId, request);
    }

    public ContractEventLogDTO selfAcceptOffer(RequestSpecification spec, Integer contractId, SendMessageRequest request, Function<ValidatableResponse, ContractEventLogDTO> responseValidator) {
        return contract.selfAcceptOffer(spec, contractId, request, responseValidator);
    }

    public String quickLinkAcceptContractOffer(Integer contractId, Integer userId) {
        return contract.quickLinkContractAction(contractId, "accept-offer", userId);
    }

    public String quickLinkAcceptContractOffer(Integer contractId, Integer userId, Function<ValidatableResponse, String> responseValidator) {
        return contract.quickLinkContractAction(contractId, "accept-offer", userId, responseValidator);
    }

    public String quickLinkDeclineContractOffer(Integer contractId, Integer userId) {
        return contract.quickLinkContractAction(contractId, "decline-offer", userId);
    }

    public String quickLinkDeclineContractOffer(Integer contractId, Integer userId, Function<ValidatableResponse, String> responseValidator) {
        return contract.quickLinkContractAction(contractId, "decline-offer", userId);
    }

    public String quickLinkAcceptContractInvitation(Integer contractId, Integer userId) {
        return contract.quickLinkContractAction(contractId, "accept-invitation", userId);
    }

    public String quickLinkAcceptContractInvitation(Integer contractId, Integer userId, Function<ValidatableResponse, String> responseValidator) {
        return contract.quickLinkContractAction(contractId, "accept-invitation", userId, responseValidator);
    }

    public String quickLinkDeclineContractInvitation(Integer contractId, Integer userId) {
        return contract.quickLinkContractAction(contractId, "decline-invitation", userId);
    }

    public String quickLinkDeclineContractInvitation(Integer contractId, Integer userId, Function<ValidatableResponse, String> responseValidator) {
        return contract.quickLinkContractAction(contractId, "decline-invitation", userId, responseValidator);
    }

    public List<ContractTaskDTO> listPrimaryContractTasks(RequestSpecification spec, Integer primaryContractId) {
        return contract.listPrimaryContractTasks(spec, primaryContractId);
    }

    public List<ContractTaskDTO> listPrimaryContractTasks(
            RequestSpecification spec,
            Integer primaryContractId,
            Function<ValidatableResponse, List<ContractTaskDTO>> responseValidator
    ) {
        return contract.listPrimaryContractTasks(spec, primaryContractId, responseValidator);
    }

    public List<ContractTaskDTO> listNegateableTasks(RequestSpecification spec, Integer primaryContractId) {
        return contract.listNegateableTasks(spec, primaryContractId);
    }

    public List<ContractTaskDTO> listNegateableTasks(
            RequestSpecification spec,
            Integer primaryContractId,
            Function<ValidatableResponse, List<ContractTaskDTO>> responseValidator
    ) {
        return contract.listNegateableTasks(spec, primaryContractId, responseValidator);
    }

    public List<ContractSummaryDTO> listPrimaryContractExtensions(
            RequestSpecification spec,
            Integer primaryContractId
    ) {
        return contract.listPrimaryContractExtensions(spec, primaryContractId);
    }

    public List<ContractSummaryDTO> listPrimaryContractExtensions(
            RequestSpecification spec,
            Integer primaryContractId,
            Function<ValidatableResponse, List<ContractSummaryDTO>> responseValidator
    ) {
        return contract.listPrimaryContractExtensions(spec, primaryContractId, responseValidator);
    }

    // =======================================================

    public static String companyEmail(String companyName, String firstName, String lastName) {
        return firstName + "." + lastName + "@" + companyNameToDomain(companyName) + ".com";
    }

    private static String companyNameToDomain(String companyName) {
        return companyName
                .replaceAll("\\s+", " ")
                .replaceAll("\\s", "-")
                .toLowerCase();
    }

    public TestProjectOwnerCompany createProjectOwnerCompany() {
        return createProjectOwnerCompany(COMPANY_NAME, RoleName.COMPANY_OWNER, true);
    }

    public TestProjectOwnerCompany createProjectOwnerCompany(String projectOwnerCompanyName) {
        return createProjectOwnerCompany(projectOwnerCompanyName, RoleName.COMPANY_OWNER, true);
    }

    public TestProjectOwnerCompany createProjectOwnerCompany(String projectOwnerCompanyName, RoleName companyOwnerRole, boolean withAdmin) {
        SignupResponseDTO signupResponseDTO = signUpWithRole(
                TestBuilder.testSignupDTO(
                        projectOwnerCompanyName, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME
                ),
                companyOwnerRole
        );

        RequestSpecification asCompanyOwner = login(companyEmail(projectOwnerCompanyName, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME));

        File temporaryFile = fileUploadTemporary(asCompanyOwner);

        NewProjectDTO newProjectDTO = TestBuilder.testNewProjectDTO("Fun Project of " + projectOwnerCompanyName, dto -> {
            dto.setDefaultImageId(temporaryFile.getId());
            return dto;
        });

        ProjectBoardDTO funProject = createProject(asCompanyOwner, newProjectDTO);

        List<LocationDetailsDTO> roots = getChildren(asCompanyOwner, funProject.getId(), null);
        if (withAdmin) {
            TeamUserDTO projectAdminDTO = inviteUserAndResetPassword(
                    asCompanyOwner,
                    TestBuilder.testTeamUserAddDTO(
                            projectOwnerCompanyName,
                            PROJECT_ADMIN_FIRST_NAME,
                            PROJECT_ADMIN_LAST_NAME
                    )
            );

            addUserToProject(asCompanyOwner, funProject.getId(), projectAdminDTO.getId());

            changeProjectAdminRole(
                    asCompanyOwner,
                    funProject.getId(),
                    projectAdminDTO.getId(),
                    testBase.roleId(RoleName.PROJECT_ADMIN)
            );

            RequestSpecification asProjectAdmin = login(companyEmail(
                    projectOwnerCompanyName,
                    PROJECT_ADMIN_FIRST_NAME,
                    PROJECT_ADMIN_LAST_NAME
            ));
            return new TestProjectOwnerCompany(
                    signupResponseDTO.getCompanyId(), signupResponseDTO.getUserId(), projectAdminDTO.getId(), funProject.getId(),
                    roots.get(0).getId(), asCompanyOwner,
                    asProjectAdmin
            );
        }
        else {
            return new TestProjectOwnerCompany(
                    signupResponseDTO.getCompanyId(), signupResponseDTO.getUserId(), signupResponseDTO.getUserId(), funProject.getId(),
                    roots.get(0).getId(), asCompanyOwner,
                    asCompanyOwner
            );

        }

    }

    public TestProjectContractorCompany createProjectContractorCompany() {
        return createProjectContractorCompany(CONTRACTOR_COMPANY_NAME);
    }

    public TestProjectContractorCompany createProjectContractorCompany(String contractorCompanyName) {
        SignupResponseDTO signupResponseDTO = signUp(
                TestBuilder.testSignupDTO(
                        contractorCompanyName, PROJECT_MANAGER_FIRST_NAME, PROJECT_MANAGER_LAST_NAME,
                        dto -> dto.toBuilder().company(
                                dto.getCompany().toBuilder()
                                  .tradeIds(testBase.tradeIds("PAINTERS"))
                                .build()
                        ).build()
                )
        );

        RequestSpecification asProjectManager = login(companyEmail(contractorCompanyName, PROJECT_MANAGER_FIRST_NAME, PROJECT_MANAGER_LAST_NAME));

        TeamUserDTO userDTO = inviteUserAndResetPassword(
                asProjectManager,
                TestBuilder.testTeamUserAddDTO(contractorCompanyName, PROJECT_WORKER_FIRST_NAME, PROJECT_WORKER_LAST_NAME)
        );

        RequestSpecification asProjectWorker = login(companyEmail(contractorCompanyName, PROJECT_WORKER_FIRST_NAME, PROJECT_WORKER_LAST_NAME));

        TeamUserDTO anotherWorkerDTO = inviteUserAndResetPassword(
                asProjectManager,
                TestBuilder.testTeamUserAddDTO(contractorCompanyName, ANOTHER_PROJECT_WORKER_FIRST_NAME, ANOTHER_PROJECT_WORKER_LAST_NAME)
        );

        return new TestProjectContractorCompany(signupResponseDTO.getCompanyId(), signupResponseDTO.getUserId(), userDTO.getId(), anotherWorkerDTO.getId(), asProjectManager, asProjectWorker);
    }

    public TestProject createProject() {
        return createProject(COMPANY_NAME, RoleName.COMPANY_OWNER, true);
    }

    public TestProject createProject(String projectOwnerCompanyName, RoleName companyOwnerRole, boolean withAdmin) {
        return createProject(projectOwnerCompanyName, CONTRACTOR_COMPANY_NAME, companyOwnerRole, withAdmin);
    }

    public TestProject createProject(String projectOwnerCompanyName, String projectContractorName, RoleName companyOwnerRole, boolean withAdmin) {
        TestProjectOwnerCompany ownerCompany = createProjectOwnerCompany(projectOwnerCompanyName, companyOwnerRole, withAdmin);
        TestProjectContractorCompany contractorCompany = createProjectContractorCompany(projectContractorName);

        inviteCompanyToProject(ownerCompany.asAdmin, ownerCompany.projectId, contractorCompany.companyId);

        MainTaskDTO mainTaskDTO = addContractualTask(ownerCompany.asAdmin, TestBuilder.testCreateMainTaskRequest(
                ownerCompany.topLocationId,
                "Main Task",
                contractorCompany.companyId
        ));

        addUserToProject(contractorCompany.asOwner, ownerCompany.projectId, contractorCompany.workerId);

        SubTaskDTO subTaskDTO = addSubTask(contractorCompany.asOwner, TestBuilder.testCreateSubTaskRequest(
                mainTaskDTO.getId(),
                "Sub Task"
        ));

        return new TestProject(
                ownerCompany,
                contractorCompany,
                mainTaskDTO.getId(),
                subTaskDTO.getId()
        );
    }
    public ExtendedTestProject createExtendedProject() {
        return createExtendedProject(COMPANY_NAME, CONTRACTOR_COMPANY_NAME);
    }

    public ExtendedTestProject createExtendedProject(String projectOwnerCompanyName, String contractorCompanyName) {
        TestProjectOwnerCompany ownerCompany = createProjectOwnerCompany(projectOwnerCompanyName);
        TestProjectContractorCompany contractorCompany = createProjectContractorCompany(contractorCompanyName);

        inviteCompanyToProject(ownerCompany.asAdmin, ownerCompany.projectId, contractorCompany.companyId);

        addUserToProject(contractorCompany.asOwner, ownerCompany.projectId, contractorCompany.workerId);
        if (contractorCompany.anotherWorkerId != null) {
            addUserToProject(contractorCompany.asOwner, ownerCompany.projectId, contractorCompany.anotherWorkerId);
        }


        return new ExtendedTestProject(ownerCompany, contractorCompany, ownerCompany.projectId);
    }

    public @Nullable CompanyDirectoryDTO directoryLookup(String companyName, RequestSpecification spec) {
        ValidatableResponse r = given().spec(spec).get(endPoint("/companies/directory")).then();
        r.assertThat().statusCode(HttpStatus.OK.value());
        List<CompanyDirectoryDTO> entries = r.extract().body().jsonPath().getList("", CompanyDirectoryDTO.class);
        return entries.stream().filter(entry -> entry.getName().equals(companyName)).findFirst().orElse(null);
    }

    public ExtendedTestProject nameTopLocationOfExtendedProject(ExtendedTestProject project, String newLocationName) {
        project.addLocation(newLocationName, project.ownerCompany.topLocationId);

        return project;
    }
    public ExtendedTestProject addLocationToExtendedProject(ExtendedTestProject project, String newLocationName) {
        TestProjectOwnerCompany ownerCompany = project.ownerCompany;

        LocationDetailsDTO locationDetailsDTO = createLocation(ownerCompany.asAdmin, TestBuilder.testLocationCreateDTO(ownerCompany.projectId, newLocationName));

        project.addLocation(newLocationName, locationDetailsDTO.getId());

        return project;
    }

    public ExtendedTestProject addSubLocationToExtendedProject(ExtendedTestProject project, String parentLocationName, String newLocationName) {
        TestProjectOwnerCompany ownerCompany = project.ownerCompany;

        LocationDetailsDTO locationDetailsDTO = createLocation(ownerCompany.asAdmin, TestBuilder.testLocationCreateDTO(
                ownerCompany.projectId, newLocationName,
                l -> l.toBuilder().parent(project.locationIDs.get(parentLocationName)).build()
        ));

        project.addLocation(newLocationName, locationDetailsDTO.getId());

        return project;
    }

    public static class TestProjectOwnerCompany {
        public final Integer companyId;
        public final Integer ownerId;
        public final Integer adminId;
        public final Integer projectId;
        public final Integer topLocationId;

        public final RequestSpecification asOwner;
        public final RequestSpecification asAdmin;

        TestProjectOwnerCompany(
                Integer companyId,
                Integer ownerId,
                Integer adminId,
                Integer projectId,
                Integer topLocationId,
                RequestSpecification asOwner,
                RequestSpecification asAdmin
        ) {
            this.companyId = companyId;
            this.ownerId = ownerId;
            this.adminId = adminId;
            this.projectId = projectId;
            this.topLocationId = topLocationId;
            this.asOwner = asOwner;
            this.asAdmin = asAdmin;
        }
    }

    public static class TestProjectContractorCompany {
        public final Integer companyId;
        public final Integer ownerId;
        public final Integer workerId;
        public final Integer anotherWorkerId;

        public final RequestSpecification asOwner;
        public final RequestSpecification asWorker;

        TestProjectContractorCompany(Integer companyId, Integer ownerId, Integer workerId, RequestSpecification asOwner, RequestSpecification asWorker) {
            this(companyId, ownerId, workerId, null, asOwner, asWorker);
        }

        TestProjectContractorCompany(Integer companyId, Integer ownerId, Integer workerId, Integer anotherWorkerId, RequestSpecification asOwner, RequestSpecification asWorker) {
            this.companyId = companyId;
            this.ownerId = ownerId;
            this.workerId = workerId;
            this.anotherWorkerId = anotherWorkerId;
            this.asOwner = asOwner;
            this.asWorker = asWorker;
        }
    }

    public static class TestProject {
        public final TestProjectOwnerCompany ownerCompany;
        public final TestProjectContractorCompany contractorCompany;

        public final Integer mainTaskId;
        public final Integer subTaskId;

        TestProject(TestProjectOwnerCompany ownerCompany, TestProjectContractorCompany contractorCompany, Integer mainTaskId, Integer subTaskId) {
            this.ownerCompany = ownerCompany;
            this.contractorCompany = contractorCompany;
            this.mainTaskId = mainTaskId;
            this.subTaskId = subTaskId;
        }
    }

    public static class ExtendedTestProject {
        public final TestProjectOwnerCompany ownerCompany;
        public final TestProjectContractorCompany contractorCompany;

        public final Integer projectId;

        public final Map<String, Integer> locationIDs;
        public final Map<String, Integer> mainTaskIDs;
        public final Map<String, Integer> subTaskIDs;
        public final Map<String, Integer> contractIDs;

        public ExtendedTestProject(TestProjectOwnerCompany ownerCompany, TestProjectContractorCompany contractorCompany, Integer projectId) {
            this.ownerCompany = ownerCompany;
            this.contractorCompany = contractorCompany;
            this.projectId = projectId;

            this.locationIDs = new HashMap<>();
            this.mainTaskIDs = new HashMap<>();
            this.subTaskIDs = new HashMap<>();
            this.contractIDs = new HashMap<>();
        }

        public ExtendedTestProject addLocation(String locationName, Integer locationId) {
            locationIDs.put(locationName, locationId);
            return this;
        }

        public ExtendedTestProject addMainTask(String mainTaskName, Integer mainTaskId) {
            mainTaskIDs.put(mainTaskName, mainTaskId);
            return this;
        }

        public ExtendedTestProject addSubTask(String subTaskName, Integer subTaskId) {
            subTaskIDs.put(subTaskName, subTaskId);
            return this;
        }

        public ExtendedTestProject addContract(String contractName, Integer contractId) {
            contractIDs.put(contractName, contractId);
            return this;
        }

        public Integer locationId(String locationName) {
            return locationIDs.get(locationName);
        }

        public Integer mainTaskId(String mainTaskName) {
            return mainTaskIDs.get(mainTaskName);
        }

        public Integer subTaskId(String subTaskName) {
            return subTaskIDs.get(subTaskName);
        }

        public Integer contractId(String contractName) {
            return contractIDs.get(contractName);
        }
    }
}
