package com.siryus.swisscon.api.company.companyregion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRegionRepository extends JpaRepository<CompanyRegion, Integer> {
}
