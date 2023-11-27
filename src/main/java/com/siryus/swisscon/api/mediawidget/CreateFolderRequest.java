package com.siryus.swisscon.api.mediawidget;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateFolderRequest {
    public Integer parentNodeId;

    @NotNull
    @NotBlank
    public String name;

    public CreateFolderRequest() {
    }

    public CreateFolderRequest(Integer parentNodeId, String name) {
        this.parentNodeId = parentNodeId;
        this.name = name;
    }
}
