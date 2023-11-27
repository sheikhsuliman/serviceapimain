package com.siryus.swisscon.api.auth;

import com.siryus.swisscon.api.util.TranslationUtil;
import org.junit.jupiter.api.Test;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;

public class TranslationUtilTest {

    private static final TranslationUtil translationUtil = new TranslationUtil();

    @Test
    public void testLoadMessage() {
        String value_en = translationUtil.get("mail.hi", "en_US");
        String value_de = translationUtil.get("mail.hi", "de_CH");
        assertTrue(value_en.contains("Hi"), "mail.hi should contain Hi");
        assertTrue(value_de.contains("Hallo"), "mail.hi should contain Hallo");
    }

    @Test
    public void testLoadNotExistingKey() {
        String value_en = translationUtil.get("not.existing", "en_US");
        assertTrue(value_en != null, "translated value without key should not be null");
    }

    @Test
    public void testLoadNotExistingLanguage() {
        String value_en = translationUtil.get("not.existing", "xx_US");
        assertTrue(value_en != null, "result should not be null if language doesn't exist");
    }
}
