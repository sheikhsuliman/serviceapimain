package com.siryus.swisscon.api.util;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CustomStringUtils {

    private static final String LEADING_ZEROS_REGEX = "^0+(?!$)";
    private static final String SPACE = "\\s";

    public static String removeLeadingZeros(String str) {
        if (StringUtils.isNotBlank(str)) {
            return str.replaceFirst(LEADING_ZEROS_REGEX, "");
        }
        return str;
    }

    public static String removeAllSpaces(String str) {
        if (StringUtils.isNotBlank(str)) {
            return str.replaceAll(SPACE, "");
        }
        return str;
    }

    public static String twoDecimalString(BigDecimal num) {
        if (num != null) {
            return new DecimalFormat("0.00").format(num);
        }
        return null;
    }

    public static String separateNewLineWithComma(String str) {
        if (StringUtils.isNotBlank(str)) {
            return replaceSeparatorTrimFields(str, "\n", ",");
        }
        return str;
    }

    public static String separateCommaWithNewLine(String str) {
        if (StringUtils.isNotBlank(str)) {
            return replaceSeparatorTrimFields(str, ",", "\n");
        }
        return str;
    }

    public static String substringAfterIndex(String str, Integer index) {
        if (!(StringUtils.isBlank(str) || str.length() - 1 < index)) {
            return str.substring(index);
        }
        return "";
    }

    private static String replaceSeparatorTrimFields(String str, String exist, String replace) {
        if (StringUtils.isNotBlank(str)) {
            return Arrays.stream(str.split(exist))
                    .map(String::trim)
                    .collect(Collectors.joining(replace));
        }
        return str;
    }

}
