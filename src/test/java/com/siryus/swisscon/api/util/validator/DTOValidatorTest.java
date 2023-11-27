package com.siryus.swisscon.api.util.validator;

import com.siryus.swisscon.api.base.AbstractMvcTestBase;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DTOValidatorTest extends AbstractMvcTestBase {

    @Test
    void Given_null_When_validate_Then_throwNotNull() {
        TestDTO dto = TestDTO.builder()
                .notNullField(null)
                .nullField(null)
                .notEmptyField("Hello")
                .build();

        LocalizedResponseStatusException thrownException = assertThrows(LocalizedResponseStatusException.class, () -> {
            DTOValidator.validateAndThrow(dto);
        });

        assertEquals(ValidationExceptions.CAN_NOT_BE_NULL.getErrorCode(), thrownException.getLocalizedReason().getErrorCode());
    }

    @Test
    void Given_empty_When_validate_Then_throwNotNull() {
        TestDTO dto = TestDTO.builder()
                .notNullField(13)
                .nullField(null)
                .notEmptyField("")
                .build();

        LocalizedResponseStatusException thrownException = assertThrows(LocalizedResponseStatusException.class, () -> {
            DTOValidator.validateAndThrow(dto);
        });

        assertEquals(ValidationExceptions.CAN_NOT_BE_EMPTY.getErrorCode(), thrownException.getLocalizedReason().getErrorCode());
    }

    @Test
    void Given_unknown_When_validate_Then_throwGeneralException() {
        TestDTO dto = TestDTO.builder()
                .notNullField(13)
                .nullField(1)
                .notEmptyField("Hello")
                .build();

        LocalizedResponseStatusException thrownException = assertThrows(LocalizedResponseStatusException.class, () -> {
            DTOValidator.validateAndThrow(dto);
        });

        assertEquals(ValidationExceptions.VALIDATION_ERROR.getErrorCode(), thrownException.getLocalizedReason().getErrorCode());
    }

    @Test
    void Given_invalidReference_When_validate_Then_throwNotValidReference() {
        TestDTO dto = TestDTO.builder()
                .notNullField(1)
                .nullField(null)
                .notEmptyField("Hello")
                .userIdField(99999)
                .build();

        LocalizedResponseStatusException thrownException = assertThrows(LocalizedResponseStatusException.class, () -> {
            DTOValidator.validateAndThrow(dto);
        });

        assertEquals(ValidationExceptions.NOT_VALID_REFERENCE.getErrorCode(), thrownException.getLocalizedReason().getErrorCode());
    }
}