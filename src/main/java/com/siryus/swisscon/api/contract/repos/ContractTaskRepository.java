package com.siryus.swisscon.api.contract.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public interface ContractTaskRepository extends JpaRepository<ContractTaskEntity, Integer> {

    @Query("SELECT ct FROM ContractTaskEntity ct WHERE ct.contractId = :contractId AND ct.disabled IS NULL")
    List<ContractTaskEntity> findByContract(@Param("contractId") Integer contractId);

    @Query("SELECT ct FROM ContractTaskEntity ct WHERE ct.taskId = :taskId AND ct.disabled IS NULL")
    List<ContractTaskEntity> findByTask(@Param("taskId") Integer taskId);

    @Query("SELECT ct FROM ContractTaskEntity ct WHERE ct.contractId IN :contractIds AND ct.disabled IS NULL ORDER BY ct.id ASC")
    List<ContractTaskEntity> findInContracts(@Param("contractIds") List<Integer> contractIds);


    @Query("SELECT ct FROM ContractTaskEntity ct WHERE ct.primaryContractId = :primaryContractId AND ct.disabled IS NULL ORDER BY ct.contractId ASC")
    List<ContractTaskEntity> findByPrimaryContract(@Param("primaryContractId") Integer primaryContractId);

    @Transactional
    @Modifying
    @Query("update ContractTaskEntity ct set ct.disabled = current_timestamp where ct.id = :contractTaskId")
    void disable(@Param("contractTaskId") Integer contractTaskId);

    default List<Integer> findTaskIdsByContract(Integer contractId) {
        return this.findByContract(contractId).stream()
                .map(ContractTaskEntity::getTaskId)
                .collect(Collectors.toList());
    }

    default List<Integer> findContractIdsByTask(Integer taskId) {
        return this.findByTask(taskId).stream()
                .map(ContractTaskEntity::getContractId)
                .collect(Collectors.toList());
    }

}
