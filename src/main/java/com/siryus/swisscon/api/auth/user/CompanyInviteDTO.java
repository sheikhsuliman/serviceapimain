package com.siryus.swisscon.api.auth.user;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "A DTO used for inviting companies")
public class CompanyInviteDTO {

    @NotNull
    private String emailOrPhone;

    @NotNull
    @NotEmpty
    private String companyName;

    private Integer countryCode;

    private Integer roleId;

}
