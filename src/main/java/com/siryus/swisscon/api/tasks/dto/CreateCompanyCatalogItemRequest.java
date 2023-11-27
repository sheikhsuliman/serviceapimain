package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import lombok.Builder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Builder
public class CreateCompanyCatalogItemRequest {
    @NotNull
    private String snpNumber;

    @NotNull
    private final Integer variationNumber;

    @NotNull
    @Reference(ReferenceType.COMPANY)
    private final Integer companyId;

    @NotNull
    private final String companyDetails;

    @NotNull
    private final BigDecimal price;

    @JsonCreator
    public CreateCompanyCatalogItemRequest(
            @JsonProperty("snpNumber") String snpNumber,
            @JsonProperty("variationNumber") Integer variationNumber,
            @JsonProperty("companyId")  Integer companyId,
            @JsonProperty("companyDetails")  String companyDetails,
            @JsonProperty("price")  BigDecimal price
    ) {
        this.snpNumber = snpNumber;
        this.variationNumber = variationNumber;
        this.companyId = companyId;
        this.companyDetails = companyDetails == null ? "" : companyDetails;
        this.price = price;
    }

    public String getSnpNumber() {
        return snpNumber;
    }

    public Integer getVariationNumber() {
        return variationNumber;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public String getCompanyDetails() {
        return companyDetails;
    }

    public BigDecimal getPrice() {
        return price;
    }

}
