package com.siryus.swisscon.api.util.validator;

import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Size;
import java.util.Set;
import java.util.function.Function;

public class DTOValidator {

    public static <T> String validate(T dto) {
        return validate(dto, DTOValidator::buildViolationsDescription);
    }

    public static <T> void validateAndThrow(T dto, Function<String, RuntimeException> onError) {
        String errorMessage = validate(dto);

        if (errorMessage != null) {
            throw onError.apply(errorMessage);
        }
    }

    public static <T> void validateAndThrow(T dto) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<T>> violations = validator.validate(dto);

        if(! violations.isEmpty()) {
            throwAppropriateException(violations.iterator().next());
        }
    }

    public static <T> LocalizedResponseStatusException calculateAppropriateException(ConstraintViolation<T> violation) {
        var annotation = violation.getConstraintDescriptor().getAnnotation();

        if (annotation.annotationType().equals(javax.validation.constraints.NotNull.class)) {
            return ValidationExceptions.canNotBeNull(violation.getPropertyPath().toString());
        }
        if (annotation.annotationType().equals(javax.validation.constraints.NotEmpty.class)) {
            return ValidationExceptions.canNotBeEmpty(violation.getPropertyPath().toString());
        }
        if (annotation.annotationType().equals(javax.validation.constraints.NotBlank.class)) {
            return ValidationExceptions.canNotBeBlank(violation.getPropertyPath().toString());
        }
        if (annotation.annotationType().equals(javax.validation.constraints.Size.class)) {
            Size sizeAnnotation = (Size) annotation;

            return ValidationExceptions.fieldValueIsWrongSize(violation.getPropertyPath().toString(), sizeAnnotation.min(), sizeAnnotation.max());
        }
        if (annotation.annotationType().equals(Reference.class)) {
            Reference reference = (Reference) annotation;
            return ValidationExceptions.notValidReference(violation.getPropertyPath().toString(), violation.getInvalidValue(), reference.value());
        }
        if (annotation.annotationType().equals(SafeHTML.class)) {
            return ValidationExceptions.unsafeHtml(violation.getPropertyPath().toString(), violation.getInvalidValue());
        }

        return ValidationExceptions.validationError(violation);
    }

    private static <T> void throwAppropriateException(ConstraintViolation<T> violation) {
        throw calculateAppropriateException(violation);
    }

    public static <T> String validate(T dto, Function<Set<ConstraintViolation<T>>, String> messageBuilder) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<T>> violations = validator.validate(dto);

        return violations.isEmpty() ? null : messageBuilder.apply(violations);
    }

    private static <T> String buildViolationsDescription(Set<ConstraintViolation<T>> violations) {
        StringBuilder result = new StringBuilder();
        violations.forEach( v -> {
            result
                    .append(v.getPropertyPath())
                    .append(" - ")
                    .append(v.getMessage())
                    .append("\n");
        });
        return result.toString();
    }

}
