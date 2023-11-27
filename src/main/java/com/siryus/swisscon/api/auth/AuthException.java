package com.siryus.swisscon.api.auth;

import com.siryus.swisscon.api.auth.validator.ValidPassword;
import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;

import java.util.Arrays;

import static com.siryus.swisscon.api.exceptions.LocalizedReason.ParameterValue.pv;

public class AuthException {

    private static int e(int n) {
        return LocalizedResponseStatusException.AUTH_ERROR_CODE + n;
    }

    public static final LocalizedReason NOT_AUTHORIZED = LocalizedReason.like(e(0), "Not authorized");

    public static final LocalizedReason EMAIL_OR_PHONE_NOT_CORRECTLY_FORMATTED = LocalizedReason.like(e(1), "Email or Phone not correctly formatted: `{{emailOrPhone}}`");

    public static final LocalizedReason EMAIL_OR_PHONE_ALREADY_EXIST = LocalizedReason.like(e(2), "Email or Phone already exist: `{{emailOrPhone}}`");

    public static final LocalizedReason TOKEN_IS_NOT_VALID_OR_EXPIRED = LocalizedReason.like(e(3), "Token is not valid or expired: `{{code}}`");

    public static final LocalizedReason SMS_SEND_FAILED = LocalizedReason.like(e(4), "Could not send sms to number: `{{number}}`");

    public static final LocalizedReason TOO_MANY_ATTEMPTS = LocalizedReason.like(e(5), "Too many attempts");

    public static final LocalizedReason USER_NOT_FOUND = LocalizedReason.like(e(6),  "User with id {{userId}}  was not found");

    public static final LocalizedReason USER_WITH_MOBILE_NOT_FOUND = LocalizedReason.like(e(7), "User with mobile not found: `{{mobile}}`");

    public static final LocalizedReason USER_WITH_USERNAME_NOT_FOUND = LocalizedReason.like(e(8), "User with username not found: `{{username}}`");

    public static final LocalizedReason MULTIPLE_USERS_WITH_PHONE_FOUND = LocalizedReason.like(e(9), "Multiple users with phone number found, please provide the number with country code: `{{mobile}}`");

    public static final LocalizedReason ANOTHER_USER_WITH_MOBILE_EXISTS = LocalizedReason.like(e(10), "Another user with mobile exists: `{{mobile}}`");

    public static final LocalizedReason ROLE_FOR_COMPANY_USER_CANNOT_BE_PROJECT_ROLE = LocalizedReason.like(e(11), "Role for company user cannot be project role: `{{roleName}}`");

    public static final LocalizedReason ROLE_WITH_ID_DOES_NOT_EXIST = LocalizedReason.like(e(12), "Role with id {{roleId}} does not exist");

    public static final LocalizedReason COMPANY_MEMBER_DEFAULT_ROLE_NOT_FOUND = LocalizedReason.like(e(13), "There is no company member default role defined");

    public static final LocalizedReason COMPANY_OWNER_DEFAULT_ROLE_NOT_FOUND = LocalizedReason.like(e(14), "There is no company owner default role defined");

    public static final LocalizedReason AUTHENTICATION_FAILURE = LocalizedReason.like(e(15), "Authentication Failure: `{{message}}`");

    public static final LocalizedReason UNAUTHORIZED_REQUEST = LocalizedReason.like(e(16), "Unauthorized Request: `{{message}}`");

    public static final LocalizedReason PASSWORD_IS_INCORRECT = LocalizedReason.like(e(17), "Password is incorrect: `{{password}}`");

    public static final LocalizedReason SIGNED_UP_USER_SHOULD_HAVE_ROLE_UNVERIFIED = LocalizedReason.like(e(18), "Unverified User has no role UNVERIFIED: `{{userId}}`");

    public static final LocalizedReason TOKEN_OR_CLAIM_INVALID = LocalizedReason.like(e(19), "Token or Claim invalid: `{{message}}`");

    public static final LocalizedReason NEW_EMAIL_NOT_SET_ON_USER = LocalizedReason.like(e(20), "New Email not set on user: `{{userId}}`");

    public static final LocalizedReason PASSWORD_DOES_NOT_MEET_MINIMUM_REQUIREMENTS = LocalizedReason.like(e(21), ValidPassword.MESSAGE);

    public static final LocalizedReason ROLE_FOR_COMPANY_CANNOT_BE_UNIQUE_AND_MANDATORY = LocalizedReason.like(e(22), "Role for company user cannot be unique and mandatory: `{{roleName}}`");

    public static final LocalizedReason ROLE_FOR_COMPANY_HAS_TO_BE_UNIQUE_AND_MANDATORY = LocalizedReason.like(e(23), "Role for company user has to be unique and mandatory: `{{roleName}}`");

    public static final LocalizedReason USER_IS_ALREADY_VERIFIED = LocalizedReason.like(e(24), "User is already verified: `{{userId}}`");

    public static final LocalizedReason INVITED_USERS_CANNOT_HAVE_BOOTSTRAP_ROLE = LocalizedReason.like(e(25), "Invited Users cannot have bootstrap role: `{{roleName}}`");

    public static LocalizedResponseStatusException emailOrPhoneNotCorrectlyFormatted(String emailOrPhone, Integer countryCode) {
        String countryCodeStr = countryCode != null ? countryCode.toString(): "";
        String errorValue = String.join(",", Arrays.asList(emailOrPhone,countryCodeStr));
        return LocalizedResponseStatusException.badRequest(EMAIL_OR_PHONE_NOT_CORRECTLY_FORMATTED.with(pv("emailOrPhone", errorValue)));
    }

    public static LocalizedResponseStatusException emailOrPhoneAlreadyExist(String emailOrPhone) {
        return LocalizedResponseStatusException.businessLogicError(EMAIL_OR_PHONE_ALREADY_EXIST.with(pv("emailOrPhone", emailOrPhone)));
    }

    public static LocalizedResponseStatusException tokenIsNotValidOrExpired(String code) {
        return LocalizedResponseStatusException.badRequest(TOKEN_IS_NOT_VALID_OR_EXPIRED.with(pv("code", code)));
    }

    public static LocalizedResponseStatusException smsSendFailed(String phoneNumber) {
        return LocalizedResponseStatusException.badRequest(SMS_SEND_FAILED.with(pv("number", phoneNumber)));
    }
    
    public static LocalizedResponseStatusException tooManyAttempts() {
        return LocalizedResponseStatusException.badRequest(TOO_MANY_ATTEMPTS.with());
    }

    public static LocalizedResponseStatusException userNotFound(Integer userId) {
        return  LocalizedResponseStatusException.notFound(USER_NOT_FOUND.with(pv("userId", userId)));
    }

    public static LocalizedResponseStatusException userWithMobileNotFound(String mobile) {
        return  LocalizedResponseStatusException.notFound(USER_WITH_MOBILE_NOT_FOUND.with(pv("mobile", mobile)));
    }

    public static LocalizedResponseStatusException userWithUsernameNotFound(String username) {
        return  LocalizedResponseStatusException.notFound(USER_WITH_USERNAME_NOT_FOUND.with(pv("username", username)));
    }

    public static LocalizedResponseStatusException multipleUsersWithMobileFound(String mobile) {
        return  LocalizedResponseStatusException.badRequest(MULTIPLE_USERS_WITH_PHONE_FOUND.with(pv("mobile", mobile)));
    }

    public static LocalizedResponseStatusException anotherUserWithMobileExists(String mobile) {
        return  LocalizedResponseStatusException.badRequest(ANOTHER_USER_WITH_MOBILE_EXISTS.with(pv("mobile", mobile)));
    }

    public static LocalizedResponseStatusException roleForCompanyUserCannotBeProjectRole(String roleName) {
        return  LocalizedResponseStatusException.badRequest(ROLE_FOR_COMPANY_USER_CANNOT_BE_PROJECT_ROLE.with(pv("roleName", roleName)));
    }

    public static LocalizedResponseStatusException roleDoesNotExist(Integer roleId) {
        return LocalizedResponseStatusException.notFound(ROLE_WITH_ID_DOES_NOT_EXIST.with(pv("roleId", roleId)));
    }

    public static LocalizedResponseStatusException companyMemberDefaultRoleNotFound() {
        return  LocalizedResponseStatusException.internalError(COMPANY_MEMBER_DEFAULT_ROLE_NOT_FOUND.with());
    }

    public static LocalizedResponseStatusException companyOwnerDefaultRoleNotFound() {
        return  LocalizedResponseStatusException.internalError(COMPANY_OWNER_DEFAULT_ROLE_NOT_FOUND.with());
    }

    public static LocalizedResponseStatusException authenticationFailure(String message) {
        return LocalizedResponseStatusException.unAuthorized(AUTHENTICATION_FAILURE.with(pv("message", message)));
    }

    public static LocalizedResponseStatusException unauthorizedRequest(String message) {
        return LocalizedResponseStatusException.unAuthorized(UNAUTHORIZED_REQUEST.with(pv("message", message)));
    }

    public static LocalizedResponseStatusException passwordIsIncorrect() {
        return LocalizedResponseStatusException.badRequest(PASSWORD_IS_INCORRECT.with());
    }

    public static LocalizedResponseStatusException signedUpUserShouldHaveRoleUnverified(Integer userId) {
        return LocalizedResponseStatusException.internalError(SIGNED_UP_USER_SHOULD_HAVE_ROLE_UNVERIFIED.with(pv("userId", userId)));
    }

    public static LocalizedResponseStatusException tokenOrClaimInvalid(String message) {
        return LocalizedResponseStatusException.badRequest(TOKEN_OR_CLAIM_INVALID.with(pv("message", message)));
    }

    public static LocalizedResponseStatusException newEmailNotSetOnUser(Integer userId) {
        return LocalizedResponseStatusException.internalError(NEW_EMAIL_NOT_SET_ON_USER.with(pv("userId", userId)));
    }

    public static LocalizedResponseStatusException passwordDoesNotMeetMinimumRequirements() {
        return LocalizedResponseStatusException.internalError(PASSWORD_DOES_NOT_MEET_MINIMUM_REQUIREMENTS.with());
    }

    public static LocalizedResponseStatusException roleForCompanyCannotBeUniqueAndMandatory(String roleName) {
        return  LocalizedResponseStatusException.badRequest(ROLE_FOR_COMPANY_CANNOT_BE_UNIQUE_AND_MANDATORY.with(pv("roleName", roleName)));
    }

    public static LocalizedResponseStatusException roleForCompanyHasToBeUniqueAndMandatory(String roleName) {
        return  LocalizedResponseStatusException.badRequest(ROLE_FOR_COMPANY_HAS_TO_BE_UNIQUE_AND_MANDATORY.with(pv("roleName", roleName)));
    }

    public static LocalizedResponseStatusException userIsAlreadyVerified(Integer userId) {
        return LocalizedResponseStatusException.badRequest(USER_IS_ALREADY_VERIFIED.with(pv("userId", userId)));
    }

    public static LocalizedResponseStatusException invitedUsersCannotHaveBootstrapRole(String roleName) {
        return  LocalizedResponseStatusException.badRequest(INVITED_USERS_CANNOT_HAVE_BOOTSTRAP_ROLE.with(pv("roleName", roleName)));
    }

}
