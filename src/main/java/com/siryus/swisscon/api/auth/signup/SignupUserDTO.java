package com.siryus.swisscon.api.auth.signup;

import com.siryus.swisscon.api.auth.validator.ValidPassword;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SignupUserDTO {

    @NotEmpty
    private String title;

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @NotNull
    @Reference(value = ReferenceType.GENDER)
    private Integer genderId;

    @NotNull
    @Reference(value = ReferenceType.LANGUAGE)
    private String language;

    @Deprecated //TODO delete after SI-177 is done
    private String email;

    @NotEmpty
    @ValidPassword
    private String password;
}
