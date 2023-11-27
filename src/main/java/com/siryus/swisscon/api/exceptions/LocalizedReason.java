package com.siryus.swisscon.api.exceptions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LocalizedReason {
    private final static Logger LOGGER = LoggerFactory.getLogger(LocalizedReason.class);

    private static final Map<Integer, LocalizedReason> LOCALIZED_REASON_REGISTRY = new HashMap<>();

    private final int errorCode;
    private final String enTemplate;
    private final Map<String,String> parameters;

    @JsonCreator
    LocalizedReason(
            @JsonProperty("errorCode") int errorCode,
            @JsonProperty("reason") String enTemplate,
            @JsonProperty("parameters") Map<String, String> parameters
    ) {
        this.errorCode = errorCode;
        this.parameters = new HashMap<>(parameters);
        this.enTemplate = enTemplate;
    }

    public static LocalizedReason like(int errorCode, String enReason) {
        return new LocalizedReason(
                errorCode,
                enReason,
                Collections.emptyMap()
        ).register();
    }

    public LocalizedReason with(ParameterValue... parameterValues) {
        return new LocalizedReason(
                errorCode,
                enTemplate,
                Arrays.stream(parameterValues).collect(Collectors.toMap(ParameterValue::getName, ParameterValue::getValue))
        ).audit();
    }

    public Map<String, Object> asMap() {
        return Map.of(
                "errorCode", errorCode,
                "reason", getReason(),
                "parameters", parameters
        );
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LocalizedReason.class.getSimpleName() + "[", "]")
                .add("errorCode=" + errorCode)
                .add("enTemplate='" + enTemplate + "'")
                .add("parameters=" + parameters)
                .toString();
    }

    public int getErrorCode() {
        return errorCode;
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public String getReason() {
        return LocalizedReason.replaceParameters(enTemplate, parameters);
    }

    static Pattern mustacheVariable = Pattern.compile("\\{\\{([a-zA-Z_]+)\\}\\}");

    static Set<String> extractParameters(String template) {
        Matcher m = mustacheVariable.matcher(template);

        Set<String> names = new HashSet<>();

        while (m.find()) {
            names.add(m.group(1));
        }

        return names;
    }

    static String replaceParameters(String template, Map<String,String> parameters) {
        return mustacheVariable.matcher(template).replaceAll(
                m -> parameters.computeIfAbsent(m.group(1), k -> reportMissingParameter(k, template))
        );
    }

    private static String reportMissingParameter(String key, String template) {
        LOGGER.warn("Required parameter '{}' was not provided for template '{}'", key, template);
        return "{{" + key + "}}";
    }

    public static class ParameterValue {
        private final String name;
        private final String value;

        ParameterValue(String name, Object value) {
            this.name = name;
            this.value = String.valueOf(value);
        }

        public static ParameterValue pv(String name, Object value) {
            return new ParameterValue(name, value);
        }

        public static Map<String, String> asMap(ParameterValue... pvs) {
            return Arrays.stream(pvs).collect(Collectors.toMap(ParameterValue::getName, ParameterValue::getValue));
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    private LocalizedReason register() {
        if (LOCALIZED_REASON_REGISTRY.containsKey(errorCode)) {
            LocalizedReason existingOne = LOCALIZED_REASON_REGISTRY.get(errorCode);
            throw new RuntimeException("Duplicate errorCode : " + errorCode + " existing: " + existingOne + " new: " + this);
        }
        LOCALIZED_REASON_REGISTRY.put(errorCode, this);

        return this;
    }

    private LocalizedReason audit() {
        return this;
    }
}

