package com.siryus.swisscon.api.auth;

import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.signup.SignupDTO;
import com.siryus.swisscon.api.auth.signup.SignupResponseDTO;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.auth.usertoken.UserTokenEntity;
import com.siryus.swisscon.api.auth.usertoken.UserTokenService;
import com.siryus.swisscon.api.auth.usertoken.UserTokenType;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.FailFastExtension;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestHelper;
import com.siryus.swisscon.api.company.company.CompanyDetailsDTO;
import com.siryus.swisscon.api.customroles.CustomRoleReader;
import io.restassured.specification.RequestSpecification;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(FailFastExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InviteCompanyIT extends AbstractMvcTestBase {

    private static final String INIT_USER_EMAIL = "inviteIT@test.com";
    private static final String CHANGED_COMPANY_NAME = "invitedCompany";

    private RequestSpecification asInitUser;

    private static String validEmailToken;
    private static String validMobileToken;
    private static Integer emailCompanyId;
    private static Integer mobileCompanyId;

    private final ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

    private final CustomRoleReader customRoleReader;
    private final UserTokenService userTokenService;

    @Autowired
    public InviteCompanyIT(CustomRoleReader customRoleReader, UserTokenService userTokenService) {
        this.customRoleReader = customRoleReader;
        this.userTokenService = userTokenService;
    }

    @Test
    @Order(1)
    public void Given_noUsersOnPlatform_When_SignupWithSpecialToken_then_UserAndCompanyIsCreated() {
        SignupResponseDTO signupResponseDTO = testHelper
                .signupFromInvite(testBuilder.testSignupDTOWithoutInvite("init", "first", "last", INIT_USER_EMAIL));
        asInitUser = testHelper.login(INIT_USER_EMAIL);

        CompanyDetailsDTO detailsTeam = testHelper.getDetailsTeam(asInitUser, signupResponseDTO.getCompanyId());
        TestAssert.assertTeamContainsUser(detailsTeam.getTeam(), signupResponseDTO.getUserId());
    }

    @Test
    @Order(2)
    public void Given_bootstrapRole_When_inviteCompany_Then_Throw() {
        testHelper.inviteCompany(asInitUser, testBuilder
                .testCompanyInviteDTO(TestHelper.COMPANY_NAME,
                        TestHelper.PROJECT_OWNER_EMAIL,
                        RoleName.COMPANY_BOOTSTRAP), r -> {
            TestAssert.assertError(HttpStatus.BAD_REQUEST, AuthException.INVITED_USERS_CANNOT_HAVE_BOOTSTRAP_ROLE.getErrorCode(), r);
            return null;
        });
    }

    @Test
    @Order(3)
    public void Given_notLoggedIn_When_inviteCompany_Then_Throw() {
        testHelper.inviteCompany(defaultSpec(), testBuilder
                .testCompanyInviteDTO(TestHelper.COMPANY_NAME,
                        TestHelper.PROJECT_OWNER_EMAIL,
                        RoleName.CUSTOMER), r -> {
            r.statusCode(HttpStatus.UNAUTHORIZED.value());
            return null;
        });
    }

    @Test
    @Order(4)
    public void Given_InvalidMail_When_inviteCompany_Then_Throw() {
        testHelper.inviteCompany(asInitUser, testBuilder
                .testCompanyInviteDTO(TestHelper.COMPANY_NAME,
                        "324abc.com",
                        RoleName.CUSTOMER), r -> {
            TestAssert.assertError(HttpStatus.BAD_REQUEST, AuthException.EMAIL_OR_PHONE_NOT_CORRECTLY_FORMATTED.getErrorCode(), r);
            return null;
        });
    }

    @Test
    @Order(5)
    public void Given_InvalidPhone_When_inviteCompany_Then_Throw() {
        testHelper.inviteCompany(asInitUser, testBuilder
                .testCompanyInviteDTO(TestHelper.COMPANY_NAME,
                        52,
                        "&*xabsjag",
                        RoleName.CUSTOMER), r -> {
            TestAssert.assertError(HttpStatus.BAD_REQUEST, AuthException.EMAIL_OR_PHONE_NOT_CORRECTLY_FORMATTED.getErrorCode(), r);
            return null;
        });
    }

    @Test
    @Order(6)
    public void Given_invalidRole_When_inviteCompany_Then_Throw() {
        emailCompanyId = testHelper.inviteCompany(asInitUser, testBuilder
                        .testCompanyInviteDTO(TestHelper.COMPANY_NAME,
                                TestHelper.PROJECT_OWNER_EMAIL,
                                RoleName.PROJECT_WORKER),
                r -> {
                    TestAssert.assertError(HttpStatus.BAD_REQUEST, AuthException.ROLE_FOR_COMPANY_USER_CANNOT_BE_PROJECT_ROLE.getErrorCode(), r);
                    return null;
                }
        );
    }

    @Test
    @Order(7)
    public void Given_initUser_When_inviteCompanyByMail_then_UserAndCompanyIsCreated() {
        captureToken();
        emailCompanyId = testHelper.inviteCompany(asInitUser, testBuilder
                .testCompanyInviteDTO(TestHelper.COMPANY_NAME,
                        TestHelper.PROJECT_OWNER_EMAIL,
                        RoleName.CUSTOMER));
        CompanyDetailsDTO detailsTeam = testHelper.getDetailsTeam(asInitUser, emailCompanyId);
        assertEquals(1, detailsTeam.getTeam().size());
        TeamUserDTO teamUserDTO = detailsTeam.getTeam().get(0);
        assertTrue(teamUserDTO.getRoleIds()
                .contains(customRoleReader.getRoleByName(RoleName.CUSTOMER.name()).getId()));
        assertTrue(teamUserDTO.getIsUnverified());

        validEmailToken = tokenCaptor.getValue();
        assertTokenIsStoredByExternalId(String.valueOf(emailCompanyId));
    }

    @Test
    @Order(8)
    public void Given_initUser_When_inviteCompanyByMailTheSecondTime_then_UserAndCompanyIsStilltheSameButNewToken() {
        captureToken();
        Integer secondCompanyId = testHelper.inviteCompany(asInitUser, testBuilder
                .testCompanyInviteDTO(TestHelper.COMPANY_NAME,
                        TestHelper.PROJECT_OWNER_EMAIL,
                        RoleName.CUSTOMER));
        assertEquals(emailCompanyId, secondCompanyId);
        CompanyDetailsDTO detailsTeam = testHelper.getDetailsTeam(asInitUser, emailCompanyId);
        assertEquals(1, detailsTeam.getTeam().size());
        TeamUserDTO teamUserDTO = detailsTeam.getTeam().get(0);
        assertTrue(teamUserDTO.getRoleIds()
                .contains(customRoleReader.getRoleByName(RoleName.CUSTOMER.name()).getId()));
        assertTrue(teamUserDTO.getIsUnverified());

        validEmailToken = tokenCaptor.getValue();
        assertTokenIsStoredByExternalId(String.valueOf(emailCompanyId));
    }

    @Test
    @Order(9)
    public void Given_initUser_When_inviteCompanyByPhone_then_UserAndCompanyIsCreated() {
        captureToken();
        mobileCompanyId = testHelper.inviteCompany(asInitUser, testBuilder
                .testCompanyInviteDTO(TestHelper.COMPANY_NAME,
                        TestHelper.PROJECT_OWNER_COUNTRY_CODE,
                        TestHelper.PROJECT_OWNER_PHONE,
                        RoleName.COMPANY_OWNER));
        CompanyDetailsDTO detailsTeam = testHelper.getDetailsTeam(asInitUser, mobileCompanyId);
        assertEquals(1, detailsTeam.getTeam().size());
        TeamUserDTO teamUserDTO = detailsTeam.getTeam().get(0);
        assertTrue(teamUserDTO.getRoleIds()
                .contains(customRoleReader.getRoleByName(RoleName.COMPANY_OWNER.name()).getId()));
        assertTrue(teamUserDTO.getIsUnverified());

        validMobileToken = tokenCaptor.getValue();
        assertTokenIsStoredByExternalId(String.valueOf(mobileCompanyId));
    }

    @Test
    @Order(10)
    public void Given_WrongCompanyId_When_VerifySignup_Then_Throw() {
        testHelper.verifySignup(45685, validEmailToken, r -> {
            TestAssert.assertError(HttpStatus.BAD_REQUEST, AuthException.TOKEN_IS_NOT_VALID_OR_EXPIRED.getErrorCode(), r);
            return null;
        });
    }

    @Test
    @Order(11)
    public void Given_WrongCode_When_VerifySignup_Then_Throw() {
        testHelper.verifySignup(emailCompanyId, "WRONG_CODE", r -> {
            TestAssert.assertError(HttpStatus.BAD_REQUEST, AuthException.TOKEN_IS_NOT_VALID_OR_EXPIRED.getErrorCode(), r);
            return null;
        });
    }

    @Test
    @Order(10)
    public void Given_invitedUserPerMail_When_verfiySignup_Then_Success() {
        SignupDTO signupDTO = testHelper.verifySignup(emailCompanyId, validEmailToken);
        assertEquals(TestHelper.COMPANY_NAME, signupDTO.getCompany().getName());
        assertEquals(emailCompanyId, signupDTO.getCompany().getCompanyId());
    }

    @Test
    @Order(11)
    public void Given_invitedUserPerMobile_When_verfiySignup_Then_Success() {
        SignupDTO signupDTO = testHelper.verifySignup(mobileCompanyId, validMobileToken);
        assertEquals(TestHelper.COMPANY_NAME, signupDTO.getCompany().getName());
        assertEquals(mobileCompanyId, signupDTO.getCompany().getCompanyId());
    }

    @Test
    @Order(12)
    public void Given_WrongCompanyId_When_Signup_Then_Throw() {
        testHelper.signupFromInvite(testBuilder.testSignupDTO(
                562316, CHANGED_COMPANY_NAME, TestHelper.PROJECT_OWNER_FIRST_NAME,
                TestHelper.PROJECT_OWNER_LAST_NAME, validEmailToken),
                r -> {
                    TestAssert.assertError(HttpStatus.BAD_REQUEST, AuthException.TOKEN_IS_NOT_VALID_OR_EXPIRED.getErrorCode(), r);
                    return null;
                }
        );
    }

    @Test
    @Order(13)
    public void Given_WrongCode_When_Signup_Then_Throw() {
        testHelper.signupFromInvite(testBuilder.testSignupDTO(
                emailCompanyId, CHANGED_COMPANY_NAME, TestHelper.PROJECT_OWNER_FIRST_NAME,
                TestHelper.PROJECT_OWNER_LAST_NAME, "WRONG_TOKEN"),
                r -> {
                    TestAssert.assertError(HttpStatus.BAD_REQUEST, AuthException.TOKEN_IS_NOT_VALID_OR_EXPIRED.getErrorCode(), r);
                    return null;
                }
        );
    }

    @Test
    @Order(14)
    public void Given_invitedUserPerEmail_When_signup_Then_Success() {
        SignupResponseDTO signupResponseDTO = testHelper.signupFromInvite(testBuilder.testSignupDTO(
                emailCompanyId,
                CHANGED_COMPANY_NAME,
                TestHelper.PROJECT_OWNER_FIRST_NAME,
                TestHelper.PROJECT_OWNER_LAST_NAME,
                validEmailToken));
        assertEquals(emailCompanyId, signupResponseDTO.getCompanyId());

        CompanyDetailsDTO detailsTeam = testHelper.getDetailsTeam(asInitUser, emailCompanyId);
        assertEquals(1, detailsTeam.getTeam().size());
        TeamUserDTO teamUserDTO = detailsTeam.getTeam().get(0);
        assertTrue(teamUserDTO.getRoleIds()
                .contains(customRoleReader.getRoleByName(RoleName.CUSTOMER.name()).getId()));
        assertEquals(CHANGED_COMPANY_NAME, detailsTeam.getName());
        assertEquals(TestHelper.PROJECT_OWNER_FIRST_NAME, teamUserDTO.getFirstName());
        assertEquals(TestHelper.PROJECT_OWNER_LAST_NAME, teamUserDTO.getLastName());
        assertFalse(teamUserDTO.getIsUnverified());
    }

    @Test
    @Order(15)
    public void Given_invitedUserPerMobile_When_signup_Then_Success() {
        SignupResponseDTO signupResponseDTO = testHelper.signupFromInvite(testBuilder.testSignupDTO(
                mobileCompanyId,
                TestHelper.CONTRACTOR_COMPANY_NAME,
                TestHelper.PROJECT_WORKER_FIRST_NAME,
                TestHelper.PROJECT_WORKER_LAST_NAME,
                validMobileToken));
        assertEquals(mobileCompanyId, signupResponseDTO.getCompanyId());

        CompanyDetailsDTO detailsTeam = testHelper.getDetailsTeam(asInitUser, mobileCompanyId);
        assertEquals(1, detailsTeam.getTeam().size());
        TeamUserDTO teamUserDTO = detailsTeam.getTeam().get(0);
        assertTrue(teamUserDTO.getRoleIds()
                .contains(customRoleReader.getRoleByName(RoleName.COMPANY_OWNER.name()).getId()));
        assertEquals(TestHelper.CONTRACTOR_COMPANY_NAME, detailsTeam.getName());
        assertEquals(TestHelper.PROJECT_WORKER_FIRST_NAME, teamUserDTO.getFirstName());
        assertEquals(TestHelper.PROJECT_WORKER_LAST_NAME, teamUserDTO.getLastName());
        assertFalse(teamUserDTO.getIsUnverified());
    }

    @Test
    @Order(16)
    public void Given_existingVerifiedUserWithEmail_When_InviteCompany_Then_Throw() {
        testHelper.inviteCompany(asInitUser, testBuilder
                        .testCompanyInviteDTO(TestHelper.COMPANY_NAME,
                                TestHelper.PROJECT_OWNER_EMAIL,
                                RoleName.CUSTOMER),
                r -> {
                    TestAssert.assertError(HttpStatus.CONFLICT, AuthException.EMAIL_OR_PHONE_ALREADY_EXIST.getErrorCode(), r);
                    return null;
                });
    }

    @Test
    @Order(17)
    public void Given_existingVerifiedUserWithPhone_When_InviteCompany_Then_Throw() {
        testHelper.inviteCompany(asInitUser, testBuilder
                        .testCompanyInviteDTO(TestHelper.COMPANY_NAME,
                                TestHelper.PROJECT_OWNER_COUNTRY_CODE,
                                TestHelper.PROJECT_OWNER_PHONE,
                                RoleName.COMPANY_OWNER),
                r -> {
                    TestAssert.assertError(HttpStatus.CONFLICT, AuthException.EMAIL_OR_PHONE_ALREADY_EXIST.getErrorCode(), r);
                    return null;
                });
    }

    private void captureToken() {
        Mockito.doReturn("mock link")
                .when(emailLinkUtil)
                .getSignupLink(any(), tokenCaptor.capture());
    }

    private void assertTokenIsStoredByExternalId(String externalId) {
        List<UserTokenEntity> tokens = userTokenService
                .findTokensByExternalId(externalId, UserTokenType.INVITE_COMPANY);
        assertFalse(tokens.isEmpty());
    }

}
