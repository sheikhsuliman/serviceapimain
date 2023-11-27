package com.siryus.swisscon.api.base;

import com.naturalprogrammer.spring.lemon.commons.domain.ResetPasswordForm;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.signup.SignupCompanyDTO;
import com.siryus.swisscon.api.auth.signup.SignupDTO;
import com.siryus.swisscon.api.auth.signup.SignupUserDTO;
import com.siryus.swisscon.api.auth.user.CompanyInviteDTO;
import com.siryus.swisscon.api.auth.user.TeamUserAddDTO;
import com.siryus.swisscon.api.auth.user.UserProfileDTO;
import com.siryus.swisscon.api.company.bankaccount.BankAccountDTO;
import com.siryus.swisscon.api.contract.dto.ContractAddTasksRequest;
import com.siryus.swisscon.api.contract.dto.ContractOrderBy;
import com.siryus.swisscon.api.contract.dto.ContractUpdateTaskRequest;
import com.siryus.swisscon.api.contract.dto.CreateContractCommentRequest;
import com.siryus.swisscon.api.contract.dto.CreateContractRequest;
import com.siryus.swisscon.api.contract.dto.ListContractsRequest;
import com.siryus.swisscon.api.contract.dto.SendMessageRequest;
import com.siryus.swisscon.api.contract.dto.UpdateContractRequest;
import com.siryus.swisscon.api.customroles.CustomRoleReader;
import com.siryus.swisscon.api.customroles.dto.CustomPermissionDTO;
import com.siryus.swisscon.api.customroles.dto.CustomRoleDTO;
import com.siryus.swisscon.api.general.unit.UnitRepository;
import com.siryus.swisscon.api.location.location.LocationCreateDTO;
import com.siryus.swisscon.api.project.project.AddressDTO;
import com.siryus.swisscon.api.project.project.EditProjectDTO;
import com.siryus.swisscon.api.project.project.NewProjectDTO;
import com.siryus.swisscon.api.tasks.dto.AddTaskChecklistItemRequest;
import com.siryus.swisscon.api.tasks.dto.CreateCommentDTO;
import com.siryus.swisscon.api.tasks.dto.CreateCompanyCatalogItemRequest;
import com.siryus.swisscon.api.tasks.dto.CreateContractualTaskRequest;
import com.siryus.swisscon.api.tasks.dto.CreateDirectorialTaskRequest;
import com.siryus.swisscon.api.tasks.dto.CreateSpecificationRequest;
import com.siryus.swisscon.api.tasks.dto.CreateSubTaskRequest;
import com.siryus.swisscon.api.tasks.dto.ListTasksRequest;
import com.siryus.swisscon.api.tasks.dto.TaskStatus;
import com.siryus.swisscon.api.tasks.dto.UpdateDirectorialTaskRequest;
import com.siryus.swisscon.api.taskworklog.dto.RejectTaskRequest;
import com.siryus.swisscon.api.taskworklog.dto.TaskWorklogRequest;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Component
public class TestBuilder {

    @Value("${lemon.default-signup-token}")
    protected String defaultSignupToken;

    private final UnitRepository unitRepository;
    private final CustomRoleReader customRoleReader;

    @Autowired
    public TestBuilder(UnitRepository unitRepository, CustomRoleReader customRoleReader) {
        this.unitRepository = unitRepository;
        this.customRoleReader = customRoleReader;
    }


    @SafeVarargs
    @Deprecated //TODO delete after SI-177
    public static SignupDTO testSignupDTO(String companyName, String ownerFirstName, String ownerLastName, String linkEmail, String linkCode, UnaryOperator<SignupDTO>... customizers) {
        return customize( SignupDTO.builder()
                        .company(
                                SignupCompanyDTO.builder()
                                        .name(companyName)
                                        .numberOfEmployeesId(2)     // 2_9
                                        .countryId(1)
                                        .tradeIds(Collections.singletonList(2)) // ARCHITECT
                                        .build()
                        )
                        .user(
                                SignupUserDTO.builder()
                                        .title("Von")
                                        .firstName(ownerFirstName)
                                        .lastName(ownerLastName)
                                        .password(TestHelper.PASSWORD)
                                        .genderId(3)    // UNDISCLOSED
                                        .language(TestHelper.TEST_LANGUAGE)
                                        .email(TestHelper.companyEmail(companyName, ownerFirstName, ownerLastName))
                                        .build()
                        )
                        .linkEmailOrPhone(linkEmail)
                        .linkCode(linkCode)
                        .acceptConditions(true)
                        .optOutPromo(true)
                        .build(),
                customizers
        );
    }

    @SafeVarargs
    @Deprecated //TODO delete after SI-177
    public static SignupDTO testSignupDTO(String companyName, String ownerFirstName, String ownerLastName, UnaryOperator<SignupDTO>... customizers) {
        return testSignupDTO(companyName, ownerFirstName, ownerLastName, TestHelper.companyEmail(companyName, ownerFirstName, ownerLastName), null, customizers);
    }

    public SignupDTO testSignupDTOWithoutInvite(String companyName, String ownerFirstName, String ownerLastName, String email) {
        return testSignupDTO(null, companyName, ownerFirstName, ownerLastName, defaultSignupToken + "." + email);
    }

    public static SignupDTO testSignupDTO(Integer companyId, String companyName, String ownerFirstName, String ownerLastName, String code) {
        return SignupDTO.builder()
                .company(
                        SignupCompanyDTO.builder()
                                .companyId(companyId)
                                .name(companyName)
                                .numberOfEmployeesId(2)     // 2_9
                                .countryId(1)
                                .tradeIds(Collections.singletonList(2)) // ARCHITECT
                                .build()
                )
                .user(
                        SignupUserDTO.builder()
                                .title("Von")
                                .firstName(ownerFirstName)
                                .lastName(ownerLastName)
                                .password(TestHelper.PASSWORD)
                                .genderId(3)    // UNDISCLOSED
                                .language(TestHelper.TEST_LANGUAGE)
                                .build()
                )
                .linkCode(code)
                .acceptConditions(true)
                .optOutPromo(true)
                .build();
    }

    public static ResetPasswordForm testResetPasswordForm(String code, String newPassword) {
        ResetPasswordForm resetPasswordForm = new ResetPasswordForm();
        resetPasswordForm.setCode(code);
        resetPasswordForm.setNewPassword(newPassword);
        return resetPasswordForm;
    }

    @SafeVarargs
    public static UserProfileDTO testUserProfileDTO(UnaryOperator<UserProfileDTO>... customizers) {
        UserProfileDTO dto = UserProfileDTO.builder()
                .title("ms.")
                .aboutMe("aboutMe")
                .driversLicense("987654")
                .car("car")
                .licensePlate("123456")
                .responsibility("responsibility")
                .genderId(2)
                .firstName("first name")
                .lastName("last name")
                .birthDate(Date.valueOf(LocalDate.now().minusYears(30)))
                .ssn("111111")
                .countryOfResidenceId(3)
                .nationalityId(3)
                .build();
        return customize(dto, customizers);
    }

    public static TeamUserAddDTO testTeamUserAddDTO(Integer countryCode, String phone) {
        return TeamUserAddDTO.builder()
                .firstName("first name")
                .lastName("last name")
                .countryCode(countryCode)
                .emailOrPhone(phone)
                .build();
    }

    public static TeamUserAddDTO testTeamUserAddDTO(String email) {
        return TeamUserAddDTO.builder()
                .firstName("first name")
                .lastName("last name")
                .emailOrPhone(email)
                .build();
    }

    public static TeamUserAddDTO testTeamUserAddDTO(String companyName, String firstName, String lastName) {
        return TeamUserAddDTO.builder()
                .firstName(firstName)
                .lastName(lastName)
                .emailOrPhone(TestHelper.companyEmail(companyName, firstName, lastName))
                .build();
    }

    @Deprecated //TODO remove after SI-177
    public static CompanyInviteDTO testCompanyInviteDTOdeprecated(Integer countryCode, String phone) {
        return CompanyInviteDTO.builder()
                .emailOrPhone(phone)
                .countryCode(countryCode)
                .build();
    }

    @Deprecated //TODO remove after SI-177
    public static CompanyInviteDTO testCompanyInviteDTOdeprecated(String email) {
        return CompanyInviteDTO.builder()
                .emailOrPhone(email)
                .build();
    }

    public CompanyInviteDTO testCompanyInviteDTO(String companyName, Integer countryCode, String phone, RoleName roleName) {
        return CompanyInviteDTO.builder()
                .companyName(companyName)
                .emailOrPhone(phone)
                .countryCode(countryCode)
                .roleId(customRoleReader.getRoleByName(roleName.name()).getId())
                .build();
    }

    public CompanyInviteDTO testCompanyInviteDTO(String companyName, String email, RoleName roleName) {
        return CompanyInviteDTO.builder()
                .companyName(companyName)
                .emailOrPhone(email)
                .roleId(customRoleReader.getRoleByName(roleName.name()).getId())
                .build();
    }


    @SafeVarargs
    public static NewProjectDTO testNewProjectDTO(String projectName, UnaryOperator<NewProjectDTO>... customizers) {
        return customize(NewProjectDTO.builder()
                .name(projectName)
                .typeId(1)
                .description("new description")
                .latitude("123456")
                .longitude("456789")
                .startDate(ZonedDateTime.now().minusDays(20).withNano(0))
                .endDate(ZonedDateTime.now().plusDays(20).withNano(0))
                .address(AddressDTO.builder()
                        .city("Someville")
                        .address("Random Street")
                        .postalCode("666")
                        .countryId(1)
                        .build())
                .build(),
                customizers
        );
    }

    @SafeVarargs
    public static EditProjectDTO testEditProjectDTO(UnaryOperator<EditProjectDTO>... customizers) {
        return customize(EditProjectDTO.builder()
                .name("edited name")
                .typeId(2)
                .latitude("111222")
                .longitude("222111")
                .description("edited description")
                .startDate(ZonedDateTime.now().minusDays(30).withNano(0))
                .endDate(ZonedDateTime.now().plusDays(30).withNano(0))
                .address(AddressDTO.builder()
                        .city("edit ville")
                        .address("edit street")
                        .postalCode("123")
                        .countryId(2)
                        .build())
                .build(), customizers);
    }

    @SafeVarargs
    public static LocationCreateDTO testLocationCreateDTO(Integer projectId, String name, UnaryOperator<LocationCreateDTO>... customizers) {
        return testLocationCreateDTO(projectId, null, name, customizers);
    }

    @SafeVarargs
    public static LocationCreateDTO testLocationCreateDTO(Integer projectId, Integer parentLocationId, String name, UnaryOperator<LocationCreateDTO>... customizers) {
        return customize( LocationCreateDTO.builder()
                .projectId(projectId)
                .parent(parentLocationId)
                .name(name)
                .build(),
                customizers
        );
    }

    @SafeVarargs
    public static CreateContractualTaskRequest testCreateMainTaskRequest(
            Integer locationId,
            String title,
            Integer contractorCompanyId,
            UnaryOperator<CreateContractualTaskRequest>... customizers

    ) {
        return customize(CreateContractualTaskRequest.builder()
                .locationId(locationId)
                .title(title)
                .description("Some description")
                .startDate(LocalDateTime.now().minusDays(10))
                .dueDate(LocalDateTime.now().plusDays(10))
                .timeBudgetMinutes(120)
                .companyCatalogItem(CreateCompanyCatalogItemRequest.builder()
                        .companyId(contractorCompanyId)
                        .snpNumber("100.100.100.100.100")
                        .variationNumber(1)
                        .companyDetails(null)
                        .price(BigDecimal.TEN)
                        .build()
                )
                .specification(CreateSpecificationRequest.builder()
                        .variation(null)
                        .amount(BigDecimal.TEN)
                        .price(BigDecimal.ONE)
                        .build()
                )
                .build(),
                customizers
        );
    }

    @SafeVarargs
    public static CreateDirectorialTaskRequest testCreateDirectorialTaskRequest(
            Integer locationId,
            String title,
            Integer contractorCompanyId,
            UnaryOperator<CreateDirectorialTaskRequest>... customizers

    ) {
        return customize(CreateDirectorialTaskRequest.builder()
                        .locationId(locationId)
                        .title(title)
                        .companyId(contractorCompanyId)
                        .description("Some description")
                        .startDate(LocalDateTime.now().minusDays(10))
                        .dueDate(LocalDateTime.now().plusDays(10))
                        .timeBudgetMinutes(120)
                        .tradeCatalogNodeId(1)
                        .attachmentIDs(Collections.emptyList())
                        .requiresContract(false)
                        .build(),
                customizers
        );
    }

    @SafeVarargs
    public static UpdateDirectorialTaskRequest testUpdateDirectorialTaskRequest(
            Integer locationId,
            String title,
            String description,
            UnaryOperator<UpdateDirectorialTaskRequest>... customizers

    ) {
        return customize(
                UpdateDirectorialTaskRequest.builder()
                                 .locationId(locationId)
                                 .title(title)
                                 .description(description)
                                 .startDate(LocalDateTime.now().minusDays(10))
                                 .dueDate(LocalDateTime.now().plusDays(10))
                                 .timeBudgetMinutes(120)
                                 .tradeCatalogNodeId(1)
                                 .build(),
                customizers
        );
    }

    @SafeVarargs
    public static CreateSubTaskRequest testCreateSubTaskRequest(Integer mainTaskId, String title, UnaryOperator<CreateSubTaskRequest>... customizers) {
        return customize(
                CreateSubTaskRequest.builder()
                        .mainTaskId(mainTaskId)
                        .title(title)
                        .description("Some description")
                        .timeBudgetMinutes(42)
                        .taskTeam(new ArrayList<>())
                        .build(),
                customizers
        );
    }

    @SafeVarargs
    public static TaskWorklogRequest testTaskWorklogRequest(Integer mainTaskId, Integer subTaskId, String comment, UnaryOperator<TaskWorklogRequest>... customizers) {
        return customize(
                TaskWorklogRequest.builder()
                        .mainTaskId(mainTaskId)
                        .subTaskId(subTaskId)
                        .comment(comment)
                        .build(),
                customizers
        );
    }

    @SafeVarargs
    public static RejectTaskRequest testRejectTaskRequest(ZonedDateTime dueDate, Integer mainTaskId, Integer subTaskId, String comment, UnaryOperator<TaskWorklogRequest>... customizers) {
        TaskWorklogRequest customize = customize(
                TaskWorklogRequest.builder()
                        .mainTaskId(mainTaskId)
                        .subTaskId(subTaskId)
                        .comment(comment)
                        .build(),
                customizers
        );
        return RejectTaskRequest.builder()
                .dueDate(dueDate)
                .worklogRequest(customize)
                .build();
    }

    public static AddTaskChecklistItemRequest testAddTaskChecklistItemRequest(String label) {
        return new AddTaskChecklistItemRequest(label);
    }

    @SafeVarargs
    public static CreateCommentDTO testCreateCommentDTO(String comment, UnaryOperator<CreateCommentDTO>... customizers) {
        return customize(
                CreateCommentDTO.builder()
                        .comment(comment)
                        .build(),
                customizers
        );
    }

    @SafeVarargs
    public static ListTasksRequest testListTasksRequest(UnaryOperator<ListTasksRequest>... customizers) {
        return customize(
                ListTasksRequest.builder()
                        .locationIDs(Collections.emptyList())
                        .snpIDs(Collections.emptyList())
                        .companyIDs(Collections.emptyList())
                        .tradeIDs(Collections.emptyList())
                        .build(),
                customizers
        );
    }

    @SafeVarargs
    public static ListTasksRequest testListTasksRequestWithLocation( Integer locationId, UnaryOperator<ListTasksRequest>... customizers) {
        return customize(
                ListTasksRequest.builder()
                        .locationIDs(Collections.singletonList(locationId))
                        .snpIDs(Collections.emptyList())
                        .companyIDs(Collections.emptyList())
                        .tradeIDs(Collections.emptyList())
                        .build(),
                customizers
        );
    }

    @SafeVarargs
    public static ListTasksRequest testListTasksRequestWithUser( Integer userId, UnaryOperator<ListTasksRequest>... customizers) {
        return customize(
                ListTasksRequest.builder()
                        .locationIDs(Collections.emptyList())
                        .snpIDs(Collections.emptyList())
                        .companyIDs(Collections.emptyList())
                        .tradeIDs(Collections.emptyList())
                        .userID(userId)
                        .build(),
                customizers
        );
    }

    @SafeVarargs
    public static ListTasksRequest testListTasksRequestWithStatus(TaskStatus status, UnaryOperator<ListTasksRequest>... customizers) {
        return customize(
                ListTasksRequest.builder()
                        .locationIDs(Collections.emptyList())
                        .snpIDs(Collections.emptyList())
                        .companyIDs(Collections.emptyList())
                        .tradeIDs(Collections.emptyList())
                        .statuses(Collections.singletonList(status))
                        .build(),
                customizers
        );
    }

    public static BankAccountDTO testBankAccountDTO() {
        return BankAccountDTO.builder()
                .bankName("bank name")
                .beneficiaryName("beneficiary name")
                .bic("BIG XYZ")
                .currencyId("CHF")
                .iban("CH9300762011623852957")
                .build();
    }

    @SafeVarargs
    public static BankAccountDTO testBankAccountDTO(UnaryOperator<BankAccountDTO>... customizers) {
        return customize(BankAccountDTO.builder()
                .bankName("bank name")
                .beneficiaryName("beneficiary name")
                .bic("BIG XYZ")
                .currencyId("CHF")
                .iban("CH9300762011623852957")
                .build(), customizers);
    }

    @SafeVarargs
    public static CustomRoleDTO testCustomRole(
            String name,
            List<CustomPermissionDTO> permissions,
            UnaryOperator<CustomRoleDTO>... customizers
    ) {
        return customize(
                new CustomRoleDTO(
                        null, name, name,
                        false, false, false,
                        false, false, false, false,
                        permissions
                ),
                customizers
        );
    }

    public static List<CustomPermissionDTO> testCustomPermissions(Integer... permissionIds) {
        return Arrays.stream(permissionIds).map(
                id -> new CustomPermissionDTO(id, "PERMISSION_" + id, "Permission " + id, false, false, false)
        ).collect(Collectors.toList());
    }

    public static CreateContractRequest testCreateContractRequest(String contractName, Integer projectId) {
        return testCreateContractRequest(null, contractName, projectId);
    }

    public static CreateContractRequest testCreateContractRequest(Integer primaryContractId, String contractName, Integer projectId) {
        return new CreateContractRequest(primaryContractId, projectId, contractName, null, null);
    }

    public static ListContractsRequest testCreateListContractRequest(Integer projectId, Integer contractorId) {
        return ListContractsRequest.builder()
                .contractorIdFilter(contractorId)
                .projectId(projectId)
                .orderAscending(false)
                .orderBy(ContractOrderBy.CONTRACTOR)
                .build();
    }
    
    public static CreateContractCommentRequest testCreateContractCommentRequest(Integer contractId, String text, Integer fileId) {        
        return new CreateContractCommentRequest(contractId, text, fileId);
    }

    public static UpdateContractRequest testUpdateContractRequest(String contractName, String description, LocalDateTime deadline) {
        return new UpdateContractRequest(contractName, description, deadline);
    }

    public static ContractAddTasksRequest testCreateContractAddTasksRequest(Integer contractId, List<Integer> taskIds) {
        return ContractAddTasksRequest.builder()
                .contractId(contractId)
                .taskIds(taskIds)
                .build();
    }

    public ContractUpdateTaskRequest testCreateContractUpdateTaskRequest(Integer contractTaskId, String unitSymbol, String amount, String pricePerUnit) {
        return ContractUpdateTaskRequest
                .builder()
                .contractTaskId(contractTaskId)
                .unitId(unitRepository.findBySymbolName(unitSymbol).getId())
                .amount(amount != null ? new BigDecimal(amount) : null)
                .pricePerUnit(pricePerUnit != null ? new BigDecimal(pricePerUnit) : null)
                .build();
    }

    public static SendMessageRequest testSendMessageRequest(Integer companyId, String message) {
        return SendMessageRequest.builder()
                .recipientCompanyId(companyId)
                .messageText(message)
                .build();
    }

    private static <T>  T customize(T dto, UnaryOperator<T>[] customizers) {
        T customizedDto = dto;
        for( var customizer : customizers ){
            customizedDto = customizer.apply(customizedDto);
        }
        return customizedDto;
    }

    public static byte[] readBytes(String filePath) throws IOException {
        return IOUtils.toByteArray(TestBuilder.class.getResourceAsStream(filePath));
    }

}
