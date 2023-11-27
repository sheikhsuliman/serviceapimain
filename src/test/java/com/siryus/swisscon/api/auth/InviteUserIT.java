package com.siryus.swisscon.api.auth;

import com.siryus.swisscon.api.auth.role.Role;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.user.TeamUserAddDTO;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.auth.usertoken.UserTokenRepository;
import com.siryus.swisscon.api.auth.usertoken.UserTokenType;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.FailFastExtension;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.util.error.TestErrorResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;

import static com.siryus.swisscon.api.base.TestHelper.COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_LAST_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(FailFastExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InviteUserIT extends AbstractMvcTestBase {

    private final ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

    private static Role companyWorkerRole;
    private static Role companyAdminRole;
    private static Role projectAdminRole;
    private static Role companyOwnerRole;

    private static final String EMAIL = "test@test.com";
    private static final Integer COUNTRY_CODE = 41;
    private static final String MOBILE = "7964654520";
    private static final String FULL_MOBILE = COUNTRY_CODE + MOBILE;
    private static final String NEW_PASSWORD = "Siryus2000";

    private final UserTokenRepository userTokenRepository;
    private final RoleRepository roleRepository;

    private RequestSpecification asCompanyOwner;
    private Integer companyId;
    private Integer userIdEmail;
    private Integer userIdMobile;
    private String validEmailToken;
    private String validMobileToken;

    @Autowired
    public InviteUserIT(UserTokenRepository userTokenRepository, RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
        this.userTokenRepository = userTokenRepository;
    }

    @BeforeAll()
    public void init() {
        companyId = testHelper
                .signUp(TestBuilder.testSignupDTO(COMPANY_NAME,
                        PROJECT_OWNER_FIRST_NAME,
                        PROJECT_OWNER_LAST_NAME)).getCompanyId();
        asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);
        companyWorkerRole = roleRepository.getRoleByName(RoleName.COMPANY_WORKER.toString());
        companyAdminRole = roleRepository.getRoleByName(RoleName.COMPANY_ADMIN.toString());
        projectAdminRole = roleRepository.getRoleByName(RoleName.PROJECT_ADMIN.toString());
        companyOwnerRole = roleRepository.getRoleByName(RoleName.COMPANY_OWNER.toString());
    }

    @Test
    public void Given_invalidEmail_When_inviteUser_Then_throw() {
        testHelper.inviteUser(asCompanyOwner, TestBuilder.testTeamUserAddDTO("this_is_not_a_mail"), r -> {
            TestErrorResponse error = r.assertThat().statusCode(HttpStatus.BAD_REQUEST.value())
                    .extract().as(TestErrorResponse.class);
            assertThat(error.getReason(), containsString("Email or Phone not correctly formatted"));
            return null;
        });
    }

    @Test
    public void Given_invalidPhone_When_inviteUser_Then_throw() {
        testHelper.inviteUser(asCompanyOwner, TestBuilder.testTeamUserAddDTO(41, "xab789655,%"), r -> {
            TestErrorResponse error = r.assertThat().statusCode(HttpStatus.BAD_REQUEST.value())
                    .extract().as(TestErrorResponse.class);
            assertThat(error.getReason(), containsString("Email or Phone not correctly formatted"));
            return null;
        });
    }

    @Test
    public void Given_invalidRoleId_When_inviteUser_Then_throw() {
        TeamUserAddDTO teamUserAddDTO = TestBuilder.testTeamUserAddDTO(EMAIL + "2");
        teamUserAddDTO.setRoleId(-15);

        testHelper.inviteUser(asCompanyOwner, teamUserAddDTO, r -> {
            r.statusCode(HttpStatus.BAD_REQUEST.value());
            return null;
        });
    }

    @Test
    public void Given_projectRoleId_When_inviteUser_Then_throw() {
        TeamUserAddDTO teamUserAddDTO = TestBuilder.testTeamUserAddDTO(EMAIL + "m");
        teamUserAddDTO.setRoleId(projectAdminRole.getId());

        testHelper.inviteUser(asCompanyOwner, teamUserAddDTO, r -> {
            TestAssert.assertError(HttpStatus.BAD_REQUEST, AuthException.ROLE_FOR_COMPANY_USER_CANNOT_BE_PROJECT_ROLE.getErrorCode(), r);
            return null;
        });
    }

    @Test
    public void Given_uniqueRoleId_When_inviteUser_Then_throw() {
        TeamUserAddDTO teamUserAddDTO = TestBuilder.testTeamUserAddDTO(EMAIL + "m");
        teamUserAddDTO.setRoleId(companyOwnerRole.getId());

        testHelper.inviteUser(asCompanyOwner, teamUserAddDTO, r -> {
            TestAssert.assertError(HttpStatus.BAD_REQUEST, AuthException.ROLE_FOR_COMPANY_CANNOT_BE_UNIQUE_AND_MANDATORY.getErrorCode(), r);
            return null;
        });
    }

    @Test
    @Order(0)
    public void Given_correctEmail_When_inviteUser_Then_success() {
        captureToken();
        TeamUserAddDTO teamUserAddDTO = TestBuilder.testTeamUserAddDTO(EMAIL);
        teamUserAddDTO.setRoleId(companyAdminRole.getId());
        TeamUserDTO invitedUser = testHelper.inviteUser(asCompanyOwner, teamUserAddDTO);
        userIdEmail = invitedUser.getId();
        validEmailToken = tokenCaptor.getValue();
        assertNotNull(validEmailToken);
        TestAssert.assertTeamUserDTOequals(teamUserAddDTO, companyAdminRole, invitedUser);

        TeamUserDTO teamUserDTO = getCompanyTeam(invitedUser.getId());
        TestAssert.assertTeamUserDTOequals(invitedUser, teamUserDTO);
    }

    @Test
    @Order(1)
    public void Given_correctPhone_When_inviteUser_Then_success() {
        captureToken();
        TeamUserAddDTO teamUserAddDTO = TestBuilder.testTeamUserAddDTO(COUNTRY_CODE, MOBILE);
        TeamUserDTO invitedUser = testHelper.inviteUser(asCompanyOwner, teamUserAddDTO);
        userIdMobile = invitedUser.getId();
        validMobileToken = tokenCaptor.getValue();
        assertNotNull(validMobileToken);
        TestAssert.assertTeamUserDTOequals(teamUserAddDTO, companyWorkerRole, invitedUser);

        TeamUserDTO teamUserDTO = getCompanyTeam(invitedUser.getId());
        TestAssert.assertTeamUserDTOequals(invitedUser, teamUserDTO);
        assertOneTokenStoredByUser(userIdMobile);
    }

    @Test
    @Order(2)
    public void Given_duplicateEmail_When_inviteUser_Then_throw() {
        testHelper.inviteUser(asCompanyOwner, TestBuilder.testTeamUserAddDTO(EMAIL), r -> {
            TestErrorResponse error = r.assertThat().statusCode(HttpStatus.CONFLICT.value())
                    .extract().as(TestErrorResponse.class);
            assertThat(error.getReason(), containsString("Email or Phone already exist"));
            assertThat(error.getReason(), containsString(EMAIL));
            return null;
        });
    }

    @Test
    @Order(3)
    public void Given_duplicatePhone_When_inviteUser_Then_throw() {
        testHelper.inviteUser(asCompanyOwner, TestBuilder.testTeamUserAddDTO(COUNTRY_CODE, MOBILE), r -> {
            TestErrorResponse error = r.assertThat().statusCode(HttpStatus.CONFLICT.value())
                    .extract().as(TestErrorResponse.class);
            assertThat(error.getReason(), containsString("Email or Phone already exist"));
            assertThat(error.getReason(), containsString(FULL_MOBILE));
            return null;
        });
    }

    @Test
    @Order(4)
    public void Given_nonExistingUserId_When_resendInvitation_Then_throw() {
        testHelper.resendInvitation(asCompanyOwner, -15,
                r -> r.assertThat().statusCode(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @Order(5)
    public void Given_existingUserWithEmail_When_resendInvitation_Then_success() {
        captureToken();
        testHelper.resendInvitation(asCompanyOwner, userIdEmail);
        validEmailToken = tokenCaptor.getValue();
        assertNotNull(validEmailToken);
    }

    @Test
    @Order(6)
    public void Given_existingUserWithPhone_When_resendInvitation_Then_success() {
        captureToken();
        testHelper.resendInvitation(asCompanyOwner, userIdMobile);
        validMobileToken = tokenCaptor.getValue();
        assertNotNull(validMobileToken);
        assertOneTokenStoredByUser(userIdMobile);
    }

    @Test
    @Order(7)
    public void Given_wrongToken_When_ResetPassword_Then_throw() {
        testHelper.resetPassword(asCompanyOwner,
                TestBuilder.testResetPasswordForm("wrong", NEW_PASSWORD),
                r -> r.assertThat().statusCode(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @Order(8)
    public void Given_correctEmailToken_When_ResetPasswordAndLogin_Then_Success() {
        testHelper.resetPassword(asCompanyOwner,
                TestBuilder.testResetPasswordForm(validEmailToken, NEW_PASSWORD));
        testHelper.login(EMAIL, NEW_PASSWORD);
        assertFalse(getCompanyTeam(userIdEmail).getIsUnverified());
    }

    @Test
    @Order(9)
    public void Given_correctMobileToken_When_ResetPasswordAndLogin_Then_Success() {
        testHelper.resetPassword(asCompanyOwner,
                TestBuilder.testResetPasswordForm(validMobileToken, NEW_PASSWORD));
        testHelper.login(FULL_MOBILE, NEW_PASSWORD);
        assertNoTokenStoredByUser(userIdMobile);
        assertFalse(getCompanyTeam(userIdMobile).getIsUnverified());
    }

    @Test
    @Order(10)
    public void Given_alreadyVerifiedEmailUser_When_ResendInvitation_Then_throw() {
        testHelper.resendInvitation(asCompanyOwner, userIdEmail, r-> {
            TestAssert.assertError(HttpStatus.BAD_REQUEST, AuthException.USER_IS_ALREADY_VERIFIED.getErrorCode(), r);
        });
    }

    @Test
    @Order(12)
    public void Given_alreadyVerifiedPhoneUser_When_ResendInvitation_Then_throw() {
        testHelper.resendInvitation(asCompanyOwner, userIdMobile, r-> {
            TestAssert.assertError(HttpStatus.BAD_REQUEST, AuthException.USER_IS_ALREADY_VERIFIED.getErrorCode(), r);
        });
    }

    private void captureToken() {
        Mockito.doReturn("mock link").when(emailLinkUtil)
                .getInvitationLink(tokenCaptor.capture(), any());
    }

    private TeamUserDTO getCompanyTeam(Integer userId) {
        return testHelper.getDetailsTeam(asCompanyOwner, companyId)
                .getTeam()
                .stream()
                .filter(u -> userId.equals(u.getId()))
                .findFirst().orElseThrow();
    }

    private void assertOneTokenStoredByUser(Integer userId) {
        long numberOfUserTokens = userTokenRepository.findAll().stream()
                .filter(t -> t.getUserId().equals(userId) && t.getType().equals(UserTokenType.INVITE_USER))
                .count();
        assertEquals(1, numberOfUserTokens);
    }

    private void assertNoTokenStoredByUser(Integer userId) {
        assertFalse(userTokenRepository.getCurrentToken(userId, UserTokenType.INVITE_USER).isPresent());
    }

}
