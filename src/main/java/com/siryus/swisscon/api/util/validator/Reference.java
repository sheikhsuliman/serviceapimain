package com.siryus.swisscon.api.util.validator;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference.List;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
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
@Repeatable(List.class)
@Documented
@Constraint(validatedBy = { ReferenceValidator.class })
public @interface Reference {

    String message() default "{value} with ID = ${validatedValue} does not exist";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };

    boolean canBeDisabled() default false;

    ReferenceType value();

    @Target({  FIELD, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    @interface List {
        Reference[] value();
    }
}
