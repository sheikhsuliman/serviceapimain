package com.siryus.swisscon.api.auth;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.siryus.swisscon.api.general.langcode.LangCode;
import com.siryus.swisscon.api.auth.user.User;
import com.siryus.swisscon.api.util.TemplateUtil;
import com.siryus.swisscon.api.util.TranslationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class LemonTemplateUtilTest {

    private static EmailLinkUtil emailLinkUtil;
    private static LemonTemplateUtil lemonTemplateUtil;
    private static TemplateUtil templateUtil;
    private static User user;

    public LemonTemplateUtilTest() {
        this.initTemplateUtil();
        this.initUser();
    }

    static class TestLanguagesParameters implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(Arguments.of("en_US"), Arguments.of("de_CH"));
        }
    }

    private void initUser() {
        user = new User();
        user.setId(1);
        user.setGivenName("first");
        user.setSurName("second");
        user.setEmail("test@mail.com");

        LangCode langCode = new LangCode();
        langCode.setId("en_US");
        user.setPrefLang(langCode);
    }

    private void initTemplateUtil() {
        LemonProperties lemonProperties = new LemonProperties();
        lemonProperties.setApplicationUrl("https://test.swisscon.com");
        TranslationUtil translationUtil = new TranslationUtil();

        emailLinkUtil = new EmailLinkUtil(lemonProperties);
        templateUtil = new TemplateUtil(translationUtil);
        lemonTemplateUtil = new LemonTemplateUtil(lemonProperties, translationUtil, templateUtil);

        ReflectionTestUtils.setField(emailLinkUtil, "confirmationLink", "/#/login/register?code={code}&user={userId}");
        ReflectionTestUtils.setField(emailLinkUtil, "invitationLink", "/#/login/register?code={code}&user={userId}");
        ReflectionTestUtils.setField(emailLinkUtil, "signupLink", "/#/signup/{email}/{code}");
        ReflectionTestUtils.setField(emailLinkUtil, "resetLink", "/#/login/register?code={code}&user={userId}");
        ReflectionTestUtils.setField(emailLinkUtil, "changeEmailLink", "/#/login/register?code={code}&user={userId}");
    }

    @Test
    public void testInvitationLink() {
        String invitationLink = emailLinkUtil.getInvitationLink("123", 1);

        String expectedLink = "https://test.swisscon.com/#/login/register?code=123&user=1";
        assertEquals(expectedLink, invitationLink);
    }

    @ParameterizedTest
    @ArgumentsSource(TestLanguagesParameters.class)
    public void testChangeEmailContent(String lang) {
        user.getPrefLang().setId(lang);
        String content = lemonTemplateUtil.getChangeEmailMailContent(user, "https://www.somerandomlink.com?code=123");

        assertBasicSwissconHtml(content);
        String expectedVerificationLink = "https://www.somerandomlink.com?code=123";
        assertTrue(content.contains(expectedVerificationLink), "confirmation link is not correct in the HTML File");
    }

    @ParameterizedTest
    @ArgumentsSource(TestLanguagesParameters.class)
    public void testConfirmationMailContent(String lang) {
        user.getPrefLang().setId(lang);
        String content = lemonTemplateUtil.getConfirmationMailContent("https://www.somerandomlink.com?code=123", user);

        assertBasicSwissconHtml(content);
        String expectedConfirmationLink = "https://www.somerandomlink.com?code=123";
        assertTrue(content.contains(expectedConfirmationLink), "confirmation link is not correct in the HTML File");
    }

    @ParameterizedTest
    @ArgumentsSource(TestLanguagesParameters.class)
    public void testInvitationMailContent(String lang) {
        user.getPrefLang().setId(lang);
        String content = lemonTemplateUtil.getInvitationMailContent("https://test.swisscon.com/#/login/register?code=123&user=1", "inviting user", user, "test company", user.getPrefLang());

        assertBasicSwissconHtml(content);
        String expectedConfirmationLink = "https://test.swisscon.com/#/login/register?code=123&user=1";
        assertTrue(content.contains(expectedConfirmationLink), "confirmation link is not correct in the HTML File");
    }

    @ParameterizedTest
    @ArgumentsSource(TestLanguagesParameters.class)
    public void testCompanyInvitationMailContent(String lang) {
        user.getPrefLang().setId(lang);
        String content = lemonTemplateUtil.getCompanyInvitationMailContent(user, "https://test.swisscon.com/#/signup/test@test.com/ey234u23u", user.getPrefLang());
        assertBasicSwissconHtml(content);
    }

    @ParameterizedTest
    @ArgumentsSource(TestLanguagesParameters.class)
    public void testResetMailContent(String lang) {
        user.getPrefLang().setId(lang);
        String content = lemonTemplateUtil.getResetMailContent("https://www.somerandomlink.com?code=123", user);

        assertBasicSwissconHtml(content);

        String expectedConfirmationLink = "https://www.somerandomlink.com?code=123";
        assertTrue(content.contains(expectedConfirmationLink), "confirmation link is not correct in the HTML File");
    }

    @ParameterizedTest
    @ArgumentsSource(TestLanguagesParameters.class)
    public void testWelcomeMailContent(String lang) {
        user.getPrefLang().setId(lang);
        String content = lemonTemplateUtil.getWelcomeMailContent(user, "test company");

        assertBasicSwissconHtml(content);

        assertTrue(content.contains("test@mail.com"), "HTML File should contain user's mail");
        assertTrue(content.contains("test company"), "HTML File should contain company name");

    }

    private void assertBasicSwissconHtml(String content) {
        String trimmedContent = content.trim();
        assertTrue(trimmedContent.contains("<table"), "HTML File should contain with <table");
        assertTrue(trimmedContent.endsWith("</table>"), "HTML File should end with </table>");
        assertTrue(containsNameOrMail(trimmedContent), "HTML File should contain full name or mail");
        assertFalse(trimmedContent.contains("{") || trimmedContent.contains("}"), "HTML File should not contain Brackets for Substitutions");
        assertTrue(trimmedContent.contains("Siryus AG, Luzernerstrasse 43"), "HTML Files contains Siryus Address");
    }

    private boolean containsNameOrMail(String content) {
        return (content.contains("first") && content.contains("second")) || content.contains("test@mail.com");
    }

}
