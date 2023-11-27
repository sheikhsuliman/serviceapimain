package com.siryus.swisscon.api.auth.user;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import io.swagger.annotations.ApiModel;
import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "A DTO used for adding new users to a company team")
public class TeamUserAddDTO {

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @NotEmpty
    private String emailOrPhone;

    private Integer countryCode;

    @Reference(ReferenceType.ROLE)
    private Integer roleId;

}
