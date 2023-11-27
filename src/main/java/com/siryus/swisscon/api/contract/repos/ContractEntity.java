package com.siryus.swisscon.api.contract.repos;

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
import java.util.Optional;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Table(name = "contract")
@Entity
public class ContractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="project_id")
    private Integer projectId;

    @Column(name="primary_contract_id")
    private Integer primaryContractId;

    @Column(name = "contract_number")
    private Integer contractNumber;

    @CreatedBy
    @Column(name = "created_by")
    private Integer createdBy;

    @CreatedDate
    @Column(name = "created")
    private LocalDateTime createdDate;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private Integer lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified")
    private LocalDateTime lastModifiedDate;

    private LocalDateTime disabled;

    private String name;

    private String description;

    @Column(name="contractor_id")
    private Integer contractorId;

    @Column(name="customer_id")
    private Integer customerId;

    private LocalDateTime deadline;

    public static ContractEntity ref(Integer id) {
        return ContractEntity.builder().id(id).build();
    }

    public boolean isPrimaryContract() {
        return primaryContractId == null;
    }

    public Integer getPrimaryContractId() {
        return Optional.ofNullable(primaryContractId).orElse(id);
    }
}
