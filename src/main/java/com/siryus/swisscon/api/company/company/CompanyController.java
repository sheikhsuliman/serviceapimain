package com.siryus.swisscon.api.company.company;

import com.naturalprogrammer.spring.lemon.commonsweb.util.LecwUtils;
import com.siryus.swisscon.api.auth.LemonService;
import com.siryus.swisscon.api.auth.user.CompanyInviteDTO;
import com.siryus.swisscon.api.company.bankaccount.BankAccountDTO;
import com.siryus.swisscon.api.company.bankaccount.BankAccountService;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController("companyController")
@Api(
        tags = "Companies",
        produces = "application/json, application/hal+json, application/vnd.api+json"
)
@RequestMapping("/api/rest/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final LemonService lemonService;
    private final BankAccountService bankAccountService;
    private final CompanyUserRoleService companyUserRoleService;

    @Autowired
        public CompanyController(
            CompanyService companyService,
            LemonService lemonService,
            BankAccountService bankAccountService,
            CompanyUserRoleService companyUserRoleService) {
        this.companyService = companyService;
        this.lemonService = lemonService;
        this.bankAccountService = bankAccountService;
        this.companyUserRoleService = companyUserRoleService;
    }

    @GetMapping(value = {"/{id}/details-team"})
    @ApiOperation(value = "Details/Team of a company", notes = "Get the team of a company", httpMethod = "GET")
    //@PreAuthorize("hasPermission(#id, 'com.siryus.swisscon.api.company.company.Company', 'COMPANY_TEAM_READ_LIST')")
    //TODO reolver doesn't work when get details of other company
    public CompanyDetailsDTO getTeam(
            @ApiParam(name = "id", required = true, value = "string") @PathVariable Integer id) {
        return companyUserRoleService.getCompanyTeam(id);
    }

    @PostMapping(value = "/{companyId}/remove-user" )
    @ApiOperation(value = "Remove a user from the company team. It's only possible if the user is not part of a project", httpMethod = "POST")
    @PreAuthorize("hasPermission(#companyId, 'COMPANY', 'COMPANY_TEAM_ARCHIVE')")
    public void removeUser(
            @PathVariable Integer companyId,
            @RequestParam(name = "user") Integer userId) {

        companyService.removeUserFromCompany(companyId, userId);
    }

    @PostMapping(value = "/{companyId}/remove-company")
    @ApiOperation(value = "Remove the company. It's only possible if you are the owner and there are no other users part of the company")
    @PreAuthorize("hasPermission(#companyId, 'COMPANY', 'COMPANY_ARCHIVE')")
    public void removeCompany(
            @PathVariable Integer companyId
    ) {
        companyService.removeCompany(companyId);
    }
    
    @PostMapping(value = "/{companyId}/contact-details")
    @PreAuthorize("hasPermission(#companyId, 'COMPANY', 'COMPANY_UPDATE')")
    public ContactDetailDTO updateContactDetails(
            @PathVariable Integer companyId,
            @RequestBody ContactDetailDTO contact
    ) {
        return  companyService.updateContactDetails(companyId, contact);
    }

    @GetMapping(value = "/{companyId}/contact-details")
    @PreAuthorize("hasPermission(#companyId, 'COMPANY', 'COMPANY_READ_PROFILE')")
    public ContactDetailDTO getContactDetails(@PathVariable Integer companyId) {
        return companyService.getContactDetails(companyId);
    }

    @PostMapping(value = "/{companyId}/profile")
    @ApiOperation(value = "Company profile", notes = "Update the company profile")
    @PreAuthorize("hasPermission(#companyId, 'COMPANY', 'COMPANY_UPDATE')")
    public CompanyProfileDTO updateCompanyProfile(
            @PathVariable Integer companyId,
            @RequestBody CompanyProfileDTO profileData
    ) {
        return companyService.updateCompanyProfile(companyId, profileData);
    }

    @PostMapping(value = "/{companyId}/registration-details")
    @ApiOperation(value = "This will update the finanacial details of the company")
    @PreAuthorize("hasPermission(#companyId, 'COMPANY', 'COMPANY_UPDATE')")
    public RegistrationDetailsDTO saveRegistrationDetails(
            @PathVariable Integer companyId,
            @RequestBody RegistrationDetailsDTO registrationDetailsDTO
    ) {
        return companyService.saveRegistrationDetails(companyId, registrationDetailsDTO);
    }

    @GetMapping(value = "/{companyId}/bank-accounts")
    @ApiOperation(value = "Will load all the companies bank accounts")
    @PreAuthorize("hasPermission(#companyId, 'COMPANY', 'COMPANY_READ_FINANCIAL_DETAILS')")
    public List<BankAccountDTO> getBankAccounts(
            @PathVariable Integer companyId
    ) {
        return bankAccountService.getBankAccounts(companyId);
    }

    @PostMapping(value = "/{companyId}/add-bank-account")
    @ApiOperation(value = "Will add a new bank account to the company")
    @PreAuthorize("hasPermission(#companyId, 'COMPANY', 'COMPANY_UPDATE')")
    public BankAccountDTO addBankAccount(
            @PathVariable Integer companyId,
            @RequestBody BankAccountDTO bankAccountDTO
    ) {
        return bankAccountService.addBankAccount(companyId, bankAccountDTO);
    }

    @PostMapping(value = "/{companyId}/edit-bank-account")
    @ApiOperation(value = "Will edit an existing bank account")
    @PreAuthorize("hasPermission(#companyId, 'COMPANY', 'COMPANY_UPDATE')")
    public BankAccountDTO editBankAccount(
            @ApiParam(name = "companyId", required = true, value = "string") @PathVariable Integer companyId,
            @RequestBody @NotNull BankAccountDTO bankAccountDTO
    ) {
        return bankAccountService.editBankAccount(companyId, bankAccountDTO);
    }

    @PostMapping(value = "/{companyId}/delete-bank-account/{bankAccountId}")
    @ApiOperation(value = "Will delete an existing bank account")
    @PreAuthorize("hasPermission(#companyId, 'COMPANY', 'COMPANY_UPDATE')")
    public void deleteBankAccount(
            @PathVariable Integer companyId,
            @PathVariable Integer bankAccountId
    ) {
        bankAccountService.deleteBankAccount(companyId, bankAccountId);
    }

    @GetMapping(value = "/directory")
    @ApiOperation(value = "The directory with all active registered companies")
    public List<CompanyDirectoryDTO> directory() {
        return companyService.directory();
    }

    @GetMapping(value = "/contractors")
    @ApiOperation(value = "The directory with all active registered contractors companies")
    public List<CompanyDirectoryDTO> contractors() {
        return companyService.contractors();
    }

    @GetMapping(value = "/customers")
    @ApiOperation(value = "The directory with all active registered customer companies")
    public List<CompanyDirectoryDTO> customers() {
        return companyService.customers();
    }

    @Deprecated //TODO remove after SI-177
    @PostMapping(value = "/invite-company")
    @ApiOperation(value = "This will send an invitation mail or sms with signup link")
    @PreAuthorize("hasPermission(0, 'COMPANY', 'COMPANY_INVITE')")
    public void inviteCompanyDeprecated(@RequestBody CompanyInviteDTO companyInviteDTO ) {
        lemonService.inviteCompanyDeprecated(companyInviteDTO);
    }

    @PostMapping(value = "/invite-company-and-user")
    @ApiOperation(value = "This will add the company with owner and send mail or sms with signup link")
    @PreAuthorize("hasPermission(0, 'COMPANY', 'COMPANY_INVITE')")
    public Integer inviteCompany(@RequestBody CompanyInviteDTO companyInviteDTO ) {
        return lemonService.inviteCompany(companyInviteDTO);
    }

    @GetMapping("/{companyId}/permissions")
    @ApiOperation(value = "Get a list of all permissions the user has for this particular company")
    public List<Integer> getUserCompanyPermissions(
        @PathVariable Integer companyId
    ) {
        return companyService.getUserCompanyPermissions(Integer.valueOf(LecwUtils.currentUser().getId()), companyId);
    }
}
