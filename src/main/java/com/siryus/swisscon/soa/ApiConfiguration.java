package com.siryus.swisscon.soa;

import com.siryus.commons.utils.AbstractStackConfiguration;
import org.apache.commons.text.CaseUtils;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiConfiguration extends AbstractStackConfiguration {
    private static final ApiConfiguration SINGLETON = new ApiConfiguration();

    public enum Var {
        RABBITMQ_HOST,
        RABBITMQ_USERNAME,
        RABBITMQ_PASSWORD,
        MONOLITH_SERVICE_BASE,

        MEDIA_WIDGET_V2,
        MEDIA_WIDGET_V2_CUT_OFF_PROJECT_ID;

        public String asCamelCase() {
            return CaseUtils.toCamelCase(this.name(), false, '_');
        }
    }

    public static ApiConfiguration configuration() {
        return SINGLETON;
    }

    public static String getVar( Var var ) {
        return SINGLETON.get(var);
    }

    public static Map<String, String> buildVarsMap(Var... vars) {
        return Arrays.stream(vars)
            .map( v -> new AbstractMap.SimpleEntry<>(v.asCamelCase(), ApiConfiguration.getVar(v)))
            .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }
}
