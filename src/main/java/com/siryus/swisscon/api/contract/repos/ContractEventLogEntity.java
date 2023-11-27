package com.siryus.swisscon.api.contract.repos;

import com.siryus.swisscon.api.contract.dto.ContractEvent;
import com.siryus.swisscon.api.contract.dto.ContractState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "contract_event_log")
@Entity
public class ContractEventLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="project_id")
    private Integer projectId;

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

    @Column(name = "event")
    @Enumerated(EnumType.STRING)
    private ContractEvent event;

    @Column(name = "contract_state")
    @Enumerated(EnumType.STRING)
    private ContractState contractState;

    @Column(name="counter_part_id")
    private Integer counterPartId;
}