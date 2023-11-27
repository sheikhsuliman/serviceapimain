package com.siryus.swisscon.api.company.bankaccount;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.siryus.swisscon.api.general.reference.ReferenceType;
import com.siryus.swisscon.api.util.validator.Reference;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Builder (toBuilder = true)
@Getter
public class BankAccountDTO {

    @Reference(ReferenceType.BANK_ACCOUNT)
    private Integer id;

    private String bankName;

    @NotEmpty
    private String iban;

    private String bic;

    @NotEmpty
    private String beneficiaryName;

    @NotNull
    @Reference(ReferenceType.CURRENCY)
    private String currencyId;

    @JsonCreator
    public BankAccountDTO(
            @JsonProperty("id") Integer id,
            @JsonProperty("bankName") String bankName,
            @JsonProperty("iban") String iban,
            @JsonProperty("bic") String bic,
            @JsonProperty("beneficiaryName") String beneficiaryName,
            @JsonProperty("currencyId") String currencyId) {
        this.id = id;
        this.bankName = bankName;
        this.iban = iban;
        this.bic = bic;
        this.beneficiaryName = beneficiaryName;
        this.currencyId = currencyId;
    }

    public static BankAccountDTO from(BankAccountEntity bankAccountEntity) {
        return BankAccountDTO.builder()
                .id(bankAccountEntity.getId())
                .bankName(bankAccountEntity.getBankName())
                .iban(bankAccountEntity.getIban())
                .bic(bankAccountEntity.getBic())
                .beneficiaryName(bankAccountEntity.getBeneficiaryName())
                .currencyId(bankAccountEntity.getCurrencyId())
                .build();
    }

}
