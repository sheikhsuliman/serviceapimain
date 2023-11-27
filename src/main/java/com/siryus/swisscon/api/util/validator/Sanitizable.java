package com.siryus.swisscon.api.util.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { SanitizabledValidator.class })

public @interface Sanitizable {
    String message() default "n/a";

    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
