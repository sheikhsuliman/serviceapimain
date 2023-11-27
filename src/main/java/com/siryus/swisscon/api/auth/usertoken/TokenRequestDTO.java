package com.siryus.swisscon.api.auth.usertoken;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@Builder(toBuilder=true)
@AllArgsConstructor
@ApiModel(description = "A DTO used for passing information required when requesting a token")
public class TokenRequestDTO {
    private final String userIdentifier;    
    
    private final String ip;
}
