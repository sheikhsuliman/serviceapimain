package com.siryus.swisscon.api.company.companytrade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyTradeRepository extends JpaRepository<CompanyTrade, Integer> {

    @Query("select ct from CompanyTrade ct where ct.company = :companyId")
    List<CompanyTrade> findCompanyTradesByCompany( @Param("companyId") Integer companyId);

    @Query("SELECT DISTINCT ct.company FROM CompanyTrade ct WHERE ct.trade in (:trades)")
    List<Integer> listCompaniesWithGivenTrades( @Param("trades") List<Integer> trades);
}
