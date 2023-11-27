package com.siryus.swisscon.api.exceptions;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.siryus.swisscon.api.exceptions.LocalizedReason.ParameterValue.pv;
import static org.junit.jupiter.api.Assertions.*;

class LocalizedReasonTest {

    @Test
    void Given_stringWithoutParameters_When_extractParameters_Then_returnEmptySet() {
        Set<String> parameters = LocalizedReason.extractParameters("Hello World");

        assertNotNull(parameters);
        assertTrue(parameters.isEmpty());
    }

    @Test
    void Given_stringWithParameters_When_extractParameters_Then_returnAllParameters() {
        Set<String> parameters = LocalizedReason.extractParameters("Hello {{who}} of {{where}}");

        assertNotNull(parameters);
        assertEquals(2, parameters.size());
        assertTrue(parameters.contains("who"));
        assertTrue(parameters.contains("where"));
    }

    @Test
    void Given_templateWithMatchingParameters_When_replaceParameters_Then_replaceAllParameters() {
        Map<String, String> p = new HashMap<>();
        p.put("who", "Tiger");
        p.put("where", "Malta");

        String result = LocalizedReason.replaceParameters("Hello {{who}} of {{where}}", p);

        assertNotNull(result);
        assertEquals("Hello Tiger of Malta", result);
    }

    @Test
    void Given_templateHasMissingParameter_When_replaceParameters_Then_leaveMissingParameterAsIs() {
        Map<String, String> p = new HashMap<>();
        p.put("where", "Malta");

        String result = LocalizedReason.replaceParameters("Hello {{who}} of {{where}}", p);

        assertNotNull(result);
        assertEquals("Hello {{who}} of Malta", result);
    }

    @Test
    void Given_oneOfParametersIsNull_When_replaceParameters_Then_replaceNullParameterWithNullString() {
        LocalizedReason lrTemplate = LocalizedReason.like(666, "Hello {{who}} of {{where}}");
        LocalizedReason lr = lrTemplate.with(pv("who", null), pv("where", "Malta"));

        String result = lr.getReason();

        assertNotNull(result);
        assertEquals("Hello null of Malta", result);
    }
}