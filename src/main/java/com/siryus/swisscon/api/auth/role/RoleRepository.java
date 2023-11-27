package com.siryus.swisscon.api.auth.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Query("select role from Role role where role.name = ?1")
    Role getRoleByName(String name);

    default Role getRoleByName(RoleName roleName) {
        return getRoleByName(roleName.name());
    }

    @Query(
            nativeQuery = true,
            // Note on query below: JPQL does not support subqueries in the FROM clause, and
            // null values in the parameters end up in the db as bytea, which can't be cast
            // directly into int, so this query looks a bit ugly. Also, double backslashes.
            //   - HF, 2020-01-31, SIR-629
            value = "SELECT r.* " +
                    "FROM (" +
                        "SELECT role FROM company_user_role cur " +
                        "WHERE cur.user_id = :userId AND cur.company = :companyId\\:\\:text\\:\\:int" +
                        " UNION " +
                        "SELECT role FROM project_user_role pur " +
                        "WHERE pur.user_id = :userId AND pur.project = :projectId\\:\\:text\\:\\:int" +
                    ") AS ur " +
                    "JOIN role r ON ur.role = r.id"
    )
    Set<Role> getUserRoles(
        @Param("userId") Integer userId,
        @Param("companyId") Integer companyId,
        @Param("projectId") Integer projectId
    );

    // Custom Role(s) ===================

    @Query(
            value = "SELECT r FROM Role r WHERE r.name = :roleName AND r.deprecated = false"
    )
    Optional<Role> findRoleByName(@Param("roleName") String roleName);

    default Optional<Role> findRoleByName(RoleName roleName) {
        return findRoleByName(roleName.name());
    }

    @Modifying
    @Query(
            value = "UPDATE Role SET memberDefault = false"
    )
    void clearMemberDefault();

    @Modifying
    @Query(
            value = "UPDATE Role SET ownerDefault = false"
    )
    void clearCompanyOwnerDefault();

    @Query(value = "SELECT r FROM Role r WHERE r.memberDefault = true and r.projectRole = false and r.deprecated = false")
    Optional<Role> findCompanyMemberDefaultRole();

    @Query(value = "SELECT r FROM Role r WHERE r.ownerDefault = true and r.projectRole = false and r.deprecated = false")
    Optional<Role> findCompanyOwnerDefaultRole();

}
