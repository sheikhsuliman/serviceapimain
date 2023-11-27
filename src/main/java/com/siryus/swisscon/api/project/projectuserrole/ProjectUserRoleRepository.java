package com.siryus.swisscon.api.project.projectuserrole;

import com.siryus.swisscon.api.auth.role.RoleName;
import com.siryus.swisscon.api.project.project.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectUserRoleRepository extends JpaRepository<ProjectUserRole, Integer> {

    @Query("select projectUserRole from ProjectUserRole projectUserRole where projectUserRole.project.id = :projectId and projectUserRole.disabled = null")
    List<ProjectUserRole> findProjectTeam(@Param("projectId") Integer projectId);

    @Query("select projectUserRole from ProjectUserRole projectUserRole where projectUserRole.projectCompany.id = :projectCompanyId and projectUserRole.disabled = null")
    List<ProjectUserRole> findProjectCompanyTeam(@Param("projectCompanyId") Integer projectCompanyId);

    @Query("select case when count(projectUserRole)>0 then true else false end from ProjectUserRole projectUserRole where projectUserRole.user.id = :userId")
    boolean existsByUser(@Param("userId") Integer userId);

    @Transactional
    @Modifying
    @Query("delete from ProjectUserRole where id = :id")
    void deleteWithId(@Param("id") Integer id);

    @Query("select projectUserRole from ProjectUserRole projectUserRole where projectUserRole.project.id = :projectId and projectUserRole.user.id in (:userIds) and projectUserRole.disabled = null")
    List<ProjectUserRole> findByProjectAndUsers(@Param("projectId") Integer projectId, @Param("userIds") List<Integer> userIds);

    @Query("update ProjectUserRole role set role.disabled = current_timestamp where user.id = :id")
    void disable(@Param("id") Integer id);

    @Query("select pur from ProjectUserRole pur where pur.role.name in (:projectOwnerRoles) AND pur.user.id = :userId")
    List<ProjectUserRole> findProjectOwnerRoles(@Param("userId") Integer userId, @Param("projectOwnerRoles")  List<String> projectOwnerRoles);

    default List<ProjectUserRole> findProjectOwnerRoles(Integer userId ) {
        return findProjectOwnerRoles(userId, Arrays.asList(RoleName.PROJECT_OWNER.name(), RoleName.PROJECT_CUSTOMER.name()));
    }

    default List<ProjectUserRole> findProjectUsersWithRole(Integer projectId, String roleName ) {
        return findProjectUsersWithRoles(projectId, Collections.singletonList(roleName));
    }

    @Query("select pur from ProjectUserRole pur where pur.role.name in (:roleNames) AND pur.project.id = :projectId")
    List<ProjectUserRole> findProjectUsersWithRoles(@Param("projectId") Integer projectId, @Param("roleNames")  List<String> roleNames);

    default Optional<ProjectUserRole> findProjectOwner(Integer projectId) {
        return findProjectUsersWithRoles(projectId, Collections.singletonList(RoleName.PROJECT_OWNER.name())).stream().findFirst()
                .or(() -> findProjectUsersWithRoles(projectId, Collections.singletonList(RoleName.PROJECT_CUSTOMER.name())).stream().findFirst());
    }

    @Query("select projectUserRole from ProjectUserRole projectUserRole")
    List<ProjectUserRole> findAllIncludeDisabled();

    @Query("select projectUserRole from ProjectUserRole projectUserRole where projectUserRole.projectCompany.id = :projectCompanyId and projectUserRole.disabled = null")
    List<ProjectUserRole> findByProjectCompany(@Param("projectCompanyId") Integer projectCompanyId);

    @Query("select pur from ProjectUserRole pur where pur.user.id = :userId " +
            "and pur.projectCompany.id in (select pc.id from ProjectCompany pc where pc.company.id = :companyId)                                                                                                        ")
    List<ProjectUserRole> findByCompanyAndUser(@Param("companyId") Integer companyId,
                                               @Param("userId") Integer userId);

    // Custom Roles ----------------

    @Query("SELECT pur FROM ProjectUserRole pur where pur.project.id = :projectId and pur.user.id = :userId")
    List<ProjectUserRole> findByUserAndProject(@Param("userId") Integer userId, @Param("projectId") Integer projectId);

    @Transactional
    @Modifying
    @Query(
            nativeQuery = true,
            value = "DELETE FROM project_user_role WHERE user_id = :userId AND project_company = :projectCompanyId AND project = :projectId"
    )
    void deleteAllUserRolesForProject(@Param("userId") Integer userId, @Param("projectId") Integer projectId, @Param("projectCompanyId") Integer projectCompanyId);

    @Transactional
    @Modifying
    @Query(
            nativeQuery = true,
            value = "INSERT INTO project_user_role (project, project_company, user_id, role) SELECT :projectId, :projectCompanyId, :userId, r.id FROM role r WHERE r.id in (:roleIds)"
    )
    void saveUserRolesForProject(@Param("userId") Integer userId, @Param("projectId") Integer projectId, @Param("projectCompanyId") Integer projectCompanyId, @Param("roleIds") List<Integer> roleIds);

    @Query("SELECT p FROM Project p WHERE p.id in ( SELECT pur.project.id FROM ProjectUserRole pur WHERE pur.user.id = :userId) and p.disabled = null")
    Page<Project> findActiveProjectsUserAssociatedWith(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.id in ( SELECT pur.project.id FROM ProjectUserRole pur WHERE pur.user.id = :userId) and p.disabled is not null")
    Page<Project> findArchivedProjectsUserAssociatedWith(@Param("userId") Integer userId, Pageable pageable);

    @Transactional
    @Modifying
    @Query(
            "delete from ProjectUserRole where project.id = :projectId AND role.id = :roleId"
    )
    void deleteAllUsersWithRoleFromProject(@Param("projectId") Integer projectId, @Param("roleId") Integer roleId);
}
