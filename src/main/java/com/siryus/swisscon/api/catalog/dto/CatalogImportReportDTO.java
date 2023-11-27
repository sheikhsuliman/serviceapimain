package com.siryus.swisscon.api.catalog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class CatalogImportReportDTO {

    private final Integer lines;
    private final Integer editedNodes;
    private final Integer editedLeafNodes;
    private final Integer addedNodes;
    private final Integer addedLeafNodes;

    @JsonCreator
    public CatalogImportReportDTO(@JsonProperty("lines") Integer lines,
                                  @JsonProperty("editedNodes") Integer editedNodes,
                                  @JsonProperty("editedLeafNodes") Integer editedLeafNodes,
                                  @JsonProperty("addedNodes") Integer addedNodes,
                                  @JsonProperty("addedLeafNodes") Integer addedLeafNodes) {
        this.lines = lines;
        this.editedNodes = editedNodes;
        this.editedLeafNodes = editedLeafNodes;
        this.addedNodes = addedNodes;
        this.addedLeafNodes = addedLeafNodes;
    }

}
