package com.siryus.swisscon.api.company.companyuserrole;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CompanyUserRoleRepository extends JpaRepository<CompanyUserRole, Integer> {

    @Query("select companyUserRole from CompanyUserRole companyUserRole where companyUserRole.company.id = :companyId")
    List<CompanyUserRole> findCompanyUsersRoleByCompany(@Param("companyId") Integer companyId);

    @Query("select cur from CompanyUserRole cur where cur.company.id = :companyId and cur.user.id = :userId")
    List<CompanyUserRole> findByUserAndCompany(@Param("userId") Integer userId, @Param("companyId") Integer companyId);
    
    @Query("select companyUserRole from CompanyUserRole companyUserRole where companyUserRole.user.id = :userId")
    List<CompanyUserRole> findByUser(@Param("userId") Integer userId);

    @Query("select cur from CompanyUserRole cur where cur.role.name in (:roles) AND cur.company.id = :companyId")
    List<CompanyUserRole> findByCompanyAndRoles(@Param("companyId") Integer companyId, @Param("roles")  List<String> roles);

    @Transactional
    @Modifying
    @Query("delete from CompanyUserRole companyUserRole where companyUserRole.user.id = :userId")
    void deleteByUser(@Param("userId") Integer userId);

    @Query("select companyUserRole from CompanyUserRole companyUserRole where companyUserRole.company.id = :companyId and companyUserRole.user.id not in" +
            "(select projectUserRole.user.id from ProjectUserRole projectUserRole where projectUserRole.project.id = :projectId and projectUserRole.projectCompany.id = :projectCompanyId)")
    List<CompanyUserRole> findUsersWhichAreNotInProject(@Param("projectId") Integer projectId, @Param("companyId") Integer companyId, @Param("projectCompanyId") Integer projectCompanyId);

    @Transactional
    @Modifying
    @Query(
            nativeQuery = true,
            value = "DELETE FROM company_user_role WHERE user_id = :userId AND company = :companyId"
    )
    void deleteAllUserPermissionsForCompany(@Param("userId") Integer userId, @Param("companyId") Integer companyId);

    @Transactional
    @Modifying
    @Query(
            nativeQuery = true,
            value = "INSERT INTO company_user_role (company, user_id, role) SELECT :companyId, :userId, r.id FROM role r WHERE r.id in (:roleIds)"
    )
    void saveUserRolesForCompany(@Param("userId") Integer userId, @Param("companyId") Integer companyId, @Param("roleIds") List<Integer> roleIds);
}
