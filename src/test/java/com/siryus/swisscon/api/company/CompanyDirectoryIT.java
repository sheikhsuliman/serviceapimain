package com.siryus.swisscon.api.company;

import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyDirectoryDTO;
import com.siryus.swisscon.api.company.company.CompanyRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CompanyDirectoryIT extends AbstractMvcTestBase {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;

    private static Company testCompany;
    private static User testUser;
    private static CompanyUserRole testCompanyUserRole;

    private static final String PATH = BASE_PATH + "/companies/";

    @Autowired
    public CompanyDirectoryIT(CompanyRepository companyRepository, UserRepository userRepository, RoleRepository roleRepository, CompanyUserRoleRepository companyUserRoleRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.companyUserRoleRepository = companyUserRoleRepository;
    }

    @Test
    public void testGetCompanyDirectory() {
        // execute
        CompanyDirectoryDTO[] directoryDTOsArr = getResponse(loginSpec(), PATH + "directory")
                .as(CompanyDirectoryDTO[].class);

        List<CompanyDirectoryDTO> directoryDTOs = Arrays.asList(directoryDTOsArr);
        assertFalse(directoryDTOs.isEmpty());

        // check that all active companies are in the DTO list
        List<Company> allActive = companyRepository.findAllActive();
        assertEquals(allActive.size(), directoryDTOs.size());
        for(Company company : allActive) {
            Optional<CompanyDirectoryDTO> dtoOpt = directoryDTOs.stream().filter(dto -> company.getId().equals(dto.getId())).findFirst();
            assertTrue(dtoOpt.isPresent());
            assertCompanyDirectoryDTO(company, dtoOpt.get());
        }

        // check that the test company is in the list
        Optional<CompanyDirectoryDTO> testCompanyDTO = directoryDTOs.stream().filter(dto -> testCompany.getId().equals(dto.getId())).findFirst();
        assertTrue(testCompanyDTO.isPresent());

        // assert Owner of Test Company in DTO
        assertNotNull(testCompanyDTO.get().getOwner());
        TestAssert.assertTeamUserDTOequals(testUser, testCompanyUserRole.getRole(), testCompanyDTO.get().getOwner());
    }

    public static void assertCompanyDirectoryDTO(Company company, CompanyDirectoryDTO dto) {
        assertEquals(company.getId(), dto.getId());
        assertEquals(company.getName(), dto.getName());
        if(company.getPicture() != null) {
            assertNotNull(dto.getPicture());
            assertEquals(company.getPicture().getId(), dto.getPicture().getId());
            assertEquals(company.getPicture().getUrl(), dto.getPicture().getUrl());
            assertEquals(company.getPicture().getUrlMedium(), dto.getPicture().getUrlMedium());
            assertEquals(company.getPicture().getUrlSmall(), dto.getPicture().getUrlSmall());
        }
    }

    @BeforeEach
    public void initTestData() {
        Company tempCompany = Company.builder().name("directoryTest").build();
        testCompany = companyRepository.save(tempCompany);

        String email = RandomStringUtils.randomAlphabetic(10) + "@test.com";
        User tempUser = User.builder().username(email).build();
        tempUser.setEmail(email);
        tempUser.setPassword("{noop}987LLAXAbx");
        testUser = userRepository.save(tempUser);

        Role ownerRole = roleRepository.getRoleByName(RoleName.COMPANY_OWNER.toString());

        CompanyUserRole tempUserRole = CompanyUserRole.builder()
                .role(ownerRole)
                .company(testCompany)
                .user(testUser)
                .build();

        testCompanyUserRole = companyUserRoleRepository.save(tempUserRole);
    }

    @AfterEach
    public void deleteTestData() {
        Optional.ofNullable(testCompanyUserRole).ifPresent(cur -> deleteIfExists(companyUserRoleRepository, cur.getId()));
        Optional.ofNullable(testUser).ifPresent(c -> deleteIfExists(userRepository, c.getId()));
        Optional.ofNullable(testCompany).ifPresent(c -> deleteIfExists(companyRepository, c.getId()));
        testCompanyUserRole = null;
        testUser = null;
        testCompany = null;
    }
}
