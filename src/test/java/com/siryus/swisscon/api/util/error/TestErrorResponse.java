package com.siryus.swisscon.api.util.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestErrorResponse {

    private String reason;
    private Integer errorCode;
    private Map<String, String> parameters;

}
