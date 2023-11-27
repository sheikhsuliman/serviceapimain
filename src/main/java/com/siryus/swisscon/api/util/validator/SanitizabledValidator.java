package com.siryus.swisscon.api.util.validator;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

public class SanitizabledValidator implements ConstraintValidator<Sanitizable, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        var fields = value.getClass().getDeclaredFields();
        for(var field : fields) {
            if (field.getAnnotation(SanitizableHtml.class) != null) {
                sanitizeField(value, field);
            }
        }
        return true;
    }

    @SneakyThrows
    private void sanitizeField(Object value, Field field) {
        field.setAccessible(true);
        String rawValue = (String) field.get(value);

        if (rawValue != null) {
            var sanitizedValue = Jsoup.clean(rawValue, Whitelist.basic());
            field.set(value, sanitizedValue);
        }
    }
}
