package com.siryus.swisscon.api.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CustomStringUtilsTest {

    @Test
    public void Given_numberWithLeadingZeros_When_removeLeadingZeros_Then_Success() {
        assertEquals("101", CustomStringUtils.removeLeadingZeros("000101"));
        assertEquals("79818987898780", CustomStringUtils.removeLeadingZeros("079818987898780"));
    }

    @Test
    public void Given_stringsWithSpaces_When_removeSpaces_Then_Success() {
        assertEquals("1111", CustomStringUtils.removeAllSpaces(" 11  1      1   "));
    }

    @Test
    public void Given_variousBigDecimals_When_twoDecimalString_Then_Success() {
        assertEquals("11.45", CustomStringUtils.twoDecimalString(new BigDecimal(("11.4523"))));
        assertEquals("0.00", CustomStringUtils.twoDecimalString(new BigDecimal(("0"))));
        assertNull(CustomStringUtils.twoDecimalString(null));
    }

    @Test
    public void Given_stringsWithSeparators_When_SeparateString_Then_Success() {
        assertEquals("ab,cd,ef", CustomStringUtils.separateNewLineWithComma("ab \n cd \n ef"));
        assertEquals("ab\ncd\nef", CustomStringUtils.separateCommaWithNewLine("ab , cd , ef"));
    }

    @Test
    public void Given_stringsWithDifferentLengths_When_substringAfterIndex_Then_Success() {
        assertEquals("", CustomStringUtils.substringAfterIndex("abc", 5));
        assertEquals("101", CustomStringUtils.substringAfterIndex("56xzTZ101", 6));
    }

}
