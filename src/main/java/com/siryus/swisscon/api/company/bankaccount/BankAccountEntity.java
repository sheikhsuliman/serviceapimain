package com.siryus.swisscon.api.company.bankaccount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bank_account")
public class BankAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private Integer createdBy;

    @CreatedDate
    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedBy
    @Column(name = "last_modified_by", nullable = false)
    private Integer lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified", nullable = false)
    private LocalDateTime lastModifiedDate;

    private LocalDateTime disabled;

    @Column(name="company_id", nullable = false)
    private Integer companyId;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name="iban", nullable = false)
    private String iban;

    private String bic;

    @Column(name = "beneficiary_name")
    private String beneficiaryName;

    @Column(name = "currency_id", nullable = false)
    private String currencyId;

    public static BankAccountEntity from(Integer companyId, BankAccountDTO bankAccountDTO) {
        return BankAccountEntity.builder()
                .id(bankAccountDTO.getId())
                .companyId(companyId)
                .bankName(bankAccountDTO.getBankName())
                .beneficiaryName(bankAccountDTO.getBeneficiaryName())
                .iban(bankAccountDTO.getIban())
                .bic(bankAccountDTO.getBic())
                .currencyId(bankAccountDTO.getCurrencyId())
                .build();
    }

}
