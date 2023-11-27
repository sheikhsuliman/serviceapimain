package com.siryus.swisscon.api.auth;

import com.siryus.swisscon.api.auth.signup.SignupDTO;
import com.siryus.swisscon.api.auth.signup.SignupResponseDTO;
import com.siryus.swisscon.api.auth.user.TeamUserAddDTO;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserRepository;
import com.siryus.swisscon.api.auth.user.UserRequestEntity;
import com.siryus.swisscon.api.auth.user.UserRequestRepository;
import com.siryus.swisscon.api.auth.user.UserRequestService;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.base.TestBuilder;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyRepository;
import com.siryus.swisscon.api.config.CustomCorsFilter;
import io.restassured.http.Cookies;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.siryus.swisscon.api.base.TestHelper.COMPANY_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PASSWORD;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_COUNTRY_CODE;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_EMAIL;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_FIRST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_FULL_PHONE;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_LAST_NAME;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_OWNER_PHONE;
import static com.siryus.swisscon.api.base.TestHelper.PROJECT_WORKER_FIRST_NAME;
import static io.restassured.RestAssured.given;
import java.time.LocalDateTime;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class LoginMvcIT extends AbstractMvcTestBase {

    private static Company testCompany;
    private static RequestSpecification asCompanyOwner;

    @SpyBean
    private LemonController lemonController;

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final UserRequestRepository userRequestRepository;

    @Autowired
    public LoginMvcIT(UserRepository userRepository, CompanyRepository companyRepository, UserRequestRepository userRequestRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.userRequestRepository = userRequestRepository;
    }

    @BeforeAll
    public void initTest() {
        cleanDatabase();
        SignupDTO signupDTO = TestBuilder.testSignupDTO(COMPANY_NAME, PROJECT_OWNER_FIRST_NAME, PROJECT_OWNER_LAST_NAME);
        SignupResponseDTO signupResponseDTO = testHelper.signUp(signupDTO);
        asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);
        testCompany = companyRepository.findById(signupResponseDTO.getCompanyId()).orElseThrow();

        // Make sure that, for the purposes of this test, the owner has a phone
        User u = userRepository.findByEmail(PROJECT_OWNER_EMAIL.toLowerCase()).orElseThrow();
        if (u.getMobile() == null) {
            u.setMobileCountryCode(PROJECT_OWNER_COUNTRY_CODE);
            u.setMobile(PROJECT_OWNER_PHONE);
            userRepository.save(u);
        }
    }

    @AfterEach
    public void clearLoginAttempts() {
        userRequestRepository.deleteAll();
    }

    @Test
    public void Given_UserAttemptsThreeFailedLogins_Then_FourthAttemptIsBlocked() throws Exception {
        tryLoginWithStatus(PROJECT_OWNER_EMAIL, PASSWORD, HttpStatus.OK);
        assertTrue(userRequestRepository.findAll().size() == 0, "Requests were found, even though they shouldn't have");

        tryLoginWithStatus(PROJECT_OWNER_EMAIL, PASSWORD  + 1, HttpStatus.UNAUTHORIZED);
        assertTrue(userRequestRepository.findAll().size() == 1, "Wrong number of previous requests");

        tryLoginWithStatus(PROJECT_OWNER_EMAIL, PASSWORD  + 1, HttpStatus.UNAUTHORIZED);
        assertTrue(userRequestRepository.findAll().size() == 2, "Wrong number of previous requests");

        tryLoginWithStatus(PROJECT_OWNER_EMAIL, PASSWORD  + 1, HttpStatus.UNAUTHORIZED);
        assertTrue(userRequestRepository.findAll().size() == 3, "Wrong number of previous requests");

        tryLoginWithStatus(PROJECT_OWNER_EMAIL, PASSWORD, HttpStatus.BAD_REQUEST);
        assertTrue(userRequestRepository.findAll().size() == 3, "Wrong number of previous requests");
    }

    @Test
    public void Given_UserAttemptsLessThanThreeFailedLogins_Then_CorrectLoginAttemptWorks() throws Exception {
        tryLoginWithStatus(PROJECT_OWNER_EMAIL, PASSWORD, HttpStatus.OK);

        tryLoginWithStatus(PROJECT_OWNER_EMAIL, PASSWORD  + 1, HttpStatus.UNAUTHORIZED);

        tryLoginWithStatus(PROJECT_OWNER_EMAIL, PASSWORD  + 1, HttpStatus.UNAUTHORIZED);

        tryLoginWithStatus(PROJECT_OWNER_EMAIL, PASSWORD, HttpStatus.OK);
        assertTrue(userRequestRepository.findAll().size() == 0, "Wrong number of previous requests");
    }

    @Test
    public void Given_UserAttemptsLoginAfterTimeout_Then_LoginAttemptWorks() throws Exception {
        tryLoginWithStatus(PROJECT_OWNER_EMAIL, PASSWORD, HttpStatus.OK);

        tryLoginWithStatus(PROJECT_OWNER_EMAIL, PASSWORD  + 1, HttpStatus.UNAUTHORIZED);
        tryLoginWithStatus(PROJECT_OWNER_EMAIL, PASSWORD  + 1, HttpStatus.UNAUTHORIZED);
        tryLoginWithStatus(PROJECT_OWNER_EMAIL, PASSWORD  + 1, HttpStatus.UNAUTHORIZED);
        tryLoginWithStatus(PROJECT_OWNER_EMAIL, PASSWORD, HttpStatus.BAD_REQUEST);

        UserRequestEntity ure = userRequestRepository.findAll().get(0);
        ure.setRequestedAt(LocalDateTime.now().minusMinutes(UserRequestService.NO_LOGIN_TIMEOUT_SECONDS + 1));
        userRequestRepository.save(ure);

        tryLoginWithStatus(PROJECT_OWNER_EMAIL, PASSWORD, HttpStatus.OK);

        assertTrue(userRequestRepository.findAll().size() == 0, "Wrong number of previous requests");
    }

    @Test
    public void Given_companyOwner_When_login_Then_success() throws Exception {
        this.testLogin(PROJECT_OWNER_EMAIL, PASSWORD);
    }

    @Test
    public void Given_userCreatedByRepository_When_login_Then_success() throws Exception {
        User user = User.builder().username("test@test.com").build();
        user.setPassword("{noop}test123ABC");
        user.setEmail("test@test.com");
        this.userRepository.save(user);

        this.testLogin(user.getUsername(), "test123ABC");
    }

    @Test
    public void Given_loginAttemptsWithDifferentNumberFormats_When_login_Then_success() throws Exception {
        testLogin("+" + PROJECT_OWNER_COUNTRY_CODE + PROJECT_OWNER_PHONE, PASSWORD);
        testLogin("+" + PROJECT_OWNER_COUNTRY_CODE + "(0)" + PROJECT_OWNER_PHONE, PASSWORD);
        testLogin("+" + PROJECT_OWNER_COUNTRY_CODE + "0" + PROJECT_OWNER_PHONE, PASSWORD);
        testLogin("00" + PROJECT_OWNER_COUNTRY_CODE + PROJECT_OWNER_PHONE, PASSWORD);
        testLogin("00" + PROJECT_OWNER_COUNTRY_CODE + "0" + PROJECT_OWNER_PHONE, PASSWORD);
        testLogin("00" + PROJECT_OWNER_COUNTRY_CODE + "(0)" + PROJECT_OWNER_PHONE, PASSWORD);
        testLogin("0" + PROJECT_OWNER_PHONE, PASSWORD);
        testLogin(PROJECT_OWNER_PHONE, PASSWORD);
    }

    @Test
    public void Given_InvalidNumbers_When_login_Then_unauthorized() {
        testHelper.login("12345678912345", PASSWORD, res-> res.assertThat().statusCode(HttpStatus.NOT_FOUND.value()));
        testHelper.login("ABC" + PROJECT_OWNER_COUNTRY_CODE + PROJECT_OWNER_PHONE, PASSWORD,
                res-> res.assertThat().statusCode(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void Given_TwoUsersWithSameMobileButDifferentCountryCodes_When_login_Then_error() {
        String testNumber = PROJECT_OWNER_PHONE + "1";
        testHelper.inviteUser(asCompanyOwner, TestBuilder.testTeamUserAddDTO(PROJECT_OWNER_COUNTRY_CODE -1, testNumber));
        testHelper.inviteUser(asCompanyOwner, TestBuilder.testTeamUserAddDTO(PROJECT_OWNER_COUNTRY_CODE -2, testNumber));

        testHelper.login(testNumber, PASSWORD, res -> TestAssert
                .assertError(HttpStatus.BAD_REQUEST,
                        AuthException.MULTIPLE_USERS_WITH_PHONE_FOUND.getErrorCode(), res));
    }

    @Test
    public void Given_twoUsersWithSameEmailButOneDisabled_When_login_then_success() {
        RequestSpecification asCompanyOwner = testHelper.login(PROJECT_OWNER_EMAIL);

        // add a user to the company
        TeamUserAddDTO teamUserAddDTO = TestBuilder
                .testTeamUserAddDTO(COMPANY_NAME, PROJECT_WORKER_FIRST_NAME, PROJECT_WORKER_FIRST_NAME);
        TeamUserDTO teamUserDTO = testHelper.inviteUserAndResetPassword(asCompanyOwner, teamUserAddDTO);

        // remove the user from company (user will be disabled)
        testHelper.removeUserFromCompany(asCompanyOwner, testCompany.getId(), teamUserDTO.getId());

        // login should still work > despite we have duplicate mail in the database
        testHelper.login(PROJECT_OWNER_EMAIL);
    }

    @Test
    public void Given_wrongOrigin_When_login_Then_forbidden() throws Exception {
        log.info("testLogin");
        mvc.perform(post("/api/rest/auth/login")
                .param("username", "test.albin-borer-ag.ch@siryus.com")
                .param("password", "cocoTest6")
                .header("contentType", MediaType.APPLICATION_FORM_URLENCODED)
                .header("Origin", "https://www.randomaddress.com"))
                .andExpect(status().is(403));
    }

    @Test
    public void Given_UserLogsInWithPhone_Then_LoginIsSuccessful() throws Exception {
        log.info("testPhoneLogin");

        mvc.perform(post("/api/rest/auth/login")
                .param("username", PROJECT_OWNER_FULL_PHONE)
                .param("password", PASSWORD)
                .header("contentType", MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    public void Given_loggedInUser_When_logout_Then_success() {
        io.restassured.http.Cookie loginCookie = buildRequestCookie(login(ADMIN_EMAIL, ADMIN_PASSWORD));
        RequestSpecification spec = defaultSpec();
        spec.cookie(loginCookie);

        Cookies result = given().spec(spec)
                                .post(BASE_PATH + "/auth/logout")
                                .getDetailedCookies();

        assertTrue(result.hasCookieWithName(loginCookie.getName()), "Siryus cookie was not included in response");

        // All fields should be the same except for Max-Age which should be set to 0
        io.restassured.http.Cookie c = result.get(loginCookie.getName());
        // new Date(10000) will be January 1970 which is the year used by the LegacyCookieProcessor as ANCIENT DATE in the past
        assertTrue(c.hasMaxAge() && c.getMaxAge() == 0 || c.hasExpiryDate() && c.getExpiryDate().compareTo(new java.util.Date(10000)) == 0, "Max age is not set to 0");
        assertTrue(c.hasDomain() && c.getDomain().equals(loginCookie.getDomain()), "Missing or invalid domain value");
        assertTrue(c.hasValue() && c.getValue().equals(loginCookie.getValue()), "Missing or invalid cookie value");
        assertEquals(c.isHttpOnly(), loginCookie.isHttpOnly(), "Invalid httpOnly value");
        assertTrue(c.hasPath() && c.getPath().equals(loginCookie.getPath()), "Missing or invalid path value");
        assertEquals(c.isSecured(), loginCookie.isSecured(), "Invalid secure value");
    }

    @Test
    public void Given_userNotLoggedIn_When_logout_Then_unauthorized() {
        testHelper.logout(defaultSpec(), r ->
                TestAssert.assertError(HttpStatus.UNAUTHORIZED,
                        AuthException.UNAUTHORIZED_REQUEST.getErrorCode(), r));
    }

    @Test
    public void Given_invalidCredentials_When_login_Then_unauthorized() {
        testHelper.login(PROJECT_OWNER_EMAIL, PASSWORD + "x", r ->
                TestAssert.assertError(HttpStatus.UNAUTHORIZED,
                        AuthException.AUTHENTICATION_FAILURE.getErrorCode(), r));
    }

    @Test
    public void Given_inExistingUserEmail_When_login_Then_throw() {
        String inExistingMail = "Inexisting@usermail.com";
        testHelper.login(inExistingMail, PASSWORD, r ->
                TestAssert.assertError(HttpStatus.NOT_FOUND,
                        AuthException.USER_WITH_USERNAME_NOT_FOUND.getErrorCode(),
                        r));
    }

    @Test
    public void Given_InternalServerError_When_Login_Then_showStacktrace() {
        Mockito.doThrow(new NullPointerException())
                .when(lemonController)
                .logout(Mockito.any(), Mockito.any());
        testHelper.logout(asCompanyOwner, r -> TestAssert.assertErrorContains(HttpStatus.INTERNAL_SERVER_ERROR, r,
                "java.lang.NullPointerException", "at org.springframework."));
    }

    /**
     * test login method which also checks necessary headers
     */
    private void testLogin(String email, String password) throws Exception {
        log.info("testLogin");
        mvc.perform(post("/api/rest/auth/login")
                .param("username", email)
                .param("password", password)
                .header("contentType", MediaType.APPLICATION_FORM_URLENCODED)
                .header("Origin", ORIGIN))
                .andExpect(status().is(200))
                .andExpect(header().string(SET_COOKIE_HEADER, containsString(cookieService.getCookieName())))
                .andExpect(header().string(CustomCorsFilter.ALLOW_ORIGIN_HEADER, equalTo("https://www.siryus.com")))
                .andExpect(header().string(CustomCorsFilter.ALLOW_METHODS_HEADER, equalTo("GET, POST, PUT, DELETE, HEAD, OPTIONS")))
                .andExpect(header().string(CustomCorsFilter.ALLOW_CREDENTIALS_HEADER, equalTo("true")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.unverified").value(false))
                .andExpect(jsonPath("$.blocked").value(false))
                .andExpect(jsonPath("$.goodUser").value(true));
    }

    private void tryLoginWithStatus(String email, String password, HttpStatus status) throws Exception {
        mvc.perform(post("/api/rest/auth/login")
                .param("username", email)
                .param("password", password)
                .header("contentType", MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is(status.value()));
    }
}
