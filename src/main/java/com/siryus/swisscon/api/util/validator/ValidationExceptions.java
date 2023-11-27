package com.siryus.swisscon.api.util.validator;

import com.siryus.swisscon.api.exceptions.LocalizedReason;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import com.siryus.swisscon.api.general.reference.ReferenceType;

import javax.validation.ConstraintViolation;

import static com.siryus.swisscon.api.exceptions.LocalizedReason.ParameterValue.pv;

public class ValidationExceptions {
    private static int e(int n) {
        return LocalizedResponseStatusException.VALIDATION_ERROR_CODE + n;
    }

    public static final LocalizedReason VALIDATION_ERROR = LocalizedReason.like(e(0), "Validation Error : {{violation}}");
    public static final LocalizedReason CAN_NOT_BE_NULL = LocalizedReason.like(e(1), "Field {{fieldName}} can not be null");
    public static final LocalizedReason CAN_NOT_BE_EMPTY = LocalizedReason.like(e(2), "Field {{fieldName}} can not be empty");
    public static final LocalizedReason NOT_VALID_REFERENCE = LocalizedReason.like(e(3), "Field {{fieldName}} with value {{fieldValue}} is not valid {{referenceType}} reference");
    public static final LocalizedReason UNSAFE_HTML = LocalizedReason.like(e(4), "Field {{fieldName}} contains unsafe HTML {{unsafeHTML}}");
    public static final LocalizedReason CAN_NOT_BE_BLANK = LocalizedReason.like(e(5), "Field {{fieldName}} can not be blank");
    public static final LocalizedReason FIELD_VALUE_IS_WRONG_SIZE = LocalizedReason.like(e(6), "Field {{fieldName}} is wrong size (min: {{min}}, max: {{max}})");

    static <T> LocalizedResponseStatusException validationError(ConstraintViolation<T> violation) {
        return LocalizedResponseStatusException.badRequest(VALIDATION_ERROR.with(pv("violation", violation.toString())));
    }
    static LocalizedResponseStatusException canNotBeNull(String fieldName) {
        return LocalizedResponseStatusException.badRequest(CAN_NOT_BE_NULL.with(pv("fieldName", fieldName)));
    }
    static LocalizedResponseStatusException canNotBeEmpty(String fieldName) {
        return LocalizedResponseStatusException.badRequest(CAN_NOT_BE_EMPTY.with(pv("fieldName", fieldName)));
    }
    static LocalizedResponseStatusException canNotBeBlank(String fieldName) {
        return LocalizedResponseStatusException.badRequest(CAN_NOT_BE_BLANK.with(pv("fieldName", fieldName)));
    }
    static LocalizedResponseStatusException fieldValueIsWrongSize(String fieldName, Integer min, Integer max) {
        return LocalizedResponseStatusException.badRequest(FIELD_VALUE_IS_WRONG_SIZE.with(
                pv("fieldName", fieldName),
                pv("min", min),
                pv("max", max)
        ));
    }
    static LocalizedResponseStatusException notValidReference(String fieldName, Object fieldValue, ReferenceType referenceType) {
        return LocalizedResponseStatusException.badRequest(NOT_VALID_REFERENCE.with(pv("fieldName", fieldName), pv("fieldValue", fieldValue), pv("referenceType", referenceType)));
    }

    public static LocalizedResponseStatusException unsafeHtml(String fieldName, Object unsafeHTML) {
        return  LocalizedResponseStatusException.badRequest(UNSAFE_HTML.with(pv("fieldName", fieldName), pv("unsafeHTML", unsafeHTML)));
    }
}
