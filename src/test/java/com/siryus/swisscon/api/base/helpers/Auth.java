package com.siryus.swisscon.api.base.helpers;

import com.naturalprogrammer.spring.lemon.commons.domain.ResetPasswordForm;
import com.naturalprogrammer.spring.lemon.commons.security.GreenTokenService;
import com.siryus.swisscon.api.auth.LemonService;
import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.auth.signup.SignupDTO;
import com.siryus.swisscon.api.auth.signup.SignupResponseDTO;
import com.siryus.swisscon.api.auth.sms.ExtendedTokenService;
import com.siryus.swisscon.api.auth.sms.MobileDTO;
import com.siryus.swisscon.api.auth.user.TeamUserAddDTO;
import com.siryus.swisscon.api.auth.user.TeamUserDTO;
import com.siryus.swisscon.api.auth.user.UserAccountDTO;
import com.siryus.swisscon.api.auth.user.UserProfileDTO;
import com.siryus.swisscon.api.auth.usertoken.UserTokenType;
import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.customroles.CustomRoleReader;
import com.siryus.swisscon.api.init.InitDTO;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.siryus.swisscon.api.base.AbstractMvcTestBase.endPoint;
import static com.siryus.swisscon.api.base.TestHelper.COMPANY_INVITATION_MAIL;
import static com.siryus.swisscon.api.base.TestHelper.PASSWORD;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class Auth {
    private final AbstractMvcTestBase testBase;
    private final LemonService lemonService;
    private final GreenTokenService greenTokenService;
    private final ExtendedTokenService extendedTokenService;
    private final String defaultSignUpToken;
    private final CustomRoleReader roleReader;

    public Auth(
            AbstractMvcTestBase testBase,
            LemonService lemonService,
            GreenTokenService greenTokenService,
            ExtendedTokenService extendedTokenService,
            String defaultSignUpToken,
            CustomRoleReader roleReader
    ) {
        this.testBase = testBase;
        this.lemonService = lemonService;
        // can't get rid of this for now because of verifyUser() which uses AbstractLemonService.mailVerificationLink(...); needs to be overriden
        this.greenTokenService = greenTokenService; 
        this.extendedTokenService = extendedTokenService;
        this.defaultSignUpToken = defaultSignUpToken;
        this.roleReader = roleReader;
    }

    @Deprecated //TODO remove after SI-177
    public void signUp(SignupDTO request, HttpStatus status) {
        given()
                .spec(testBase.defaultSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .post(endPoint("/auth/signup"))
                .then()
                .assertThat()
                .statusCode(equalTo(status.value()));
    }

    @Deprecated //TODO remove after SI-177
    public SignupResponseDTO signUpWithRole(SignupDTO request, RoleName roleName) {
        var roleId = roleReader.getRoleByName(roleName.name()).getId();

        return signUp(
                request.toBuilder()
                .linkCode(extendedTokenService
                  .getAndIssueCompanyInvitationToken(0, request.getLinkEmailOrPhone(), roleId, 100000L)
                )
                .build()
        );
    }

    @Deprecated //TODO remove after SI-177
    public SignupResponseDTO signUp(SignupDTO request) {
        if(request.getLinkEmailOrPhone() == null) {
            request.setLinkEmailOrPhone(COMPANY_INVITATION_MAIL);
        }

        if(request.getLinkCode() == null) {
            request.setLinkCode(defaultSignUpToken);
        }

        SignupResponseDTO signupResponseDTO = given()
                .spec(testBase.defaultSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .post(endPoint("/auth/signup"))
                .then()
                .assertThat()
                .statusCode(equalTo(HttpStatus.OK.value()))
                .extract().as(SignupResponseDTO.class);

        verifyUser(signupResponseDTO.getUserId(), request.getUser().getEmail());

        return signupResponseDTO;
    }

    public SignupResponseDTO signupFromInvite(SignupDTO request) {
        request.setLinkCode(request.getLinkCode() != null ? request.getLinkCode(): defaultSignUpToken);
        return signupFromInvite(request, r-> r.assertThat().statusCode(equalTo(HttpStatus.OK.value()))
                .extract().as(SignupResponseDTO.class));
    }

    public SignupResponseDTO signupFromInvite(SignupDTO request,  Function<ValidatableResponse, SignupResponseDTO> responseValidator) {
        return responseValidator.apply(given()
                .spec(testBase.defaultSpec())
                .body(request)
                .post(endPoint("/auth/signup-from-invite"))
                .then()
        );
    }

    public UserProfileDTO updateUserProfile(RequestSpecification spec, UserProfileDTO request) {
        return updateUserProfile(spec, request,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(UserProfileDTO.class)
        );
    }

    public UserProfileDTO updateUserProfile(RequestSpecification spec, UserProfileDTO request, Function<ValidatableResponse, UserProfileDTO> responseValidator) {
        return responseValidator.apply(given()
                .spec(spec)
                .body(request)
                .post(endPoint("/auth/users/profile"))
                .then()
        );
    }

    public InitDTO init(RequestSpecification spec) {
        return init(spec,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(InitDTO.class)
        );
    }

    public InitDTO init(RequestSpecification spec, Function<ValidatableResponse, InitDTO> responseValidator) {
        return responseValidator.apply(given()
                .spec(spec)
                .get(endPoint("/auth/init"))
                .then()
        );
    }

    public void verifyUser(Integer userId, String email) {
        Map<String, Object> options = new HashMap<>();
        options.put("email", email);
        String verificationCode = greenTokenService.createToken(
                GreenTokenService.VERIFY_AUDIENCE,
                userId.toString(), 10000000L,
                options
        );

        lemonService.verifyUser(userId, verificationCode);
    }

    public RequestSpecification login(String email) {
        return login(email, PASSWORD);
    }

    public RequestSpecification login(String email, String password) {
        return testBase.loginSpec(email, password);
    }

    public void login(String username, String password, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(given().urlEncodingEnabled(true)
                .param("username", username)
                .param("password", password)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .post(endPoint("/auth/login"))
                .then());
    }

    public void logout(RequestSpecification spec) {
        logout(spec, r-> r.assertThat().statusCode(HttpStatus.OK.value()));
    }

    public void logout(RequestSpecification spec, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(given().spec(spec)
                .post(endPoint("/auth/logout"))
                .then());
    }

    public void resetPassword(RequestSpecification spec, ResetPasswordForm resetPasswordForm) {
        resetPassword(spec, resetPasswordForm,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
        );
    }

    public void resetPassword(RequestSpecification spec, ResetPasswordForm resetPasswordForm, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(given()
                .spec(spec)
                .body(resetPasswordForm)
                .post(endPoint("/auth/reset-password"))
                .then()
        );
    }

    public void resetPasswordWithOwnToken(Integer userId) {
        resetPasswordWithOwnToken(userId, PASSWORD);
    }
    public void resetPasswordWithOwnToken(Integer userId, String newPassword) {
        String verificationCode = extendedTokenService.issueToken(userId, UserTokenType.FORGOT_PASSWORD);

        ResetPasswordForm resetPasswordForm = new ResetPasswordForm();
        resetPasswordForm.setCode( verificationCode);
        resetPasswordForm.setNewPassword(newPassword);
        lemonService.resetPassword(resetPasswordForm);
    }

    public TeamUserDTO inviteUser(RequestSpecification spec, TeamUserAddDTO request) {
        return inviteUser(spec, request,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
                        .extract()
                        .as(TeamUserDTO.class)
        );
    }

    public TeamUserDTO inviteUser(RequestSpecification spec,TeamUserAddDTO request, Function<ValidatableResponse, TeamUserDTO> responseValidator) {
        return responseValidator.apply(given()
                .spec(spec)
                .body(request)
                .post(endPoint("/auth/invite"))
                .then()
        );
    }

    public void resendInvitation(RequestSpecification spec, Integer userId) {
        resendInvitation(spec, userId,
                r -> r.assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.OK.value()))
        );
    }

    public void resendInvitation(RequestSpecification spec,Integer userId, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(given()
                .spec(spec)
                .pathParam("id", userId)
                .post(endPoint("/auth/users/{id}/resend-invitation"))
                .then()
        );
    }

    public TeamUserDTO inviteUserAndResetPassword(RequestSpecification spec, TeamUserAddDTO request) {
        TeamUserDTO response = inviteUser(spec, request);

        // TODO: As of now, we auto-verify all invites. It should stop at some point.
        // verifyUser(response.getId(), request.getEmail());

        resetPasswordWithOwnToken(response.getId());

        return response;
    }

    public TeamUserDTO toggleAdmin(RequestSpecification spec, Integer userId, boolean toggleAdmin) {
        return toggleAdmin(spec, userId, toggleAdmin, r -> r.assertThat().statusCode(HttpStatus.OK.value()).extract().as(TeamUserDTO.class));
    }
    public TeamUserDTO toggleAdmin(RequestSpecification spec, Integer userId, boolean toggleAdmin, Function<ValidatableResponse, TeamUserDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(spec)
                        .queryParam("make-admin", toggleAdmin)
                        .pathParam("id", userId)
                        .post(endPoint("/auth/users/{id}/toggle-admin"))
                        .then()
        );
    }

    public void verifySignupCode(String email, String code, HttpStatus status) {
        given()
                .queryParam("emailOrPhone", email)
                .queryParam("code", code)
                .post(endPoint("/auth/verify-signup-code"))
                .then()
                .assertThat()
                .statusCode(equalTo(status.value()));
    }
    
    public void sendForgotPasswordSms(String mobile, HttpStatus status) {
        given()
            .spec(testBase.defaultSpec())
            .contentType(ContentType.JSON)
            .body(MobileDTO.builder().mobile(mobile).build())
            .post(endPoint("/auth/sms/send-forgot-password"))
            .then()
            .assertThat()
            .statusCode(equalTo(status.value()));
    }

    public SignupDTO verifySignup(Integer companyId, String code) {
        return verifySignup(companyId, code,
                r -> r.assertThat().statusCode(HttpStatus.OK.value())
                        .extract().as(SignupDTO.class));
    }

    public SignupDTO verifySignup(Integer companyId, String code, Function<ValidatableResponse, SignupDTO> responseValidator) {
        return responseValidator.apply(
                given()
                        .spec(testBase.defaultSpec())
                        .contentType(ContentType.JSON)
                        .queryParam("company", companyId)
                        .queryParam("code", code)
                        .post(endPoint("/auth/verify-signup"))
                        .then()
        );
    }

    public void sendChangeMobileSms(RequestSpecification spec, Integer mobileCountryCode, String mobile) {
        sendChangeMobileSms(spec, mobileCountryCode, mobile, r -> r.assertThat().statusCode(HttpStatus.OK.value()) );
    }    
    
    public void sendChangeMobileSms(RequestSpecification spec, Integer mobileCountryCode, String mobile, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(
            given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .body(MobileDTO.builder().mobile(mobile).countryCode(mobileCountryCode).build())
                .post(endPoint("/auth/sms/send-change-phone"))
                .then()                
        );    
    }   
    
    public void updateMobile(RequestSpecification spec, String token) {
        updateMobile(spec, token, r -> r.assertThat().statusCode(HttpStatus.OK.value()) );
    }    
    
    public void updateMobile(RequestSpecification spec, String token, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(
            given()
                .spec(spec)
                .queryParam("confirmationCode", token)
                .post(endPoint("/auth/users/phone"))
                .then()
        );
    }    

    public void changeLanguage(RequestSpecification spec, String languageId) {
        changeLanguage(spec, languageId, r -> r.assertThat().statusCode(HttpStatus.OK.value()));
    }

    public void changeLanguage(RequestSpecification spec, String languageId, Consumer<ValidatableResponse> responseValidator) {
        responseValidator.accept(
            given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .body(UserAccountDTO.builder().languageId(languageId).build())
                .post(endPoint("/auth/users/account"))
                .then()
        );
    }
}
