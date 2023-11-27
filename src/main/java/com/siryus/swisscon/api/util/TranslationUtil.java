package com.siryus.swisscon.api.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class TranslationUtil {

    public static final String DEFAULT_LANGUAGE = "de";

    private static final Map<String, String> TRANSLATION_FILES = Map.of(
            "en", "translation/en.json",
            "de", "translation/de.json"
    );

    private Map<String, HashMap<String, String>> translations;

    public TranslationUtil() {
        initTranslations();
    }

    private void initTranslations() {
        this.translations = TRANSLATION_FILES
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> loadTranslations(e.getValue())));
    }

    private HashMap<String, String> loadTranslations(String fileName) {
        try {
            InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(fileName));
            String strContent = IOUtils.toString(inputStream, Charsets.UTF_8);

            return new ObjectMapper()
                    .readValue(strContent, new TypeReference<HashMap<String, String>>() {
                    });
        } catch (IOException e) {
            throw new RuntimeException("Could not load Translations: " + fileName, e);
        }
    }

    private String extractLangCode(String languageId) {
        return languageId.substring(0,2);
    }

    public Map<String, String> getMap(String languageId) {
        return translations.get(extractLangCode(languageId));
    }

    public String get(String key) {
        return get(key, DEFAULT_LANGUAGE);
    }

    public String get(String key, String languageId) {
        String effectiveLangId = StringUtils.isBlank(languageId) ? DEFAULT_LANGUAGE : languageId;

        HashMap<String, String> translation = this.translations.get(extractLangCode(effectiveLangId));
        if (translation != null) {
            return translation.getOrDefault(key, "");
        }
        return this.translations.get(DEFAULT_LANGUAGE).getOrDefault(key, "");
    }

}
