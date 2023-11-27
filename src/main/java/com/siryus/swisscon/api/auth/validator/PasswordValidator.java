package com.siryus.swisscon.api.auth.validator;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.function.IntPredicate;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 50;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean isValid;
        isValid = StringUtils.isNotBlank(value);
        isValid = isValid && value.length() >= MIN_LENGTH;
        isValid = isValid && value.length() <= MAX_LENGTH;
        isValid = isValid && containsUppercase(value);
        isValid = isValid && containsLowerCase(value);
        isValid = isValid && containsNumber(value);
        return isValid;
    }

    private boolean containsNumber(String value) {
        return contains(value, Character::isDigit);
    }

    private boolean containsUppercase(String value) {
        return contains(value, i -> Character.isLetter(i) && Character.isUpperCase(i));
    }

    private boolean containsLowerCase(String value) {
        return contains(value, i -> Character.isLetter(i) && Character.isLowerCase(i));
    }

    private boolean contains(String value, IntPredicate predicate) {
        return value.chars().anyMatch(predicate);
    }

}
