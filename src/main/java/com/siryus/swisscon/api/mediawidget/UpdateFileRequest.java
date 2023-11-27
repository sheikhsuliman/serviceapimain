package com.siryus.swisscon.api.mediawidget;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class UpdateFileRequest {
    @NotNull
    public Integer nodeId;

    @NotNull
    @NotBlank
    public String name;

    public UpdateFileRequest() {
    }

    public UpdateFileRequest(Integer nodeId, String name) {
        this.nodeId = nodeId;
        this.name = name;
    }
}
