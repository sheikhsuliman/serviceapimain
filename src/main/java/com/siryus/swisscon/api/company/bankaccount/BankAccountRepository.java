package com.siryus.swisscon.api.company.bankaccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccountEntity, Integer> {

    /** magic **/
    List<BankAccountEntity> findAllByCompanyId(Integer companyId);

}
