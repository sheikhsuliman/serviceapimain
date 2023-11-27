package com.siryus.swisscon.api.auth.signup;

import com.siryus.swisscon.api.company.companyuserrole.CompanyUserRole;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder=true)
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponseDTO {

    private Integer userId;
    private Integer companyId;

    public static SignupResponseDTO from(CompanyUserRole companyUserRole) {
        return SignupResponseDTO
                .builder()
                .companyId(companyUserRole.getCompany().getId())
                .userId(companyUserRole.getUser().getId())
                .build();
    }
}
