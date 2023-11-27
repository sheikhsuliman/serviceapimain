package com.siryus.swisscon.api.auth;

import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.role.RoleRepository;
import com.siryus.swisscon.api.auth.signup.SignupDTO;
import com.siryus.swisscon.api.auth.signup.SignupResponseDTO;
import com.siryus.swisscon.api.auth.user.CompanyInviteDTO;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.auth.usertoken.UserTokenEntity;
import com.siryus.swisscon.api.auth.usertoken.UserTokenRepository;
import com.siryus.swisscon.api.auth.usertoken.UserTokenType;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.FailFastExtension;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyDetailsDTO;
import com.siryus.swisscon.api.company.company.CompanyRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.exceptions.NotFoundException;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.siryus.swisscon.api.base.TestBuilder.testSignupDTO;
import static com.siryus.swisscon.api.base.TestHelper.COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PASSWORD;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_COUNTRY_CODE;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_LAST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_PHONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(FailFastExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Deprecated //TODO remove after SI-177
public class InviteCompanyITDeprecated extends AbstractMvcTestBase {

    private static final String INVITED_COMPANY = "invited-company";
    private static final String INVITED_FIRSTNAME = "invited-firstname";
    private static final String INVITED_LASTNAME = "invited-lastname";

    private static final Integer COUNTRY_CODE = 41;
    private static final String MOBILE = "456123456";
    private static final String FULL_MOBILE = COUNTRY_CODE + MOBILE;

    private static RequestSpecification asCompanyOwner;
    private static Integer companyOwnerRoleId;
    private static Integer companyCustomerRoleId;
    private static Integer companyWorkerRoleId;

    @Value("${lemon.default-signup-token}")
    private String defaultSignupToken;

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final CompanyUserRoleRepository commpanyUserRoleRepository;
    private final RoleRepository roleRepository;


    private static String invitedEmail;
    private static String validEmailToken;
    private static String validMobileToken;

    private final ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

    @Autowired
    public InviteCompanyITDeprecated(CompanyRepository companyRepository, UserRepository userRepository, UserTokenRepository userTokenRepository, CompanyUserRoleRepository commpanyUserRoleRepository, RoleRepository roleRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.userTokenRepository = userTokenRepository;
        this.commpanyUserRoleRepository = commpanyUserRoleRepository;
        this.roleRepository = roleRepository;
    }

    @BeforeAll()
    public void doBeforeAll() {
        invitedEmail = getRandomMail();
        testHelper.signUp(testSignupDTO(COMPANY_NAME, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME));
        asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);

        companyOwnerRoleId = roleRepository.getRoleByName(RoleName.COMPANY_OWNER.toString()).getId();
        companyCustomerRoleId = roleRepository.getRoleByName(RoleName.CUSTOMER.toString()).getId();
        companyWorkerRoleId = roleRepository.getRoleByName(RoleName.COMPANY_WORKER.toString()).getId();
    }

    @Test
    @Order(0)
    public void Given_notLoggedIn_When_inviteCompany_Then_Throw() {
        testHelper.inviteCompanyDeprecated(defaultSpec(), TestBuilder.testCompanyInviteDTOdeprecated("valid@valid.com"), HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(1)
    public void Given_InvalidMail_When_inviteCompany_Then_Throw() {
        testHelper.inviteCompanyDeprecated(asCompanyOwner, TestBuilder.testCompanyInviteDTOdeprecated("324abc.com"), HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(2)
    public void Given_InvalidPhone_When_inviteCompany_Then_Throw() {
        testHelper.inviteCompanyDeprecated(asCompanyOwner, TestBuilder.testCompanyInviteDTOdeprecated(52, "&*xabsjag"), HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(3)
    public void Given_invalidRole_When_inviteCompany_Then_Throw() {
        CompanyInviteDTO companyInviteDTO = TestBuilder.testCompanyInviteDTOdeprecated(COUNTRY_CODE, MOBILE);
        companyInviteDTO.setRoleId(companyWorkerRoleId);
        testHelper.inviteCompanyDeprecated(asCompanyOwner, TestBuilder.testCompanyInviteDTOdeprecated(52, "&*xabsjag"), HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(4)
    public void Given_validEmail_WhenInvite_Then_SendInvitationMail() {
        captureToken();
        testHelper.inviteCompanyDeprecated(asCompanyOwner, TestBuilder.testCompanyInviteDTOdeprecated(invitedEmail));
        validEmailToken = tokenCaptor.getValue();
        assertTokenIsStoredByExternalId(invitedEmail);
    }

    @Test
    @Order(5)
    public void Given_validPhone_When_inviteCompany_Then_SenInvitationSms() {
        CompanyInviteDTO companyInviteDTO = TestBuilder.testCompanyInviteDTOdeprecated(COUNTRY_CODE, MOBILE);
        companyInviteDTO.setRoleId(companyCustomerRoleId);
        captureToken();
        testHelper.inviteCompanyDeprecated(asCompanyOwner, companyInviteDTO);
        validMobileToken = tokenCaptor.getValue();
        assertTokenIsStoredByExternalId(FULL_MOBILE);
    }


    @Test
    @Order(6)
    public void Given_WrongEmail_When_VerifySignupCode_Then_Throw() {
        testHelper.verifySignupCode("wrong-mail@siryus.com", validEmailToken, HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(7)
    public void Given_WrongCode_When_VerifySignupCode_Then_Throw() {
        testHelper.verifySignupCode(invitedEmail, "wrong code", HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(8)
    public void Given_DefaultSignupToken_When_VerifySignupCode_Then_Success() {
        testHelper.verifySignupCode(invitedEmail, defaultSignupToken, HttpStatus.OK);
    }

    @Test
    @Order(9)
    public void Given_validEmailToken_When_VerifySignupCode_Then_Success() {
        testHelper.verifySignupCode(invitedEmail, validEmailToken, HttpStatus.OK);
    }

    @Test
    @Order(10)
    public void Given_validMobileToken_When_VerifySignupCode_Then_Success() {
        testHelper.verifySignupCode(FULL_MOBILE, validMobileToken, HttpStatus.OK);
    }


    @Test
    @Order(11)
    public void Given_WrongEmail_When_Signup_Then_Throw() {
        SignupDTO signupDTO = testSignupDTO("x", "x", "x", "wrong-mail@siryus.com", validEmailToken);
        testHelper.signUp(signupDTO, HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(12)
    public void Given_WrongCode_When_Signup_Then_Throw() {
        SignupDTO signupDTO = testSignupDTO("x", "x", "x", invitedEmail, "wrong code");
        testHelper.signUp(signupDTO, HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(13)
    public void Given_validEmailToken_When_Signup_Then_Success() {
        SignupDTO signupDTO = testSignupDTO(INVITED_COMPANY, INVITED_FIRSTNAME, INVITED_LASTNAME, invitedEmail, validEmailToken);
        SignupResponseDTO signupResponseDTO = testHelper.signUp(signupDTO);
        assertCompanyUser(signupDTO, signupResponseDTO);
        assertCompanyUserRole(signupResponseDTO.getUserId(), companyOwnerRoleId);
        assertTokenIsInvalidated(invitedEmail);

        CompanyDetailsDTO detailsTeam = testHelper.getDetailsTeam(asCompanyOwner, signupResponseDTO.getCompanyId());
        TestAssert.assertDetailsTeam(signupDTO, signupResponseDTO, detailsTeam);
    }

    @Test
    @Order(14)
    public void Given_validMobileToken_When_Signup_Then_Success() {
        SignupDTO signupDTO = testSignupDTO(INVITED_COMPANY, INVITED_FIRSTNAME, INVITED_LASTNAME, FULL_MOBILE, validMobileToken);
        signupDTO.getUser().setEmail(getRandomMail());
        SignupResponseDTO signupResponseDTO = testHelper.signUp(signupDTO);
        assertCompanyUser(signupDTO, signupResponseDTO);
        assertCompanyUserRole(signupResponseDTO.getUserId(), companyCustomerRoleId);
        assertTokenIsInvalidated(FULL_MOBILE);

        CompanyDetailsDTO detailsTeam = testHelper.getDetailsTeam(asCompanyOwner, signupResponseDTO.getCompanyId());
        TestAssert.assertDetailsTeam(signupDTO, signupResponseDTO, detailsTeam);
    }

    @Test
    @Order(15)
    public void Given_existingUnverifiedUserWithEmail_When_InviteCompany_Then_InvitedUserIsDeleted() {
        String anotherMail = getRandomMail();
        TeamUserDTO invitedUser = testHelper.inviteUser(asCompanyOwner, TestBuilder.testTeamUserAddDTO(anotherMail));
        testHelper.inviteCompanyDeprecated(asCompanyOwner, TestBuilder.testCompanyInviteDTOdeprecated(anotherMail));

        assertFalse(userRepository.findByEmail(anotherMail).isPresent());
        assertTrue(userTokenRepository.findAll().stream()
                .noneMatch(token->token.getUserId().equals(invitedUser.getId())));
    }

    @Test
    @Order(16)
    public void Given_existingUnverifiedUserWithPhone_When_InviteCompany_Then_InvitedUserIsDeleted() {
        String anotherPhone = PROJECT_OWNER_PHONE + RandomStringUtils.randomNumeric(2);
        TeamUserDTO invitedUser = testHelper.inviteUser(asCompanyOwner,
                TestBuilder.testTeamUserAddDTO(PROJECT_OWNER_COUNTRY_CODE, anotherPhone));
        testHelper.inviteCompanyDeprecated(asCompanyOwner,
                TestBuilder.testCompanyInviteDTOdeprecated(PROJECT_OWNER_COUNTRY_CODE, anotherPhone));

        assertFalse(userRepository.findByMobile(PROJECT_OWNER_COUNTRY_CODE + anotherPhone).isPresent());
        assertTrue(userTokenRepository.findAll().stream()
                .noneMatch(token->token.getUserId().equals(invitedUser.getId())));
    }

    @Test
    @Order(17)
    public void Given_existingVerifiedUserWithEmail_When_InviteCompany_Then_Throw() {
        String anotherMail = getRandomMail();
        captureInviteUserToken();
        testHelper.inviteUser(asCompanyOwner, TestBuilder.testTeamUserAddDTO(anotherMail));
        testHelper.resetPassword(asCompanyOwner,
                TestBuilder.testResetPasswordForm(tokenCaptor.getValue(), PASSWORD));
        testHelper.inviteCompanyDeprecated(asCompanyOwner, TestBuilder.testCompanyInviteDTOdeprecated(anotherMail),HttpStatus.CONFLICT);
    }

    @Test
    @Order(18)
    public void Given_existingVerifiedUserWithPhone_When_InviteCompany_Then_Throw() {
        String anotherPhone = PROJECT_OWNER_PHONE + RandomStringUtils.randomNumeric(3);
        captureInviteUserToken();
        testHelper.inviteUser(asCompanyOwner, TestBuilder.testTeamUserAddDTO(PROJECT_OWNER_COUNTRY_CODE, anotherPhone));
        testHelper.resetPassword(asCompanyOwner,
                TestBuilder.testResetPasswordForm(tokenCaptor.getValue(), PASSWORD));
        testHelper.inviteCompanyDeprecated(asCompanyOwner, TestBuilder.testCompanyInviteDTOdeprecated(PROJECT_OWNER_COUNTRY_CODE, anotherPhone),HttpStatus.CONFLICT);
    }

    /**
     * We disable all users here > that means we can signup without signup code.
     * Because without any users nobody can invite anyone
     */
    @Test
    @Order(19)
    public void Given_AllUsersDisabled_When_VerifySignupToken_Then_Success() {
        userRepository.findAll().forEach(u -> userRepository.disable(u.getId()));
        testHelper.verifySignupCode(invitedEmail, "wrong-code", HttpStatus.OK);
    }

    /**
     * Users where disabled > that means we can signup without signup code.
     * Because without any users nobody can invite anyone
     */
    @Test
    @Order(20)
    public void Given_AllUsersDisabled_When_Signup_Then_Success() {
        SignupDTO signupDTO = testSignupDTO(INVITED_COMPANY, INVITED_FIRSTNAME, INVITED_LASTNAME, "mail@mail.com", "wrong-code");

        SignupResponseDTO signupResponseDTO = testHelper.signUp(signupDTO);
        assertCompanyUser(signupDTO, signupResponseDTO);
        assertCompanyUserRole(signupResponseDTO.getUserId(), companyOwnerRoleId);
    }

    @Test
    @Order(21)
    public void Given_DefaultToken_When_Signup_Then_Success() {
        SignupDTO signupDTO = testSignupDTO(INVITED_COMPANY + "2", INVITED_FIRSTNAME + "2", INVITED_LASTNAME + "2", "default@mail.com", defaultSignupToken);

        SignupResponseDTO signupResponseDTO = testHelper.signUp(signupDTO);
        assertCompanyUser(signupDTO, signupResponseDTO);
        assertCompanyUserRole(signupResponseDTO.getUserId(), companyOwnerRoleId);
    }

    private void captureInviteUserToken() {
        Mockito.doReturn("mock link").when(emailLinkUtil)
                .getInvitationLink(tokenCaptor.capture(), any());
    }

    private void captureToken() {
        Mockito.doReturn("mock link")
                .when(emailLinkUtil)
                .getSignupLinkDeprecated(any(), tokenCaptor.capture());
    }

    private void assertTokenIsStoredByExternalId(String externalId) {
        List<UserTokenEntity> tokens = userTokenRepository
                .findTokensByExternalId(externalId, UserTokenType.INVITE_COMPANY);
        assertFalse(tokens.isEmpty());
    }

    private void assertTokenIsInvalidated(String externalId) {
        assertTrue(userTokenRepository.findTokensByExternalId(externalId, UserTokenType.INVITE_COMPANY).isEmpty());
    }

    private String getRandomMail() {
        return RandomStringUtils.randomAlphabetic(10) + "@siryus.com";
    }

    private void assertCompanyUser(SignupDTO signupDTO, SignupResponseDTO signupResponseDTO) {
        // assert company
        Company company = companyRepository.findById(signupResponseDTO.getCompanyId()).orElseThrow(NotFoundException::new);
        assertEquals(signupDTO.getCompany().getName(), company.getName());

        // assert user
        User user = userRepository.findById(signupResponseDTO.getUserId()).orElseThrow(IllegalStateException::new);
        assertEquals(signupDTO.getUser().getFirstName(), user.getGivenName());
        assertEquals(signupDTO.getUser().getLastName(), user.getSurName());
    }

    private void assertCompanyUserRole(Integer userId, Integer roleId) {
        List<CompanyUserRole> companyUserRoles = commpanyUserRoleRepository.findByUser(userId);
        assertFalse(companyUserRoles.isEmpty());
        assertTrue(companyUserRoles.stream().anyMatch(cur->cur.getRole().getId().equals(roleId)));
    }

}
