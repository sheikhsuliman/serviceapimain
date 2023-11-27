package com.siryus.swisscon.api.catalog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Builder( toBuilder = true )
public class CatalogVariationsRequest {
    @NotEmpty
    private final String snp;
    private final List<CatalogVariationDTO> variations;

    @JsonCreator
    public CatalogVariationsRequest(
            @JsonProperty("snp") String snp,
            @JsonProperty("variations") List<CatalogVariationDTO> variations
    ) {
        this.snp = snp;
        this.variations = variations;
    }

    public String getSnp() {
        return snp;
    }

    public List<CatalogVariationDTO> getVariations() {
        return variations;
    }
}
