package com.siryus.swisscon.api.contract.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<ContractEntity, Integer> {

    @Query(
            "SELECT c FROM ContractEntity c WHERE c.projectId  = :projectId AND c.name = :contractName AND c.disabled is null"
    )
    Optional<ContractEntity> findContractByName(@Param("projectId") Integer projectId, @Param("contractName") String contractName);

    @Query(
            "SELECT c FROM ContractEntity c WHERE c.projectId  = :projectId AND c.primaryContractId is null AND c.disabled is null"
    )
    List<ContractEntity> findPrimaryContractsInProject(@Param("projectId") Integer projectId);

    @Query(
            "SELECT c FROM ContractEntity c WHERE c.projectId  = :projectId AND c.disabled is null"
    )
    List<ContractEntity> findAllContractsInProject(@Param("projectId") Integer projectId);

    @Query(
            "SELECT c FROM ContractEntity c WHERE c.primaryContractId  = :primaryContractId AND c.disabled is null"
    )
    List<ContractEntity> findAllPrimaryContractExtensions(@Param("primaryContractId") Integer primaryContractId);
}
