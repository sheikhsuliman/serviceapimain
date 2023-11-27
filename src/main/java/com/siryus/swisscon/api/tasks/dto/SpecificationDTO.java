package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.specification.Specification;

import java.math.BigDecimal;

public class SpecificationDTO {
    private final Integer id;
    private final Integer catalogItemId;
    private final String variation;
    private final BigDecimal amount;
    private final BigDecimal price;

    @JsonCreator
    public SpecificationDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("catalogItemId")  Integer catalogItemId,
            @JsonProperty("variation") String variation,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("price") BigDecimal price
    ) {
        this.id = id;
        this.catalogItemId = catalogItemId;
        this.variation = variation;
        this.amount = amount;
        this.price = price;
    }

    public static SpecificationDTO from(Specification specification) {
        if (specification == null) {
            return null;
        }
        return new SpecificationDTO(
                specification.getId(),
                specification.getCompanyCatalogVariation().getId(),
                specification.getVariation(),
                specification.getAmount(),
                specification.getPrice()
        );
    }

    public Integer getId() {
        return id;
    }

    public String getVariation() {
        return variation;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getCatalogItemId() {
        return catalogItemId;
    }
}
