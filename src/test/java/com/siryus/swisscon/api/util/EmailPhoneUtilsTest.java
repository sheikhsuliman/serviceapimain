package com.siryus.swisscon.api.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailPhoneUtilsTest {

    private static final Integer COUNTRY_CODE = 41;
    private static final String PHONE_NUMBER = "796432124";
    private static final String CORRECT_PHONE_NUMBER = COUNTRY_CODE + PHONE_NUMBER;
    private static final String EMAIL = "valid@valid.com";

    /** TEST IS PHONE WITH FULL NUMBER **/

    @Test
    public void Given_correctPhoneNumber_When_isPhone_Then_True() {
        assertTrue(EmailPhoneUtils.isPhone(CORRECT_PHONE_NUMBER));
        assertTrue(EmailPhoneUtils.isPhone(COUNTRY_CODE, PHONE_NUMBER));
    }

    @Test
    public void Given_mailWithNumber_When_isPhone_Then_False() {
        assertFalse(EmailPhoneUtils.isPhone("123456@4562.com"));
        assertFalse(EmailPhoneUtils.isPhone(COUNTRY_CODE, "123456@4562.com"));
    }

    @Test
    public void Given_onlyLetters_When_isPhone_Then_False() {
        assertFalse(EmailPhoneUtils.isPhone("+abc"));
        assertFalse(EmailPhoneUtils.isPhone(COUNTRY_CODE, "+abc"));
    }

    @Test
    public void Given_phoneWithLetters_When_isPhone_Then_False() {
        assertFalse(EmailPhoneUtils.isPhone("792345-ABC"));
        assertFalse(EmailPhoneUtils.isPhone(COUNTRY_CODE, "792345-ABC"));
    }

    @Test
    public void Given_noCountryCode_When_isPhone_Then_False() {
        assertFalse(EmailPhoneUtils.isPhone(null, PHONE_NUMBER));
    }

    /** TEST TO PHONE NUMBER **/

    @Test
    public void Given_correctPhoneNumber_When_formatPhoneNumber_Then_ReturnTheSameNumber() {
        assertEquals(CORRECT_PHONE_NUMBER, EmailPhoneUtils.toPhoneNumber(CORRECT_PHONE_NUMBER));
    }

    @Test
    public void Given_phoneNumberWithSpecialTokens_When_formatPhoneNumber_Then_ReturnCorrectNumber() {
        assertEquals(CORRECT_PHONE_NUMBER, EmailPhoneUtils.toPhoneNumber("41 796/ 43212 & 4"));
    }

    @Test
    public void Given_phoneNumberWithLetters_When_formatPhoneNumber_Then_ReturnCorrectNumber() {
        assertEquals(CORRECT_PHONE_NUMBER, EmailPhoneUtils.toPhoneNumber("41 796/ 43212 & 4x"));
    }

    /** TEST TO FULL PHONE NUMBER **/

    @Test
    public void Given_phoneNumberWithCountryCode_When_toFullPhoneNumber_Then_ReturnCorrectNumber() {
        assertEquals(CORRECT_PHONE_NUMBER, EmailPhoneUtils.toFullPhoneNumber(41, "796432124"));
    }

    @Test
    public void Given_missingValues_When_toFullPhoneNumber_Then_ReturnNull() {
        assertNull(EmailPhoneUtils.toFullPhoneNumber(null, "796432124"));
        assertNull(EmailPhoneUtils.toFullPhoneNumber(41, ""));
        assertNull(EmailPhoneUtils.toFullPhoneNumber(41, null));
    }

    /** TEST TO MAIL OR PHONE NUMBER **/

    @Test
    public void Given_email_When_toMailOrPhoneNumber_Then_returnMail() {
        assertEquals(EMAIL, EmailPhoneUtils.toMailOrPhoneNumber(EMAIL));
        assertEquals(EMAIL, EmailPhoneUtils.toMailOrPhoneNumber(EMAIL, null));
    }

    @Test
    public void Given_correctPhoneNumber_When_toMailOrPhoneNumber_Then_returnMail() {
        assertEquals(CORRECT_PHONE_NUMBER, EmailPhoneUtils.toMailOrPhoneNumber(CORRECT_PHONE_NUMBER));
        assertEquals(CORRECT_PHONE_NUMBER, EmailPhoneUtils.toMailOrPhoneNumber(PHONE_NUMBER, COUNTRY_CODE));
    }

    /** TEST IS EMAIL **/

    @Test
    public void Given_correctMail_When_isMail_Then_True() {
        assertTrue(EmailPhoneUtils.isEmail("abc@abc.com"));
    }

    @Test
    public void Given_mailWithoutDomain_When_isMail_Then_False() {
        assertFalse(EmailPhoneUtils.isEmail("abc@abc"));
    }

    @Test
    public void Given_mailWithoutAt_When_isMail_Then_False() {
        assertFalse(EmailPhoneUtils.isEmail("abc.com"));
    }

    @Test
    public void Given_mailWithoutName_When_isMail_Then_False() {
        assertFalse(EmailPhoneUtils.isEmail("@abc.com"));
    }

    @Test
    public void Given_mailWithDots_When_isMail_Then_True() {
        assertTrue(EmailPhoneUtils.isEmail("Project.Admin@build-anything-anywhere.com"));
    }

}
