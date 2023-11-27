package com.siryus.swisscon.api.util.validator;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SafeHTMLValidator implements ConstraintValidator<SafeHTML, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return value == null || Jsoup.isValid((String)value, Whitelist.basic());
    }
}
