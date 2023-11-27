package com.siryus.swisscon.api.contract.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractLogRepository extends JpaRepository<ContractEventLogEntity, Integer> {

    default Optional<ContractEventLogEntity> findTopLogEntry( Integer contractId) {
        return findAllEventsForContract(contractId).stream().findFirst();
    }

    @Query(
            "SELECT l FROM ContractEventLogEntity l WHERE l.contractId = :contractId ORDER BY ID DESC"
    )
    List<ContractEventLogEntity> findAllEventsForContract(@Param("contractId") Integer contractId);

    @Query(
            "SELECT l FROM ContractEventLogEntity l WHERE l.primaryContractId = :primaryContractId ORDER BY ID DESC"
    )
    List<ContractEventLogEntity> findAllEventsForPrimaryContract(@Param("primaryContractId") Integer primaryContractId);
}
