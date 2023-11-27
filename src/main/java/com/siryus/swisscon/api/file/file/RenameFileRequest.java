package com.siryus.swisscon.api.file.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;

@Getter
public class RenameFileRequest {
    @NotEmpty
    private final String newFileName;

    @JsonCreator
    public RenameFileRequest(
            @JsonProperty("newFileName") String newFileName
    ) {
        this.newFileName = newFileName;
    }
}
