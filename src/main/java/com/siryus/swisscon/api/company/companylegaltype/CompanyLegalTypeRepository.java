package com.siryus.swisscon.api.company.companylegaltype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyLegalTypeRepository extends JpaRepository<CompanyLegalType, Integer> {
}
