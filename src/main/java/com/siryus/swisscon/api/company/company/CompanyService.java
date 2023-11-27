package com.siryus.swisscon.api.company.company;

import com.siryus.swisscon.api.auth.permission.Permission;
import com.siryus.swisscon.api.auth.permission.PermissionRepository;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.company.CompanyExceptions;
import com.siryus.swisscon.api.company.companylegaltype.CompanyLegalType;
import com.siryus.swisscon.api.company.companytrade.CompanyTrade;
import com.siryus.swisscon.api.company.companytrade.CompanyTradeRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleService;
import com.siryus.swisscon.api.company.numworkersofcompany.NumWorkersOfCompany;
import com.siryus.swisscon.api.file.file.File;
import com.siryus.swisscon.api.file.file.FileService;
import com.siryus.swisscon.api.general.favorite.FavoriteRepository;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompany;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompanyRepository;
import com.siryus.swisscon.api.util.security.SecurityHelper;
import com.siryus.swisscon.api.util.validator.DTOValidator;
import com.siryus.swisscon.api.util.validator.Reference;
import com.siryus.swisscon.soa.EventsEmitter;
import com.siryus.swisscon.soa.notification.contract.NotificationEvent;
import com.siryus.swisscon.soa.notification.contract.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("companyService")
@Validated
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final FavoriteRepository favoriteRepository;
    private final CompanyTradeRepository companyTradeRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final FileService fileService;
    private final PermissionRepository permissionRepository;
    private final CompanyUserRoleService companyUserRoleService;
    private final ProjectCompanyRepository projectCompanyRepository;
    private final SecurityHelper securityHelper;
    private final EventsEmitter eventsEmitter;

    @Autowired
    public CompanyService(
            FavoriteRepository favoriteRepository,
            CompanyTradeRepository companyTradeRepository,
            CompanyUserRoleRepository companyUserRoleRepository,
            FileService fileService,
            CompanyRepository companyRepository,
            PermissionRepository permissionRepository,
            CompanyUserRoleService companyUserRoleService,
            ProjectCompanyRepository projectCompanyRepository,
            SecurityHelper securityHelper,
            EventsEmitter eventsEmitter
    ) {
        this.favoriteRepository = favoriteRepository;
        this.companyTradeRepository = companyTradeRepository;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.fileService = fileService;
        this.companyRepository = companyRepository;
        this.permissionRepository = permissionRepository;
        this.companyUserRoleService = companyUserRoleService;
        this.projectCompanyRepository = projectCompanyRepository;
        this.securityHelper = securityHelper;
        this.eventsEmitter = eventsEmitter;
    }

    @Transactional
    void removeUserFromCompany(
            @Reference(ReferenceType.COMPANY) Integer companyId,
            @Reference(ReferenceType.USER) Integer userId
    ) {
        companyUserRoleService.removeUserFromCompanyTeam(companyId, userId);
    }

    @Transactional
    public Company updateProfile(Company company, CompanyProfileDTO companyProfileDTO) {

        // update picture and delete old one
        File previousPicture = company.getPicture();
        if (companyProfileDTO.getFileIdProfileImage() != null) {
            File newPicture = fileService.findById(companyProfileDTO.getFileIdProfileImage());
            if (fileService.isNewPicture(previousPicture, newPicture)) {
                newPicture.setReferenceType(ReferenceType.COMPANY.toString());
                newPicture.setReferenceId(company.getId());
                File updatedNewPicture = fileService.update(newPicture);
                company.setPicture(updatedNewPicture);
                Optional.ofNullable(previousPicture).ifPresent(p -> fileService.disable(previousPicture));
            }
        }
        else if(previousPicture != null) {
            fileService.disable(previousPicture);
            company.setPicture(null);
        }

        // set simple properties
        company.setName(companyProfileDTO.getName());
        company.setDirector(companyProfileDTO.getDirector());
        company.setDescription(companyProfileDTO.getDescription());

        // set company size
        NumWorkersOfCompany numWorkersOfCompany1 = new NumWorkersOfCompany();
        numWorkersOfCompany1.setId(companyProfileDTO.getNumberOfEmployees());
        company.setNumberOfEmployees(numWorkersOfCompany1);

        // set company legal type
        if (companyProfileDTO.getCompanyTypeId() != null) {
            CompanyLegalType companyLegalType = new CompanyLegalType();
            companyLegalType.setId(companyProfileDTO.getCompanyTypeId());
            company.setLegalType(companyLegalType);
        }

        List<CompanyTrade> companyTrades = companyTradeRepository.findCompanyTradesByCompany(company.getId());
        List<Integer> oldTradeIds = companyTrades.stream().map(ct -> ct.getTrade()).collect(Collectors.toList());
        List<Integer> newTradeIds = Arrays.asList(companyProfileDTO.getTradeIds());

        // delete trades which don't exist anymore
        List<CompanyTrade> deletedCompanyTrades = companyTrades
                .stream()
                .filter(ct -> !newTradeIds.contains(ct.getTrade()))
                .peek(ct -> companyTradeRepository.deleteById(ct.getId()))
                .collect(Collectors.toList());

        // add trades which are new
        List<CompanyTrade> addedCompanyTrades = newTradeIds
                .stream()
                .filter(tid -> !oldTradeIds.contains(tid))
                .map(tid -> {
                    return CompanyTrade.builder().company(company.getId()).trade(tid).build();
                })
                .map(companyTradeRepository::save)
                .collect(Collectors.toList());

        Company updatedCompany = companyRepository.save(company);

        // set the companyTrades cause it's a lazy loaded property
        companyTrades.removeAll(deletedCompanyTrades);
        companyTrades.addAll(addedCompanyTrades);
        updatedCompany.setCompanyTrades(companyTrades);

        eventsEmitter.emitCacheUpdate(ReferenceType.COMPANY, company.getId());

        return updatedCompany;
    }

    @Transactional
    public ContactDetailDTO updateContactDetails(
            @Reference(ReferenceType.COMPANY) Integer companyId,
            @Valid ContactDetailDTO companyDetails
    ) {
        Company company = getValidCompany(companyId);

        company.setContactData(companyDetails);

        companyRepository.save(company);

        eventsEmitter.emitCacheUpdate(ReferenceType.COMPANY, company.getId());

        return ContactDetailDTO.fromCompany(company);
    }

    ContactDetailDTO getContactDetails(@Reference(ReferenceType.COMPANY) Integer companyId) {
        return ContactDetailDTO.fromCompany(getValidCompany(companyId));
    }

    @Transactional
    public CompanyProfileDTO updateCompanyProfile(
            @Reference(ReferenceType.COMPANY) Integer companyId,
            @Valid CompanyProfileDTO profile
    ) {
        Company company = getValidCompany(companyId);

        Company updatedCompany = updateProfile(company, profile);

        eventsEmitter.emitNotification(NotificationEvent.fromCompany(NotificationType.COMPANY_PROFILE_UPDATED,
                companyId, securityHelper.currentUserId(), companyId, companyId));

        return CompanyProfileDTO.fromCompany(updatedCompany);
    }

    @Transactional
    public void removeCompany(
            @Reference(ReferenceType.COMPANY) Integer companyId
    ) {
        securityHelper.validateUserIsPartOfCompany(securityHelper.currentUserId(), companyId);

        List<CompanyUserRole> companyTeam = companyUserRoleRepository.findCompanyUsersRoleByCompany(companyId);

        // company cannot be deleted if it's part of a project
        List<ProjectCompany> projectCompanies = projectCompanyRepository.findActiveByCompany(companyId);
        if(!projectCompanies.isEmpty()) {
            List<Integer> projectIds = projectCompanies.stream().map(pc -> pc.getProject().getId()).collect(Collectors.toList());
            throw CompanyExceptions.companyWithProjectsCanNotBeRemoved(projectIds);
        }

        // remove all users from the company
        companyTeam.forEach(cur->companyUserRoleService
                .removeUserFromCompanyTeam(companyId, cur.getUser().getId()));

        companyRepository.disable(companyId);

        eventsEmitter.emitNotification(NotificationEvent.fromCompany(NotificationType.COMPANY_ARCHIVED,
                companyId, securityHelper.currentUserId(), companyId, companyId));

    }

    @Transactional
    public void delete(Company resource) {
        this.delete(resource.getId());
    }

    @Transactional()
    public void delete(Integer id) {
        companyRepository.deleteById(id);
        favoriteRepository.removeForAllUsers(id, ReferenceType.COMPANY.toString());
    }

    public List<CompanyDirectoryDTO> directory() {
        List<Company> activeCompanies = findAllActive();

        List<Integer> companyIds = activeCompanies
                .stream()
                .map(Company::getId)
                .collect(Collectors.toList());

        List<CompanyUserRole> companyOwners = companyUserRoleService.getCompanyOwners(companyIds);

        return companyOwners
                .stream()
                .map(CompanyDirectoryDTO::from)
                .collect(Collectors.toList());
    }

    public List<CompanyDirectoryDTO> contractors() {
        return directory()
                .stream()
                .filter( c -> c.getOwner().getRoleIds().contains(securityHelper.roleId(RoleName.COMPANY_OWNER)))
                .collect(Collectors.toList());
    }

    public List<CompanyDirectoryDTO> customers() {
        return directory()
                .stream()
                .filter( c -> c.getOwner().getRoleIds().contains(securityHelper.roleId(RoleName.CUSTOMER)))
                .collect(Collectors.toList());
    }

    public List<Company> findAllActive() {
        return companyRepository.findAllActive();
    }

    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    public RegistrationDetailsDTO saveRegistrationDetails(Integer companyId, RegistrationDetailsDTO registrationDetailsDTO) {
        DTOValidator.validateAndThrow(registrationDetailsDTO);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> CompanyExceptions.companyCanNotBeFound(companyId));

        company.setVatNumber(registrationDetailsDTO.getVatNumber());
        company.setTaxNumber(registrationDetailsDTO.getTaxNumber());
        company.setRegisterEntryNumber(registrationDetailsDTO.getRegisterEntryNumber());

        return RegistrationDetailsDTO.from(companyRepository.save(company));
    }

    public List<Integer> getUserCompanyPermissions(Integer userId, Integer companyId) {
        return this.permissionRepository.findByUserAndCompany(userId, companyId).stream()
                .map(Permission::getId).collect(Collectors.toList());
    }

    public Company getValidCompany(Integer companyId ) {
        return getValidCompany(companyId, false);
    }

    public Company getValidCompany(Integer companyId, boolean canBeDisabled ) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> CompanyExceptions.companyCanNotBeFound(companyId));

        if (canBeDisabled || company.getDisabled() == null) {
            return company;
        }

        throw CompanyExceptions.companyCanNotBeFound(companyId);
    }
}
