package com.siryus.swisscon.api.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Builder
public class CreateSpecificationRequest {
    @NotNull
    private final String variation;

    @NotNull
    @PositiveOrZero
    private final BigDecimal amount;

    private final BigDecimal price;

    @JsonCreator
    public CreateSpecificationRequest(
            @JsonProperty("variation") String variation,
            @JsonProperty("amount")  BigDecimal amount,
            @JsonProperty("price")  BigDecimal price
    ) {
        this.variation = variation == null ? "" : variation;
        this.amount = amount;
        this.price = price;
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
}
