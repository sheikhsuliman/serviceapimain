package com.siryus.swisscon.api.auth.signup;

import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder=true)
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SignupCompanyDTO {

    //@NotNull //TODO after signup in FE is migrated activate @NotNull (SI-177)
    private Integer companyId;

    @NotEmpty
    private String name;

    @NotNull
    @Reference(value = ReferenceType.COUNTRY)
    private Integer countryId;

    @NotNull
    private List<Integer> tradeIds;

    @NotNull
    @Reference(value = ReferenceType.NUM_WORKERS_OF_COMPANY)
    private Integer numberOfEmployeesId;
}

