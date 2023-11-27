package com.siryus.swisscon.api.company.numworkersofcompany;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NumWorkersOfCompanyRepository extends JpaRepository<NumWorkersOfCompany, Integer> {

    @Query("SELECT num FROM NumWorkersOfCompany num WHERE minWorkers <= ?1 AND maxWorkers >= ?1")
    NumWorkersOfCompany findBySize(Integer size);

}
