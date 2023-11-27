package com.siryus.swisscon.api.contract.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.contract.repos.ContractTaskEntity;
import com.siryus.swisscon.api.general.unit.UnitDTO;
import com.siryus.swisscon.api.tasks.dto.MainTaskDTO;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
public class ContractTaskDTO {
    private final Integer contractId;
    private final Integer primaryContractId;
    private final LocalDateTime disabled;
    private final Integer contractTaskId;
    private final MainTaskDTO task;
    private final UnitDTO unit;
    private final BigDecimal pricePerUnit;
    private final BigDecimal amount;
    private final BigDecimal price;
    private final Integer negatedContractTaskId;

    @JsonCreator
    public ContractTaskDTO(
            @JsonProperty("contractId") Integer contractId,
            @JsonProperty("primaryContractId") Integer primaryContractId,
            @JsonProperty("disabled") LocalDateTime disabled,
            @JsonProperty("contractTaskId") Integer contractTaskId,
            @JsonProperty("task") MainTaskDTO task,
            @JsonProperty("unit") UnitDTO unit,
            @JsonProperty("pricePerUnit") BigDecimal pricePerUnit,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("negatedContractTaskId") Integer negatedContractTaskId
    ) {
        this.contractId = contractId;
        this.primaryContractId = primaryContractId;
        this.disabled = disabled;
        this.contractTaskId = contractTaskId;
        this.task = task;
        this.unit = unit;
        this.pricePerUnit = pricePerUnit;
        this.amount = amount;
        this.price = price;
        this.negatedContractTaskId = negatedContractTaskId;
    }


    public static ContractTaskDTO from(ContractTaskEntity contractTask, MainTaskDTO mainTaskDTO, UnitDTO unitDTO) {
        return new ContractTaskDTO(contractTask.getContractId(),
                                   contractTask.getPrimaryContractId(),
                                   contractTask.getDisabled(),
                                   contractTask.getId(),
                                   mainTaskDTO,
                                   unitDTO,
                                   contractTask.getPricePerUnit(),
                                   contractTask.getAmount(),
                                   calculatePrice(contractTask.getPricePerUnit(), contractTask.getAmount()),
                                   contractTask.getNegatedContractTaskId()
        );
    }

    private static BigDecimal calculatePrice(BigDecimal pricePerUnit, BigDecimal amount) {
        return ((pricePerUnit == null) || (amount == null))
                ? null
                : pricePerUnit.multiply(amount).setScale(pricePerUnit.scale(), RoundingMode.HALF_EVEN);
    }
}
