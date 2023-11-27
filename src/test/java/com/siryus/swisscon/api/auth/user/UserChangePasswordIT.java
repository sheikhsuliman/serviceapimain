package com.siryus.swisscon.api.auth.user;

import com.naturalprogrammer.spring.lemon.commons.domain.ChangePasswordForm;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.base.TestAssert;
import com.siryus.swisscon.api.company.company.Company;
import com.siryus.swisscon.api.company.company.CompanyRepository;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRoleRepository;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import com.siryus.swisscon.api.general.langcode.LangCode;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserChangePasswordIT extends AbstractMvcTestBase {

    private static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CompanyUserRoleRepository companyUserRoleRepository;

    private static String email;
    private static String oldPassword;
    private static String newPassword;

    private static User testUser;
    private static Company testCompany;
    private static CompanyUserRole testCompanyUserRole;

    @Autowired
    public UserChangePasswordIT(UserRepository userRepository, CompanyRepository companyRepository, CompanyUserRoleRepository companyUserRoleRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.companyUserRoleRepository = companyUserRoleRepository;
    }

    @Test
    public void testChangeUserEmail() {
        RequestSpecification loginSpec = loginSpec(testUser.getEmail(), oldPassword);

        // prepare method body
        ChangePasswordForm changePasswordForm = new ChangePasswordForm();
        changePasswordForm.setOldPassword(oldPassword);
        changePasswordForm.setPassword(newPassword);
        changePasswordForm.setRetypePassword(newPassword);

        // request to change the password
        executeChangePassword(loginSpec, changePasswordForm)
                .statusCode(equalTo(HttpStatus.NO_CONTENT.value()));

        // check if password is changed
        Optional<User> testUserOpt = userRepository.findById(Objects.requireNonNull(testUser.getId()));
        assert testUserOpt.isPresent();
        assertTrue(PASSWORD_ENCODER.matches(newPassword, testUserOpt.get().getPassword()));

    }

    @Test
    public void testChangeUserPasswordWithWrongOldPassword() {
        RequestSpecification loginSpec = loginSpec(testUser.getEmail(), oldPassword);

        // prepare method body
        ChangePasswordForm changePasswordForm = new ChangePasswordForm();
        changePasswordForm.setOldPassword(oldPassword + "THIS IS NOT CORRECT");
        changePasswordForm.setPassword(newPassword);
        changePasswordForm.setRetypePassword(newPassword);

        // request to change the password
        executeChangePassword(loginSpec, changePasswordForm)
                .statusCode(equalTo(HttpStatus.BAD_REQUEST.value()));

        // check password is not changed
        Optional<User> testUserOpt = userRepository.findById(Objects.requireNonNull(testUser.getId()));
        assert testUserOpt.isPresent();
        assertTrue(PASSWORD_ENCODER.matches(oldPassword, testUserOpt.get().getPassword()));
    }

    @Test
    public void testChangeUserEmailWithWrongRetype() {
        RequestSpecification loginSpec = loginSpec(testUser.getEmail(), oldPassword);

        // prepare method body with
        ChangePasswordForm changePasswordForm = new ChangePasswordForm();
        changePasswordForm.setOldPassword(oldPassword);
        changePasswordForm.setPassword(newPassword);
        changePasswordForm.setRetypePassword("THIS IS NOT CORRECT");

        // request to change the password
        ValidatableResponse validatableResponse = executeChangePassword(loginSpec, changePasswordForm);
        TestAssert.assertError(HttpStatus.BAD_REQUEST,
                LocalizedResponseStatusException.VALIDATION_ERROR_CODE,
                validatableResponse);

        // check password is not changed
        Optional<User> testUserOpt = userRepository.findById(Objects.requireNonNull(testUser.getId()));
        assert testUserOpt.isPresent();
        assertTrue(PASSWORD_ENCODER.matches(oldPassword, testUserOpt.get().getPassword()));
    }

    private ValidatableResponse executeChangePassword(RequestSpecification loginSpec, ChangePasswordForm changePasswordForm) {
        return given()
                .spec(loginSpec)
                .contentType(ContentType.JSON)
                .body(changePasswordForm)
                .post(BASE_PATH + "/auth/users/" + testUser.getId() + "/password")
                .then()
                .assertThat();
    }

    @BeforeEach()
    public void initTestData() {
        oldPassword = "1AoldPassword";
        newPassword = "1AnewPassword";
        email = "test@random.net";

        createTestUser();
    }

    private void createTestUser() {
        User user = User.builder().build();
        user.setPassword(PASSWORD_ENCODER.encode(oldPassword));
        user.setEmail(email);
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
        email = null;
        oldPassword = null;
        newPassword = null;
    }


}
