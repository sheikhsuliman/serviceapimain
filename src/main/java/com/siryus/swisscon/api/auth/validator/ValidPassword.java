package com.siryus.swisscon.api.auth.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Repeatable(ValidPassword.List.class)
@Documented
@Constraint(validatedBy = {PasswordValidator.class})
public @interface ValidPassword {

    String MESSAGE = "Password doesn't meet the minimum requirements: 8 tokens, 1 number, 1 uppercase";

    String message() default MESSAGE;

    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };

    @Target({FIELD})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        ValidPassword[] value();
    }
}
