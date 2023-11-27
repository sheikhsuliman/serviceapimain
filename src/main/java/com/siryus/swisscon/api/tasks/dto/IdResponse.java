package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IdResponse {

    private final Integer id;

    @JsonCreator
    public IdResponse(
            @JsonProperty("id") Integer id
    ) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
