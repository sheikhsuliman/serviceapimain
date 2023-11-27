package com.siryus.swisscon.api.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class EmailPhoneUtils {
    private static final String EMAIL_REGEX = "\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b";
    private static final String PHONE_REGEX = "^[0-9]+$";
    private static final String ALL_SPECIAL_CHARACTERS = "[^a-zA-Z0-9]";
    private static final String ALL_SPECIAL_CHARACTERS_AND_LETTERS = "[^0-9]";

    public static boolean isPhone(Integer countryCode, String phone) {
        boolean isCountryCode = countryCode != null && countryCode > 0;
        return isCountryCode && isPhone(phone);
    }

    public static boolean isPhone(String str) {
        if (StringUtils.isNotBlank(str)) {
            String phoneWithoutSpecialChars = str.replaceAll(ALL_SPECIAL_CHARACTERS, "");
            return Pattern.matches(PHONE_REGEX, phoneWithoutSpecialChars);
        }
        return false;
    }

    public static String toFullPhoneNumber(Integer countryCode, String mobile) {
        if (countryCode != null && StringUtils.isNotBlank(mobile)) {
            return countryCode + toPhoneNumber(mobile);
        }
        return null;
    }
    
    public static String toReversibleFullPhoneNumber(Integer countryCode, String mobile) {
        if (countryCode != null && StringUtils.isNotBlank(mobile)) {
            return "+" + countryCode + " " + toPhoneNumber(mobile);
        }
        return null;
    }
    
    public static Integer countryCodeFromFullPhoneNumber(String reversibleFullPhoneNumber) {
        if (!isReversibleFullPhoneNumber(reversibleFullPhoneNumber)) {
            return null;
        }

        return Integer.parseInt(reversibleFullPhoneNumber.split(" |\\+")[1]);
    }

    public static String mobileFromFullPhoneNumber(String reversibleFullPhoneNumber) {
        if (!isReversibleFullPhoneNumber(reversibleFullPhoneNumber)) {
            return null;
        }

        return reversibleFullPhoneNumber.split(" |\\+")[2];
    }        
    
    public static String toMailOrPhoneNumber(String emailOrPhone) {
        return isPhone(emailOrPhone) ? toPhoneNumber(emailOrPhone) : emailOrPhone;
    }

    public static String toMailOrPhoneNumber(String emailOrPhone, Integer countryCode) {
        return isPhone(countryCode, emailOrPhone) ? toFullPhoneNumber(countryCode, emailOrPhone) : emailOrPhone;
    }

    public static String toPhoneNumber(String str) {
        return CustomStringUtils.removeLeadingZeros(str)
                .replaceAll(ALL_SPECIAL_CHARACTERS_AND_LETTERS, "");
    }

    public static boolean isEmail(String str) {
        if (StringUtils.isNotBlank(str)) {
            return Pattern.matches(EMAIL_REGEX, str);
        }
        return false;
    }
    
    public static boolean isReversibleFullPhoneNumber(String phone) {
        return phone != null && StringUtils.isNotBlank(phone) && StringUtils.startsWith(phone, "+") && phone.contains(" ");
    }

}
