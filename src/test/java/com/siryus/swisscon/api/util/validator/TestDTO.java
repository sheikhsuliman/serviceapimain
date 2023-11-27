package com.siryus.swisscon.api.util.validator;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import lombok.Builder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Builder(toBuilder = true)
public class TestDTO {
    @NotNull
    private final Integer notNullField;

    @NotEmpty
    private final String notEmptyField;

    @Null
    private final Integer nullField;

    @Reference(ReferenceType.USER)
    private final Integer userIdField;

    public TestDTO(
            Integer notNullField,
            String notEmptyField,
            Integer nullField,
            Integer userIdField
    ) {
        this.notNullField = notNullField;
        this.notEmptyField = notEmptyField;
        this.nullField = nullField;
        this.userIdField = userIdField;
    }
}
