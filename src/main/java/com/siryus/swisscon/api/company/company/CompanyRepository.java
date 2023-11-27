package com.siryus.swisscon.api.company.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {

    @Query("select company from Company company where company.disabled = null and company.id > 0")
    List<Company> findAllActive();

    @Transactional
    @Modifying
    @Query("update Company company set company.disabled = current_timestamp where company.id = :companyId")
    void disable(@Param("companyId") Integer companyId);

}
