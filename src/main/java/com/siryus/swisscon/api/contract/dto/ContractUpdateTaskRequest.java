package com.siryus.swisscon.api.contract.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Builder(toBuilder = true)
@Getter
public class ContractUpdateTaskRequest {

    @Reference(ReferenceType.CONTRACT_TASK)
    @NotNull
    private final Integer contractTaskId;

    @Reference(ReferenceType.UNIT)
    @NotNull
    private final Integer unitId;

    private final BigDecimal pricePerUnit;

    private final BigDecimal amount;

    @JsonCreator
    public ContractUpdateTaskRequest(
            @JsonProperty("contractTaskId") Integer contractTaskId,
            @JsonProperty("unitId") Integer unitId,
            @JsonProperty("pricePerUnit") BigDecimal pricePerUnit,
            @JsonProperty("amount") BigDecimal amount
    ) {
        this.contractTaskId = contractTaskId;
        this.unitId = unitId;
        this.pricePerUnit = pricePerUnit;
        this.amount = amount;
    }

}
