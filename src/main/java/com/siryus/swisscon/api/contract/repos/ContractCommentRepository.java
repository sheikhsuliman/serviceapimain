package com.siryus.swisscon.api.contract.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ContractCommentRepository extends JpaRepository<ContractCommentEntity, Integer> {
    
    @Query("SELECT comment FROM ContractCommentEntity comment WHERE comment.contractId = :contractId AND comment.disabled = NULL ORDER BY comment.createdDate DESC")
    List<ContractCommentEntity> findByContractId(@Param("contractId") Integer contractId);

    @Query("SELECT comment FROM ContractCommentEntity comment WHERE comment.primaryContractId = :primaryContractId AND comment.disabled = NULL ORDER BY comment.createdDate DESC")
    List<ContractCommentEntity> findByPrimaryContractId(@Param("primaryContractId") Integer primaryContractId);

    @Transactional
    @Modifying
    @Query("UPDATE ContractCommentEntity comment SET comment.disabled = current_timestamp WHERE comment.id = :commentId")
    void disable(@Param("commentId") Integer commentId);
}
