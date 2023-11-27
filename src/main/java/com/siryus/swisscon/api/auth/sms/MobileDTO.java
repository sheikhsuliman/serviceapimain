package com.siryus.swisscon.api.auth.sms;

import io.swagger.annotations.ApiModel;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@Builder(toBuilder=true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "A DTO used for passing information required when requesting a token")
public class MobileDTO {
    @NotNull
    String mobile;
    
    // TODO: promote this to @Positive in SIR-1542
    Integer countryCode;  
}
