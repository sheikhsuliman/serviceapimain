package com.siryus.swisscon.api.auth.signup;

import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import io.swagger.annotations.ApiModel;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@EqualsAndHashCode
@Builder(toBuilder=true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "A DTO used for signup a user and a company")
public class SignupDTO {

    @Valid
    private SignupCompanyDTO company;

    @Valid
    private SignupUserDTO user;

    @Deprecated //TODO remove after SI-177
    private String linkEmailOrPhone;

    @NotNull
    private String linkCode;

    @NotNull
    private boolean optOutPromo;

    @NotNull
    @AssertTrue
    private boolean acceptConditions;

    public static SignupDTO from(CompanyUserRole companyUserRole, String code) {
        return SignupDTO.builder()
                .company(SignupCompanyDTO.builder()
                        .name(companyUserRole.getCompany().getName())
                        .companyId(companyUserRole.getCompany().getId())
                        .build())
                .user(SignupUserDTO.builder()
                        .language(companyUserRole.getUser().getPrefLang().getId())
                        .build())
                .linkCode(code)
                .build();
    }

}
