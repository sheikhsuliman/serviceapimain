package com.siryus.swisscon.api.auth;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.siryus.swisscon.api.general.langcode.LangCode;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.util.TemplateUtil;
import com.siryus.swisscon.api.util.TranslationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LemonTemplateUtil {

    private final LemonProperties properties;
    private final TranslationUtil translationUtil;
    private final TemplateUtil templateUtil;

    private static final String CONFIRMATION_EMAIL_FILE = "template/confirmationEmail.html";
    private static final String RESET_PASSWORD_EMAIL_FILE = "template/resetPasswordEmail.html";
    private static final String INVITATION_EMAIL_FILE = "template/invitationEmail.html";
    private static final String INVITATION_SMS_FILE = "template/invitationSms.txt";
    private static final String WELCOME_EMAIL_FILE = "template/welcomeEmail.html";
    private static final String CHANGE_EMAIL_FILE = "template/changeEmail.html";
    private static final String INVITATION_COMPANY_EMAIL_FILE = "template/inviteCompanyEmail.html";
    private static final String INVITATION_COMPANY_SMS_FILE = "template/inviteCompanySms.txt";
    private static final String SMS_RESET_PASSWORD_FILE = "template/smsForgetPassword.txt";
    private static final String SMS_CHANGE_MOBILE_NAME = "sms.change_mobile.text";      
    private static final String TOKEN = "{tokenCode}";
    
    private static final String VERIFICATION_LINK = "{verificationLink}";
    private static final String USERNAME = "{username}";
    private static final String GIVEN_NAME = "{givenName}";
    private static final String FULL_NAME = "{fullName}";
    private static final String COMPANY_NAME = "{companyName}";
    private static final String RESET_PASSWORD_LINK = "{resetPasswordLink}";
    private static final String LOGIN_URL = "{loginUrl}";
    private static final String CHANGE_EMAIL_LINK = "{changeEmailLink}";
    private static final String SIGNUP_LINK = "{signupLink}";

    @Autowired
    public LemonTemplateUtil(LemonProperties properties, TranslationUtil translationUtil, TemplateUtil templateUtil) {
        this.properties = properties;
        this.translationUtil = translationUtil;
        this.templateUtil = templateUtil;
    }

    public String getChangeMobileSmsContent(String rawToken, User user) {
        return translationUtil.get(SMS_CHANGE_MOBILE_NAME, user.getPrefLang().getId())
                .replace(TOKEN, rawToken);
    }       
    
    public String getForgetPasswordSmsContent(String rawToken, User user) {
        return templateUtil.loadTranslatedTemplate(SMS_RESET_PASSWORD_FILE, user.getPrefLang().getId())
                .replace(TOKEN, rawToken);
    }    
    
    public String getConfirmationMailContent(String confirmationLink, User user) {
        return templateUtil.loadTranslatedTemplate(CONFIRMATION_EMAIL_FILE, user.getPrefLang().getId())
                .replace(VERIFICATION_LINK, confirmationLink)
                .replace(USERNAME, user.getGivenName() + " " + user.getSurName());
    }

    public String getInvitationMailContent(String invitationLink, String invitingUsername, User invitedUser, String companyName, LangCode lang) {
        return templateUtil.loadTranslatedTemplate(INVITATION_EMAIL_FILE, lang.getId())
                .replace(VERIFICATION_LINK, invitationLink)
                .replace(USERNAME, invitedUser.getGivenName() + " " + invitedUser.getSurName())
                .replace(GIVEN_NAME, invitingUsername)
                .replace(COMPANY_NAME, companyName);
    }

    public String getInvitationSmsContent(String invitationLink, User invitedUser, String companyName, LangCode lang) {
        return templateUtil.loadTranslatedTemplate(INVITATION_SMS_FILE, lang.getId())
                .replace(VERIFICATION_LINK, invitationLink)
                .replace(USERNAME, invitedUser.getGivenName() + " " + invitedUser.getSurName())
                .replace(COMPANY_NAME, companyName);
    }

    public String getCompanyInvitationMailContent(User invitingUser, String signupLink, LangCode lang) {
        return templateUtil.loadTranslatedTemplate(INVITATION_COMPANY_EMAIL_FILE, lang.getId())
                .replace(FULL_NAME, invitingUser.getGivenName() + " " + invitingUser.getSurName())
                .replace(SIGNUP_LINK, signupLink);
    }

    public String getCompanyInvitationSmsContent(User invitingUser, String signupLink, LangCode lang) {
        return templateUtil.loadTranslatedTemplate(INVITATION_COMPANY_SMS_FILE, lang.getId())
                .replace(FULL_NAME, invitingUser.getGivenName() + " " + invitingUser.getSurName())
                .replace(SIGNUP_LINK, signupLink);
    }

    public String getResetMailContent(String resetPasswordLink, User user) {
        return templateUtil.loadTranslatedTemplate(RESET_PASSWORD_EMAIL_FILE, user.getPrefLang().getId())
                .replace(RESET_PASSWORD_LINK, resetPasswordLink)
                .replace(USERNAME, user.getGivenName() + " " + user.getSurName());
    }

    public String getWelcomeMailContent(User user, String companyName) {
        return templateUtil.loadTranslatedTemplate(WELCOME_EMAIL_FILE, user.getPrefLang().getId())
                .replace(FULL_NAME, user.getGivenName() + " " + user.getSurName())
                .replace(USERNAME, user.getEmail())
                .replace(COMPANY_NAME, companyName)
                .replace(LOGIN_URL, properties.getApplicationUrl());
    }

    public String getChangeEmailMailContent(User user, String changeEmailLink) {
        return templateUtil.loadTranslatedTemplate(CHANGE_EMAIL_FILE, user.getPrefLang().getId())
                .replace(USERNAME, user.getGivenName() + " " + user.getSurName())
                .replace(CHANGE_EMAIL_LINK, changeEmailLink);
    }
    
    public String extractSmsToken(String text, User user) {
        return text.replace(getForgetPasswordSmsContent("", user), "");
    }
    
    public String extractChangePhoneToken(String text, User user) {
        return text.replace(getChangeMobileSmsContent("", user), "");
    }

}
