package com.siryus.swisscon.api.util.validator;

import com.siryus.swisscon.api.general.reference.ReferenceService;
import com.siryus.swisscon.api.general.reference.ReferenceType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ReferenceValidator implements ConstraintValidator<Reference, Object> {
    protected ReferenceType referenceType;
    private boolean canBeDisabled;

    @Override
    public void initialize(Reference constraintAnnotation) {
        referenceType =constraintAnnotation.value();
        canBeDisabled = constraintAnnotation.canBeDisabled();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return value == null || referenceService().isValidReference(referenceType, value, canBeDisabled);
    }

    private ReferenceService referenceService() {
        return SpringContext.getBean(ReferenceService.class);
    }
}
