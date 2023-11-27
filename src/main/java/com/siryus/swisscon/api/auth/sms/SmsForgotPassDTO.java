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
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "A DTO used for passing information required when requesting a token")
public class SmsForgotPassDTO {
    @NotNull
    private String mobile;

    @NotNull
    public String token;

    @NotNull
    public String newPassword;
}