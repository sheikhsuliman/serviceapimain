package com.siryus.swisscon.api.project.projectcompanytrade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectCompanyTradeRepository extends JpaRepository<ProjectCompanyTrade, Integer> {
}
