package com.siryus.swisscon.api.company;

import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyDirectoryDTO;
import com.siryus.swisscon.api.company.company.CompanyProfileDTO;
import com.siryus.swisscon.api.company.company.CompanyService;
import com.siryus.swisscon.api.company.company.ContactDetailDTO;
import com.siryus.swisscon.api.company.company.RegistrationDetailsDTO;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleService;
import com.siryus.swisscon.api.project.project.Project;
import com.siryus.swisscon.api.project.project.ProjectRepository;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompany;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompanyRepository;
import com.siryus.swisscon.api.project.projectcompany.ProjectCompanyService;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRole;
import com.siryus.swisscon.api.project.projectuserrole.ProjectUserRoleRepository;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompanyIT extends AbstractMvcTestBase {

    private static final String PATH = BASE_PATH + "/companies/";

    private static List<CompanyUserRole> testTeam;

    private final CompanyService companyService;
    private final RoleRepository roleRepository;
    private final CompanyUserRoleService companyUserRoleService;
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final ProjectRepository projectRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final ProjectCompanyService projectCompanyService;
    private final ProjectCompanyRepository projectCompanyRepository;

    private static User testUserActive, testUserDisabled, testUserAdd;

    @Autowired
    public CompanyIT(CompanyService companyService, RoleRepository roleRepository,
                     CompanyUserRoleService companyUserRoleService, CompanyUserRoleRepository companyUserRoleRepository, ProjectRepository projectRepository,
                     ProjectCompanyService projectCompanyService,
                     ProjectUserRoleRepository projectUserRoleRepository, ProjectCompanyRepository projectCompanyRepository) {
        this.companyService = companyService;
        this.roleRepository = roleRepository;
        this.companyUserRoleService = companyUserRoleService;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.projectRepository = projectRepository;
        this.projectCompanyService = projectCompanyService;
        this.projectUserRoleRepository = projectUserRoleRepository;
        this.projectCompanyRepository = projectCompanyRepository;
    }

    @BeforeEach
    public void initTestData() {
        testTeam = createCompanyTeam();
    }

    @AfterEach
    public void clearTestData() {
        deleteCompanyTeam();
    }

    @Test
    public void testGetCompanyDetails() {
        // prepare
        RequestSpecification specification = loginSpec();
        Company expectedCompany = testTeam.get(0).getCompany();

        // execute
        ContactDetailDTO contactDetailDTO = getResponse(specification, PATH + "1/contact-details")
                .as(ContactDetailDTO.class);

        // assert
        assertEquals(expectedCompany.getAddress1(), contactDetailDTO.getAddress1());
        assertEquals(expectedCompany.getAddress2(), contactDetailDTO.getAddress2());
        assertEquals(expectedCompany.getPhone(), contactDetailDTO.getPhone());
        assertEquals(expectedCompany.getPlz(), contactDetailDTO.getPlz());
        assertEquals(expectedCompany.getCity(), contactDetailDTO.getCity());
        assertEquals(expectedCompany.getWebUrl(), contactDetailDTO.getWebUrl());
        assertEquals(expectedCompany.getCompanyEmail(), contactDetailDTO.getCompanyEmail());
        assertEquals(expectedCompany.getFax(), contactDetailDTO.getFax());
        assertEquals(expectedCompany.getCountry().getId(), contactDetailDTO.getCountryId());
    }

    @Test
    public void testPutContactDetails() {
        // prepare
        RequestSpecification specification = loginSpec();
        ContactDetailDTO contactDetailDTO = new ContactDetailDTO();

        contactDetailDTO.setAddress1("updated street");
        contactDetailDTO.setAddress2("updated house");
        contactDetailDTO.setPhone("+41 79 854 45 22");
        contactDetailDTO.setPlz("2000");
        contactDetailDTO.setCity("Genf");
        contactDetailDTO.setWebUrl("www.updated.com");
        contactDetailDTO.setCompanyEmail("updated@test.com");
        contactDetailDTO.setFax("+41 451 21 12");
        contactDetailDTO.setCountryId(1);

        // Check that company does not have the data yet
        final Integer companyId = 1;
        Company company = companyService.getValidCompany(companyId);
        assertNotEquals(company.getAddress1(), contactDetailDTO.getAddress1());
        assertNotEquals(company.getAddress2(), contactDetailDTO.getAddress2());
        assertNotEquals(company.getPhone(), contactDetailDTO.getPhone());
        assertNotEquals(company.getPlz(), contactDetailDTO.getPlz());
        assertNotEquals(company.getCity(), contactDetailDTO.getCity());
        assertNotEquals(company.getWebUrl(), contactDetailDTO.getWebUrl());
        assertNotEquals(company.getCompanyEmail(), contactDetailDTO.getCompanyEmail());
        assertNotEquals(company.getFax(), contactDetailDTO.getFax());
        assertNotEquals(company.getCountry().getId(), contactDetailDTO.getCountryId());

        // execute
        ContactDetailDTO updatedContactDetailDTO = getResponse(specification, PATH + companyId.toString() + "/contact-details", contactDetailDTO)
                .as(ContactDetailDTO.class);

        // assert saved company
        company = companyService.getValidCompany(companyId);
        assertEquals(company.getAddress1(), contactDetailDTO.getAddress1());
        assertEquals(company.getAddress2(), contactDetailDTO.getAddress2());
        assertEquals(company.getPhone(), contactDetailDTO.getPhone());
        assertEquals(company.getPlz(), contactDetailDTO.getPlz());
        assertEquals(company.getCity(), contactDetailDTO.getCity());
        assertEquals(company.getWebUrl(), contactDetailDTO.getWebUrl());
        assertEquals(company.getCompanyEmail(), contactDetailDTO.getCompanyEmail());
        assertEquals(company.getFax(), contactDetailDTO.getFax());
        assertEquals(company.getCountry().getId(), contactDetailDTO.getCountryId());

        // assert response
        assertEquals(updatedContactDetailDTO.getAddress1(), contactDetailDTO.getAddress1());
        assertEquals(updatedContactDetailDTO.getAddress2(), contactDetailDTO.getAddress2());
        assertEquals(updatedContactDetailDTO.getPhone(), contactDetailDTO.getPhone());
        assertEquals(updatedContactDetailDTO.getPlz(), contactDetailDTO.getPlz());
        assertEquals(updatedContactDetailDTO.getCity(), contactDetailDTO.getCity());
        assertEquals(updatedContactDetailDTO.getWebUrl(), contactDetailDTO.getWebUrl());
        assertEquals(updatedContactDetailDTO.getCompanyEmail(), contactDetailDTO.getCompanyEmail());
        assertEquals(updatedContactDetailDTO.getFax(), contactDetailDTO.getFax());
        assertEquals(updatedContactDetailDTO.getCountryId(), contactDetailDTO.getCountryId());
    }

    @Test
    public void testPutProfile() {
        // For unhappy path: not logged in
        RequestSpecification unauthorizedUser = defaultSpec();

        RequestSpecification specification = loginSpec();

        final String newName = "new name";
        assertNull(testHelper.directoryLookup(newName, specification));

        CompanyProfileDTO companyProfileDTO = CompanyProfileDTO.builder()
                .name(newName)
                .numberOfEmployees(2)
                .tradeIds(Arrays.asList(1,2,3,4).toArray(new Integer[0]))
                .companyTypeId(2)
                .fileIdProfileImage(4)
                .build();

        // execute
        final String path = PATH + "1/profile";
        // unhappy path: not logged in
        getResponse(unauthorizedUser, path, companyProfileDTO, HttpStatus.UNAUTHORIZED);
        // happy path: expect a result
        CompanyProfileDTO updatedCompanyProfileDTO = getResponse(specification, path, companyProfileDTO)
                .as(CompanyProfileDTO.class);

        assertEquals(companyProfileDTO.getName(), updatedCompanyProfileDTO.getName());
        assertEquals(companyProfileDTO.getNumberOfEmployees(), updatedCompanyProfileDTO.getNumberOfEmployees());
        assertArrayEquals(companyProfileDTO.getTradeIds(), updatedCompanyProfileDTO.getTradeIds());
        assertEquals(companyProfileDTO.getCompanyTypeId(), updatedCompanyProfileDTO.getCompanyTypeId());
        assertEquals(companyProfileDTO.getFileIdProfileImage(), updatedCompanyProfileDTO.getFileIdProfileImage());
        assertNotNull(updatedCompanyProfileDTO.getLastModified());
        final CompanyDirectoryDTO directoryDTO = testHelper.directoryLookup(newName, specification);
        assertNotNull(directoryDTO);
        assertEquals(newName, directoryDTO.getName());
    }

    @Test
    public void testPutProfileWithEmptyTradesAndProfileImage() {
        RequestSpecification specification = loginSpec();

        CompanyProfileDTO companyProfileDTO = CompanyProfileDTO.builder()
                .name("new name")
                .description("new description")
                .fileIdProfileImage(4)
                .numberOfEmployees(2)
                .tradeIds(new Integer[0])
                .companyTypeId(2)
                .build();

        // execute
        CompanyProfileDTO updatedCompanyProfileDTO = getResponse(specification, PATH + "1/profile", companyProfileDTO)
                .as(CompanyProfileDTO.class);

        assertEquals(companyProfileDTO.getName(), updatedCompanyProfileDTO.getName());
        assertEquals(companyProfileDTO.getDescription(), updatedCompanyProfileDTO.getDescription());
        assertEquals(companyProfileDTO.getNumberOfEmployees(), updatedCompanyProfileDTO.getNumberOfEmployees());
        assertArrayEquals(companyProfileDTO.getTradeIds(), updatedCompanyProfileDTO.getTradeIds());
        assertEquals(companyProfileDTO.getCompanyTypeId(), updatedCompanyProfileDTO.getCompanyTypeId());
        assertEquals(Integer.valueOf(4), updatedCompanyProfileDTO.getFileIdProfileImage());
        assertNotNull(updatedCompanyProfileDTO.getLastModified());
    }

    @Test
    public void testUpdateRegistrationDetails() {
        // prepare
        RequestSpecification specification = loginSpec();
        RegistrationDetailsDTO registrationDetailsDTO = RegistrationDetailsDTO.builder()
                .vatNumber("VAT NUMBER 123")
                .taxNumber("TAX NUMBER 123")
                .registerEntryNumber("REGISTRY NUMBER 123")
                .build();

        // execute
        getResponse(specification, PATH + "1/registration-details", registrationDetailsDTO)
                .as(RegistrationDetailsDTO.class);

        // assert
        Company company = companyService.getValidCompany(1);
        assertEquals(registrationDetailsDTO.getVatNumber(), company.getVatNumber());
        assertEquals(registrationDetailsDTO.getTaxNumber(), company.getTaxNumber());
        assertEquals(registrationDetailsDTO.getRegisterEntryNumber(), company.getRegisterEntryNumber());
    }

    private List<CompanyUserRole> createCompanyTeam() {
        User user1 = User.builder().username("companytest1@test.com").build();
        user1.setEmail("companytest1@mail.com");
        user1.setPassword("{noop}test");
        User savedUser1 = userRepository.save(user1);

        User user2 = User.builder().username("companytest2@test.com").build();
        user2.setEmail("companytest2@mail.com");
        user2.setPassword("{noop}test");
        User savedUser2 = userRepository.save(user2);
        
        User user3 = User.builder().username("companytest1@test.com").build();
        user3.setEmail("companytest" + RandomStringUtils.randomAlphabetic(5) + "@mail.com");
        user3.setPassword("{noop}test");
        User savedUser3 = userRepository.save(user3);        

        Company company = companyService.getValidCompany(1);
        Optional<Role> role2Opt = roleRepository.findById(102);
        Optional<Role> role3Opt = roleRepository.findById(103);
        assertTrue(role2Opt.isPresent());
        assertTrue(role3Opt.isPresent());

        CompanyUserRole companyUserRole1 = CompanyUserRole.builder().company(company).user(savedUser1).role(role2Opt.get()).build();
        CompanyUserRole companyUserRole2 = CompanyUserRole.builder().company(company).user(savedUser2).role(role3Opt.get()).build();
        CompanyUserRole companyUserRole3 = CompanyUserRole.builder().company(company).user(savedUser3).role(role2Opt.get()).build();

        LinkedList<CompanyUserRole> companyUserRoles = new LinkedList<>();

        companyUserRoles.add(companyUserRoleService.create(companyUserRole1));
        companyUserRoles.add(companyUserRoleService.create(companyUserRole2));
        companyUserRoles.add(companyUserRoleService.create(companyUserRole3));

        Project p = projectRepository.findAll().get(0);

        ProjectCompany pc = projectCompanyService.findByProjectAndCompany(p, company);
        if (null == pc) {
            pc = ProjectCompany.builder().company(company).project(p).build();
            projectCompanyRepository.save(pc);
        }
        
        ProjectUserRole projectUserRole = ProjectUserRole.builder().project(p).user(user1).projectCompany(pc).role(role2Opt.get()).build();
        projectUserRoleRepository.save(projectUserRole);
        
        // For testing disabled behaviour
        ProjectUserRole result = projectUserRoleRepository.save(ProjectUserRole.builder().project(p).user(user2).projectCompany(pc).role(role2Opt.get()).build());
        projectUserRoleRepository.delete(result);
        
        testUserActive = user1;
        testUserDisabled = user2;
        testUserAdd = user3;
        
        return companyUserRoles;
    }

    private void deleteCompanyTeam() {
        projectUserRoleRepository.findAllIncludeDisabled().forEach(pur -> {
            if (pur.getUser().getId().equals(testUserActive.getId()) || 
                pur.getUser().getId().equals(testUserDisabled.getId()) || 
                pur.getUser().getId().equals(testUserAdd.getId())) {
                projectUserRoleRepository.deleteWithId(pur.getId());
            }
        });
        
        testTeam.forEach(role -> {
            companyUserRoleRepository.deleteById(role.getId());
            userRepository.deleteById(Objects.requireNonNull(role.getUser().getId()));
        });
    }

}
