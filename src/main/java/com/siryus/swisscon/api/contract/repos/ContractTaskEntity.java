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
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Table(name = "contract_task")
@Entity
public class ContractTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="contract_id")
    private Integer contractId;

    @Column(name="primary_contract_id")
    private Integer primaryContractId;

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

    @Column(name="task_id")
    private Integer taskId;

    @Column(name="project_id")
    private Integer projectId;

    @Column(name="unit_id")
    private Integer unitId;

    @Column(name = "price_per_unit")
    private BigDecimal pricePerUnit;

    @Column(name="amount")
    private BigDecimal amount;

    @Column(name="price")
    private BigDecimal price;

    @Column(name="negated_contract_task_id")
    private Integer negatedContractTaskId;

    public static ContractTaskEntity ref(Integer id) {
        return ContractTaskEntity.builder().id(id).build();
    }
}
