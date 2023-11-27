package com.siryus.swisscon.api.init;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
public class FELogRequest {
    private final String level;
    private final String message;

    @JsonCreator
    public FELogRequest(
            @JsonProperty("level") String level,
            @JsonProperty("message") String message
    ) {
        this.level = level;
        this.message = message;
    }
}
