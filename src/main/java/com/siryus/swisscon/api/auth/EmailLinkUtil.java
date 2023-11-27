package com.siryus.swisscon.api.auth;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailLinkUtil {

    @Value("${lemon.confirmation-link}")
    private String confirmationLink;

    @Value("${lemon.invitation-link}")
    private String invitationLink;

    @Deprecated //TODO remove after SI-177
    @Value("${lemon.signup-link.deprecated}")
    private String signupLinkDepcrecated;

    @Value("${lemon.signup-link}")
    private String signupLink;

    @Value("${lemon.reset-link}")
    private String resetLink;

    @Value("${lemon.change-email-link}")
    private String changeEmailLink;

    private static final String CODE = "{code}";
    private static final String USER_ID = "{userId}";
    private static final String EMAIL = "{email}";
    private static final String COMPANY_ID = "{companyId}";

    private static final String CODE_QUERY_PARAM="code=";

    private LemonProperties properties;

    @Autowired
    public EmailLinkUtil(LemonProperties properties) {
        this.properties = properties;
    }

    public String getConfirmationLink(String verifyLink, Integer userId) {
        String code = StringUtils.substringAfterLast(verifyLink, CODE_QUERY_PARAM);
        String extendedConfirmationLink = confirmationLink.replace(CODE, code).replace(USER_ID, String.valueOf(userId));
        return properties.getApplicationUrl() + extendedConfirmationLink;
    }

    public String getSignupLink(Integer companyId, String code) {
        String extendedSignupLink = signupLink.replace(COMPANY_ID, String.valueOf(companyId)).replace(CODE, code);
        return properties.getApplicationUrl() + extendedSignupLink;
    }

    @Deprecated //TODO remove after SI-177
    public String getSignupLinkDeprecated(String emailOrPhone, String code) {
        String extendedSignupLink = signupLinkDepcrecated.replace(EMAIL, emailOrPhone).replace(CODE, code);
        return properties.getApplicationUrl() + extendedSignupLink;
    }

    public String getInvitationLink(String code, Integer userId) {
        String extendedConfirmationLink = invitationLink.replace(CODE, code).replace(USER_ID, String.valueOf(userId));
        return properties.getApplicationUrl() + extendedConfirmationLink;
    }

    public String getResetLink(String forgotPasswordLink, Integer userId) {
        String code = StringUtils.substringAfterLast(forgotPasswordLink, CODE_QUERY_PARAM);
        String extendedConfirmationLink = resetLink.replace(CODE, code).replace(USER_ID, String.valueOf(userId));
        return properties.getApplicationUrl() + extendedConfirmationLink;
    }

    public String getEmailChangeLink(String verifyLink, Integer userId) {
        String code = StringUtils.substringAfterLast(verifyLink, CODE_QUERY_PARAM);
        String extendedChangeEmailLink = changeEmailLink.replace(CODE, code).replace(USER_ID, String.valueOf(userId));
        return properties.getApplicationUrl() + extendedChangeEmailLink;
    }

}
