package com.siryus.swisscon.api.auth;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class EmailLinkUtilTest {

    private static EmailLinkUtil emailLinkUtil;

    public EmailLinkUtilTest() {
        this.initEmailLinkUtil();
    }

    private void initEmailLinkUtil() {
        LemonProperties lemonProperties = new LemonProperties();
        lemonProperties.setApplicationUrl("https://test.swisscon.com");

        emailLinkUtil = new EmailLinkUtil(lemonProperties);

        //@Deprecated TODO remove after SI-177
        ReflectionTestUtils.setField(emailLinkUtil, "signupLinkDepcrecated", "/#/signup/{email}/{code}");

        ReflectionTestUtils.setField(emailLinkUtil, "confirmationLink", "/#/confirm?code={code}&user={userId}");
        ReflectionTestUtils.setField(emailLinkUtil, "invitationLink", "/#/register?code={code}&user={userId}");
        ReflectionTestUtils.setField(emailLinkUtil, "signupLink", "/#/signup/{companyId}/{code}");
        ReflectionTestUtils.setField(emailLinkUtil, "resetLink", "/#/reset?code={code}&user={userId}");
        ReflectionTestUtils.setField(emailLinkUtil, "changeEmailLink", "/#/change-email?code={code}&user={userId}");
    }

    @Test
    public void testConfirmationLink() {
        String confirmationLink = emailLinkUtil.getConfirmationLink("/confirmation?code=123", 1);

        String expectedLink = "https://test.swisscon.com/#/confirm?code=123&user=1";
        assertEquals(expectedLink, confirmationLink);
    }

    @Test
    public void testInvitationLink() {
        String invitationLink = emailLinkUtil.getInvitationLink("123", 1);

        String expectedLink = "https://test.swisscon.com/#/register?code=123&user=1";
        assertEquals(expectedLink, invitationLink);
    }

    @Test
    @Deprecated //TODO remove after SI-177
    public void testSignupLinkDeprecated() {
        String signupLink = emailLinkUtil.getSignupLinkDeprecated("test@test.com", "123");

        String expectedLink = "https://test.swisscon.com/#/signup/test@test.com/123";
        assertEquals(expectedLink, signupLink);
    }

    @Test
    public void testSignupLink() {
        String signupLink = emailLinkUtil.getSignupLink(5, "123");

        String expectedLink = "https://test.swisscon.com/#/signup/5/123";
        assertEquals(expectedLink, signupLink);
    }

    @Test
    public void testResetLink() {
        String resetLink = emailLinkUtil.getResetLink("/reset?code=123", 1);

        String expectedLink = "https://test.swisscon.com/#/reset?code=123&user=1";
        assertEquals(expectedLink, resetLink);
    }

    @Test
    public void testChangeEmailLink() {
        String changeEmailLink = emailLinkUtil.getEmailChangeLink("/verify?code=123", 1);

        String expectedLink = "https://test.swisscon.com/#/change-email?code=123&user=1";
        assertEquals(expectedLink, changeEmailLink);
    }

}
