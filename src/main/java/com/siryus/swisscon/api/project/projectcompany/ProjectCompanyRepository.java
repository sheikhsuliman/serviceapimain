package com.siryus.swisscon.api.project.projectcompany;

import com.siryus.swisscon.api.company.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectCompanyRepository extends JpaRepository<ProjectCompany, Integer> {
    @Query("select projectCompany from ProjectCompany projectCompany where projectCompany.project.id = :projectId and projectCompany.company.id = :companyId and projectCompany.disabled = null")
    ProjectCompany findByProjectAndCompany(@Param("projectId") Integer projectId,
                                           @Param("companyId") Integer companyId);

    @Query("select projectCompany from ProjectCompany projectCompany where projectCompany.project.id = :projectId and projectCompany.disabled = null")
    List<ProjectCompany> findActiveByProject(@Param("projectId") Integer projectId);

    @Query("select projectCompany from ProjectCompany projectCompany where projectCompany.company.id = :companyId and projectCompany.disabled = null")
    List<ProjectCompany> findActiveByCompany(@Param("companyId") Integer companyId);

    @Query("select c from Company c where c.id not in  (" +
            "select pc.company.id from ProjectCompany pc where pc.project.id = :projectId and pc.disabled = null) and id > 0")
    List<Company> findCompaniesNotInProject(@Param("projectId") Integer projectId);
}
