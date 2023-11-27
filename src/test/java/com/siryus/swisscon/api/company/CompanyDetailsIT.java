package com.siryus.swisscon.api.company;

import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.user.TeamUserAddDTO;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyDetailsDTO;
import com.siryus.swisscon.api.company.company.CompanyRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.util.EmailPhoneUtils;
import io.restassured.specification.RequestSpecification;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompanyDetailsIT extends AbstractMvcTestBase {

    private static final String PATH = BASE_PATH + "/companies/";

    private final CompanyRepository companyRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public CompanyDetailsIT(CompanyRepository companyRepository, CompanyUserRoleRepository companyUserRoleRepository, RoleRepository roleRepository) {
        this.companyRepository = companyRepository;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.roleRepository = roleRepository;
    }

    @Test
    public void testModifyTeam() {
        final Integer companyId = 1;
        final RequestSpecification loginSpec = loginSpec();
        CompanyDetailsDTO companyDetailsDTO = getResponse(loginSpec, PATH + companyId.toString() + "/details-team")
                .as(CompanyDetailsDTO.class);
        final int originalSize = companyDetailsDTO.getTeam().size();

        TeamUserAddDTO teamUserAddDTO = TestBuilder.testTeamUserAddDTO("Invitee Ltd", "Invitee", "Royal");

        TeamUserDTO teamUserDTO = getResponse(loginSpec, BASE_PATH + "/auth/invite", teamUserAddDTO).as(TeamUserDTO.class);

        companyDetailsDTO = getResponse(loginSpec, PATH + companyId.toString() + "/details-team")
                .as(CompanyDetailsDTO.class);

        assertNotEquals(originalSize, companyDetailsDTO.getTeam().size());
        assertTrue(companyDetailsDTO.getTeam().stream().anyMatch(u -> u.getId() == teamUserDTO.getId()));

        final Map<String, String> params = Maps.newHashMap("user", teamUserDTO.getId().toString());
        final String removeUserPath = PATH + companyId.toString() + "/remove-user";
        // unhappy path: not logged in
        getResponse(defaultSpec(), removeUserPath, params, HttpStatus.UNAUTHORIZED);
        // happy path
        getResponse(loginSpec, removeUserPath, params);

        companyDetailsDTO = getResponse(loginSpec, PATH + companyId.toString() + "/details-team")
                .as(CompanyDetailsDTO.class);

        assertEquals(originalSize, companyDetailsDTO.getTeam().size());
        assertFalse(companyDetailsDTO.getTeam().stream().anyMatch(u -> u.getId() == teamUserDTO.getId()));
    }

    @Test
    public void testGetCompanyDetailsTeam() {
        // execute
        // unhappy path: not logged in
        getResponse(defaultSpec(), PATH + "1/details-team", HttpStatus.UNAUTHORIZED);
        CompanyDetailsDTO companyDetailsDTO = getResponse(loginSpec(), PATH + "1/details-team")
                .as(CompanyDetailsDTO.class);

        Optional<User> user1Opt = userRepository.findById(1);
        Optional<User> user2Opt = userRepository.findById(1);
        Optional<CompanyUserRole> companyUserRole1Opt = companyUserRoleRepository.findById(1);
        Optional<CompanyUserRole> companyUserRole2Opt = companyUserRoleRepository.findById(1);
        Optional<Company> companyOpt = companyRepository.findById(1);

        assert user1Opt.isPresent();
        assert user2Opt.isPresent();
        assert companyUserRole1Opt.isPresent();
        assert companyUserRole2Opt.isPresent();
        assert companyOpt.isPresent();

        // assert team user 1 and user 2
        assertTrue(companyDetailsDTO.getTeam().size() > 1, "There have to be users in the response");
        Optional<TeamUserDTO> user1DTOOpt = companyDetailsDTO.getTeam().stream().filter(p -> p.getId().equals(companyUserRole1Opt.get().getUser().getId())).findFirst();
        Optional<TeamUserDTO> user2DTOOpt = companyDetailsDTO.getTeam().stream().filter(p -> p.getId().equals(companyUserRole2Opt.get().getUser().getId())).findFirst();
        assertTrue(user1DTOOpt.isPresent());
        assertTrue(user2DTOOpt.isPresent());
        assertTeamMember(companyUserRole1Opt.get(), user1DTOOpt.get());
        assertTeamMember(companyUserRole2Opt.get(), user2DTOOpt.get());

        // assert company details
        assertEquals(companyOpt.get().getId(), companyDetailsDTO.getId());
        assertEquals(companyOpt.get().getName(), companyDetailsDTO.getName());
        assertEquals(companyOpt.get().getCompanyEmail(), companyDetailsDTO.getCompanyEmail());
        assertEquals(companyOpt.get().getAddress1(), companyDetailsDTO.getAddress1());
        assertEquals(companyOpt.get().getAddress2(), companyDetailsDTO.getAddress2());
        assertEquals(companyOpt.get().getPhone(), companyDetailsDTO.getPhone());
        assertEquals(companyOpt.get().getPlz(), companyDetailsDTO.getPlz());
        assertEquals(companyOpt.get().getCity(), companyDetailsDTO.getCity());
        assertEquals(companyOpt.get().getCountry().getId(), companyDetailsDTO.getCountryId());
        assertEquals(companyOpt.get().getPicture().getId(), companyDetailsDTO.getPicture().getId());
        assertEquals(companyOpt.get().getPicture().getUrl(), companyDetailsDTO.getPicture().getUrl());
        assertEquals(companyOpt.get().getPicture().getUrlSmall(), companyDetailsDTO.getPicture().getUrlSmall());
        assertEquals(companyOpt.get().getPicture().getUrlMedium(), companyDetailsDTO.getPicture().getUrlMedium());
        assertEquals(companyOpt.get().getDescription(), companyDetailsDTO.getDescription());
        assertTrue(companyDetailsDTO.getTradeIds().length > 0);
    }

    @Test
    public void testGetCompanyDetailsTeamOfAnotherCompany() {
        CompanyDetailsDTO companyDetailsDTO = getResponse(loginSpec("test3@siryus.com", "test 3"), PATH + "1/details-team")
                .as(CompanyDetailsDTO.class);

        // check that we see only the owner of the company, because we logged is as another company
        assertEquals(1,companyDetailsDTO.getTeam().size());
        Role ownerRole = roleRepository.getRoleByName(RoleName.COMPANY_OWNER.toString());
        assertEquals(ownerRole.getId(), companyDetailsDTO.getTeam().get(0).getRoleId());
    }

    private void assertTeamMember(CompanyUserRole expectedRole, TeamUserDTO userDTO) {
        String expectedMobile = EmailPhoneUtils
                .toFullPhoneNumber(expectedRole.getUser().getMobileCountryCode(),
                expectedRole.getUser().getMobile());

        assertEquals(expectedRole.getUser().getId(), userDTO.getId());
        assertEquals(expectedRole.getUser().getGivenName(), userDTO.getFirstName());
        assertEquals(expectedRole.getUser().getSurName(), userDTO.getLastName());
        assertEquals(expectedRole.getRole().getId(), userDTO.getRoleId());
        assertEquals(expectedMobile, userDTO.getMobile());
        assertEquals(expectedRole.getUser().getEmail(), userDTO.getEmail());
        boolean expectedIsAdmin = !expectedRole.getRole().getName().equals(RoleName.COMPANY_WORKER.toString());
        assertEquals(expectedIsAdmin, userDTO.getIsAdmin());
    }

}
