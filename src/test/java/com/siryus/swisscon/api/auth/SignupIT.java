package com.siryus.swisscon.api.auth;

import com.naturalprogrammer.spring.lemon.commons.util.UserUtils;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.signup.SignupCompanyDTO;
import com.siryus.swisscon.api.auth.signup.SignupDTO;
import com.siryus.swisscon.api.auth.signup.SignupResponseDTO;
import com.siryus.swisscon.api.auth.signup.SignupUserDTO;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.auth.user.UserService;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyService;
import com.siryus.swisscon.api.company.companytrade.CompanyTrade;
import com.siryus.swisscon.api.company.companytrade.CompanyTradeRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.config.CustomCorsFilter;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test the Signup Process of the {@link LemonController} and {@link LemonService}
 */
public class SignupIT extends AbstractMvcTestBase {

    private static final String PATH = BASE_PATH + "/auth/signup";

    private static Company testCompany;
    private static User testUser;
    private static CompanyUserRole testCompanyUserRole;
    private static List<CompanyTrade> testCompanyTrades;

    private final UserService userService;
    private final CompanyService companyService;
    private final CompanyTradeRepository companyTradeRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;

    private final AtomicInteger signUpDtoGeneration = new AtomicInteger(0);

    @Autowired
    public SignupIT(
            UserService userService,
            CompanyService companyService,
            CompanyTradeRepository companyTradeRepository,
            CompanyUserRoleRepository companyUserRoleRepository
    ) {
        this.userService = userService;
        this.companyService = companyService;
        this.companyTradeRepository = companyTradeRepository;
        this.companyUserRoleRepository = companyUserRoleRepository;
    }

    @Test
    public void testSignup() {
        SignupDTO signupDTO = createTestSignupDTO();

        SignupResponseDTO signupResponseDTO = given()
                .spec(defaultSpec())
                .contentType(ContentType.JSON)
                .body(signupDTO)
                .post(PATH)
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.OK.value()))
                .extract().as(SignupResponseDTO.class);


        // assert user
        assertNotNull(signupResponseDTO.getCompanyId(), "company should be created");
        testUser = userService.findById(signupResponseDTO.getUserId());
        assertEquals(signupDTO.getUser().getTitle(), testUser.getTitle());
        assertEquals(signupDTO.getUser().getEmail(), testUser.getEmail());
        assertEquals(signupDTO.getUser().getFirstName(), testUser.getGivenName());
        assertEquals(signupDTO.getUser().getLastName(), testUser.getSurName());
        assertEquals(signupDTO.getUser().getGenderId(), testUser.getGender().getId());
        assertEquals(signupDTO.getUser().getLanguage(), testUser.getPrefLang().getId());
        assertEquals("de_DE", testUser.getPrefLang().getId(), "Default Language has to be set");
        assertEquals(1, testUser.getRoles().size());
        assertTrue(testUser.getRoles().stream().anyMatch(r->r.equals("UNVERIFIED")));

        // assert company user role
        List<CompanyUserRole> companyUserRoles = companyUserRoleRepository.findByUser(testUser.getId());
        assertEquals(1, companyUserRoles.size());
        testCompanyUserRole = companyUserRoles.get(0);
        assertEquals(RoleName.COMPANY_OWNER.toString(), testCompanyUserRole.getRole().getName());

        // assert company
        assertNotNull(signupResponseDTO.getUserId(), "user should be created");
        testCompany = companyService.getValidCompany(signupResponseDTO.getCompanyId());
        assertEquals(signupDTO.getCompany().getName(), testCompany.getName());
        assertEquals(signupDTO.getCompany().getCountryId(), testCompany.getCountry().getId());
        assertEquals(signupDTO.getCompany().getNumberOfEmployeesId(), testCompany.getNumberOfEmployees().getId());

        // assert company trades
        testCompanyTrades = companyTradeRepository.findCompanyTradesByCompany(testCompany.getId());
        assertEquals(signupDTO.getCompany().getTradeIds().size(), testCompanyTrades.size());
        Integer tradeId1 = signupDTO.getCompany().getTradeIds().get(0);
        Integer tradeId2 = signupDTO.getCompany().getTradeIds().get(1);
        assertTrue(testCompanyTrades.stream().anyMatch(ct-> ct.getTrade().equals(tradeId1)));
        assertTrue(testCompanyTrades.stream().anyMatch(ct-> ct.getTrade().equals(tradeId2)));
    }

    @Test
    public void testFreshSignupUserCannotLogin() throws Exception {
        SignupDTO signupDTO = createTestSignupDTO();

        SignupResponseDTO signupResponseDTO = given()
                .spec(defaultSpec())
                .contentType(ContentType.JSON)
                .body(signupDTO)
                .post(PATH)
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.OK.value()))
                .extract().as(SignupResponseDTO.class);


        // assign created entities
        testUser = userService.findById(signupResponseDTO.getUserId());
        assertTrue(testUser.hasRole(UserUtils.Role.UNVERIFIED));
        List<CompanyUserRole> companyUserRoles = companyUserRoleRepository.findByUser(testUser.getId());
        testCompanyUserRole = companyUserRoles.get(0);
        testCompany = companyService.getValidCompany(signupResponseDTO.getCompanyId());
        testCompanyTrades = companyTradeRepository.findCompanyTradesByCompany(testCompany.getId());

        mvc.perform(post("/api/rest/auth/login")
                .param("username", signupDTO.getUser().getEmail())
                .param("password", signupDTO.getUser().getPassword())
                .header("contentType", MediaType.APPLICATION_FORM_URLENCODED)
                .header("Origin", ORIGIN))
                .andExpect(status().is(200))
                .andExpect(header().string(SET_COOKIE_HEADER, containsString(cookieService.getCookieName())))
                .andExpect(header().string(CustomCorsFilter.ALLOW_ORIGIN_HEADER, Matchers.equalTo("https://www.siryus.com")))
                .andExpect(header().string(CustomCorsFilter.ALLOW_METHODS_HEADER, Matchers.equalTo("GET, POST, PUT, DELETE, HEAD, OPTIONS")))
                .andExpect(header().string(CustomCorsFilter.ALLOW_CREDENTIALS_HEADER, Matchers.equalTo("true")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.unverified").value(true))
                .andExpect(jsonPath("$.blocked").value(false))
                .andExpect(jsonPath("$.goodUser").value(false));
    }

    @Test
    @Disabled("RE: SI-177")
    public void testSignupWithWrongMail() {

        SignupDTO testSignupDTO = createTestSignupDTO();
        testSignupDTO.getUser().setEmail("abc");

        given()
                .spec(defaultSpec())
                .contentType(ContentType.JSON)
                .body(testSignupDTO)
                .post(PATH)
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void testSignupWithPasswordWithoutUppercase() {

        SignupDTO testSignupDTO = createTestSignupDTO();
        testSignupDTO.getUser().setPassword("siryus2020");

        given()
                .spec(defaultSpec())
                .contentType(ContentType.JSON)
                .body(testSignupDTO)
                .post(PATH)
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void testSignupWithPasswordWithoutLowercase() {

        SignupDTO testSignupDTO = createTestSignupDTO();
        testSignupDTO.getUser().setPassword("SIRYUS2020");

        given()
                .spec(defaultSpec())
                .contentType(ContentType.JSON)
                .body(testSignupDTO)
                .post(PATH)
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void testSignupWithPasswordWithoutNumber() {

        SignupDTO testSignupDTO = createTestSignupDTO();
        testSignupDTO.getUser().setPassword("ABCabcABCabc");

        given()
                .spec(defaultSpec())
                .contentType(ContentType.JSON)
                .body(testSignupDTO)
                .post(PATH)
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void testSignupWithDuplicateMail() {
        SignupDTO signupDTO = createTestSignupDTO();

        SignupResponseDTO signupResponseDTO = given()
                .spec(defaultSpec())
                .contentType(ContentType.JSON)
                .body(signupDTO)
                .post(PATH)
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.OK.value()))
                .extract().as(SignupResponseDTO.class);

        // assign created entities
        testUser = userService.findById(signupResponseDTO.getUserId());
        List<CompanyUserRole> companyUserRoles = companyUserRoleRepository.findByUser(testUser.getId());
        testCompanyUserRole = companyUserRoles.get(0);
        testCompany = companyService.getValidCompany(signupResponseDTO.getCompanyId());
        testCompanyTrades = companyTradeRepository.findCompanyTradesByCompany(testCompany.getId());

        given()
                .spec(defaultSpec())
                .contentType(ContentType.JSON)
                .body(signupDTO)
                .post(PATH)
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.CONFLICT.value()));
    }

    private SignupDTO createTestSignupDTO() {
        String email = "signUpDtoEmail." + signUpDtoGeneration.incrementAndGet() + ".siryus.com";
        String code = defaultSignupToken;

        SignupUserDTO userSignupDTO = SignupUserDTO.builder()
                .email("signUpUserDtoEmail." + signUpDtoGeneration.incrementAndGet() + "@testX123456.com")
                .firstName("firstname")
                .lastName("lastname")
                .genderId(1)
                .language("de_DE")
                .password("1A" + RandomStringUtils.randomAlphabetic(10))
                .title("mr.")
                .build();

        SignupCompanyDTO companySignupDTO = SignupCompanyDTO.builder()
                .countryId(1)
                .name("test company")
                .numberOfEmployeesId(1)
                .tradeIds(Arrays.asList(tradeId("ARCHITECT"), tradeId("PREPARATION")))
                .build();

        return SignupDTO.builder()
                .user(userSignupDTO)
                .company(companySignupDTO)
                .linkCode(code)
                .linkEmailOrPhone(email)
                .acceptConditions(true)
                .build();
    }

    @AfterEach
    public void cleanTestData() {
//        Optional.ofNullable(testCompanyUserRole).ifPresent(companyUserRoleRepository::delete);
//        Optional.ofNullable(testCompanyTrades).ifPresent(cts->cts.forEach(companyTradeRepository::delete));
//        Optional.ofNullable(testUser).ifPresent(userRepository::delete);
//        Optional.ofNullable(testCompany).ifPresent(companyService::delete);
//        testCompanyUserRole = null;
//        testCompanyTrades = null;
//        testUser = null;
//        testCompany = null;
    }


}
