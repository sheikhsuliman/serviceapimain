package com.siryus.swisscon.api.util.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, PARAMETER, ANNOTATION_TYPE, CONSTRUCTOR, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { SafeHTMLValidator.class })
public @interface SafeHTML {

    String message() default "HTML contains invalid tags: ${validatedValue}";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
