package com.siryus.swisscon.api.auth.user;

import com.naturalprogrammer.spring.lemon.commons.security.UserDto;
import com.siryus.swisscon.api.auth.AuthException;
import com.siryus.swisscon.api.auth.LemonTemplateUtil;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.general.langcode.LangCode;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;

import java.util.Objects;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class UserChangeMailIT extends AbstractMvcTestBase {

    @SpyBean
    private LemonTemplateUtil lemonTemplateUtil;

    private final UserRepository userRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final CompanyRepository companyRepository;

    private static String password;
    private static String oldMail;
    private static String newMail;

    private static User testUser;
    private static Company testCompany;
    private static CompanyUserRole testCompanyUserRole;

    private final ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

    @Autowired
    public UserChangeMailIT(UserRepository userRepository, CompanyUserRoleRepository companyUserRoleRepository, CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.companyRepository = companyRepository;
    }

    @Test
    public void Given_correctPasswordAndNewEmail_When_changeEmail_Then_success() {
        extractChangeEmailLinkByMocking();
        RequestSpecification loginSpec = loginSpec(testUser.getEmail(), password);

        // prepare method body
        User changeEmailBody = new User();
        changeEmailBody.setPassword(password);
        changeEmailBody.setNewEmail(newMail);

        // request to change the email
        given()
                .spec(loginSpec)
                .contentType(ContentType.JSON)
                .body(changeEmailBody)
                .post(BASE_PATH + "/auth/users/" + testUser.getId() + "/email-change-request")
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.NO_CONTENT.value()));

        // check that the "newMail" ist set, but the other properties are still old ones
        String testChangeEmailLink = tokenCaptor.getValue();
        Optional<User> testUserOpt = userRepository.findById(Objects.requireNonNull(testUser.getId()));
        assert testUserOpt.isPresent();
        assertNotNull(testUserOpt.get().getUsername());
        assertEquals(oldMail, testUserOpt.get().getEmail());
        assertEquals(newMail, testUserOpt.get().getNewEmail());

        // get the verification link trough mocking
        assertNotNull(testChangeEmailLink, "The change email link should have been extracted through a mock bean");
        String changeEmailCode = StringUtils.substringAfterLast(testChangeEmailLink, "/");
        assertFalse(StringUtils.isBlank(changeEmailCode), "the change email link should contain a query parameter named code");

        // change the email definitely after "clicking" the "change-email-link"
        // important here: the user is not logged in!
        UserDto userDTO = given()
                .contentType(ContentType.JSON)
                .queryParam("code", changeEmailCode)
                .post(BASE_PATH + "/auth/users/" + testUser.getId() + "/email")
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.OK.value()))
                .extract()
                .as(UserDto.class);

        // check that the "newMail" ist reset to null and the the email has the new value
        assertEquals(newMail, userDTO.getUsername());
        Optional<User> testUserOpt2 = userRepository.findById(Objects.requireNonNull(testUser.getId()));
        assert testUserOpt2.isPresent();
        assertNotNull(testUserOpt2.get().getUsername());
        assertEquals(newMail, testUserOpt2.get().getEmail());
        assertNull(testUserOpt2.get().getNewEmail());
    }

    @Test
    public void Given_duplicateEmail_When_changeEmail_Then_throw() {
        RequestSpecification loginSpec = loginSpec(testUser.getEmail(), password);
        Optional<User> user1Opt = userRepository.findById(1);
        assert user1Opt.isPresent();

        // prepare method body
        User changeEmailBody = new User();
        changeEmailBody.setPassword(password);
        changeEmailBody.setNewEmail(user1Opt.get().getEmail());

        // request to change the email will fail because we already have this user email in the DB
        given()
                .spec(loginSpec)
                .contentType(ContentType.JSON)
                .body(changeEmailBody)
                .post(BASE_PATH + "/auth/users/" + testUser.getId() + "/email-change-request")
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void Given_wrongPassword_When_changeEmail_Then_throw() {
        RequestSpecification loginSpec = loginSpec(testUser.getEmail(), password);

        // prepare method body
        User changeEmailBody = new User();
        changeEmailBody.setPassword("wrong_password_A1");
        changeEmailBody.setNewEmail("new_mail@test.com");

        // request to change the email will fail because we already have this user email in the DB
        ValidatableResponse response = given()
                .spec(loginSpec)
                .contentType(ContentType.JSON)
                .body(changeEmailBody)
                .post(BASE_PATH + "/auth/users/" + testUser.getId() + "/email-change-request")
                .then();
        TestAssert.assertError(HttpStatus.BAD_REQUEST, AuthException.PASSWORD_IS_INCORRECT.getErrorCode(), response);
    }

    @BeforeEach()
    public void initTestData() {
        oldMail = "oldMail@" + RandomStringUtils.randomAlphanumeric(5) + ".com";
        newMail = "newMail@" + RandomStringUtils.randomAlphanumeric(5) + ".com";
        password = RandomStringUtils.randomAlphanumeric(10);

        createTestUser();
    }

    private void extractChangeEmailLinkByMocking() {
        Mockito.doReturn("html_template")
                .when(lemonTemplateUtil)
                .getChangeEmailMailContent(any(), tokenCaptor.capture());
    }

    private void createTestUser() {
        User user = User.builder().build();
        user.setPassword("{noop}" + password);
        user.setEmail(oldMail);
        user.setPrefLang(LangCode.builder().id("en_US").build());
        testUser = userRepository.save(user);
        testCompany = companyRepository.save(Company.builder().name("change email test company").build());
        testCompanyUserRole = companyUserRoleService.addUserToCompanyTeam(testUser, testCompany, RoleName.COMPANY_WORKER.name());
    }

    @AfterEach()
    public void cleanTestData() {
        Optional.ofNullable(testCompanyUserRole).ifPresent(e -> deleteIfExists(companyUserRoleRepository, e.getId()));
        Optional.ofNullable(testCompany).ifPresent(e -> deleteIfExists(companyRepository, e.getId()));
        Optional.ofNullable(testUser).ifPresent(e -> deleteIfExists(userRepository, e.getId()));
        testUser = null;
        oldMail = null;
        newMail = null;
    }


}
