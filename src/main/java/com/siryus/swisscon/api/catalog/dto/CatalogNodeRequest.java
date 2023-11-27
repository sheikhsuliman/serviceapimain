package com.siryus.swisscon.api.catalog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeEntity;
import lombok.Builder;

import javax.validation.constraints.NotEmpty;

@Builder( toBuilder = true )
public class CatalogNodeRequest {

    @NotEmpty
    private final String snp;

    private final String name;

    @JsonCreator
    public CatalogNodeRequest(
            @JsonProperty("snp") String snp,
            @JsonProperty("name") String name
    ) {
        this.snp = snp;
        this.name = name;
    }

    public String getSnp() {
        return snp;
    }

    public String getName() {
        return name;
    }

    public static CatalogNodeRequest from(CatalogNodeEntity entity) {
        return CatalogNodeRequest
                .builder()
                .snp(entity.getSnp())
                .name(entity.getName())
                .build();
    }

}
