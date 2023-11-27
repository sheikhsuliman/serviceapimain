package com.siryus.swisscon.api.util;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class TemplateUtil {

    private final TranslationUtil translationUtil;

    @Autowired
    public TemplateUtil(TranslationUtil translationUtil) {
        this.translationUtil = translationUtil;
    }

    public String loadTranslatedTemplate(String fileName, String lang, String... keysAndValues) {
        Map<String, String> extraVariables = new LinkedHashMap<>();

        for( int i = 0; i+1 < keysAndValues.length; i = i + 2) {
            extraVariables.put(keysAndValues[i], keysAndValues[i+1]);
        }

        return loadTranslatedTemplate(fileName, lang, extraVariables);
    }
    public String loadTranslatedTemplate(String fileName, String lang, Map<String, String> extraVariables) {
        try {
            String fileContent = loadResourceAsString(fileName);

            Map<String, String> translations = new HashMap<>(translationUtil.getMap(lang));
            for (Map.Entry<String, String> entry : translations.entrySet()) {
                fileContent = fileContent.replace("{" + entry.getKey() + "}", entry.getValue());
            }

            for (Map.Entry<String, String> entry : extraVariables.entrySet()) {
                fileContent = fileContent.replace("{" + entry.getKey() + "}", entry.getValue());
            }

            return fileContent;
        } catch (IOException e) {
            throw new AuthenticationException("HTML Template " + fileName + " couldn't be loaded") { };
        }
    }

    private String loadResourceAsString(String fileName) throws IOException {
        InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(fileName));
        return IOUtils.toString(inputStream, Charsets.UTF_8);
    }

}
