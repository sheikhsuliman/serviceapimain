package com.siryus.swisscon.api.auth.permission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    @Query("select rp.permission from ProjectUserRole pur join RolePermission rp on pur.role = rp.role where pur.user.id = :userId and pur.project.id = :projectId")
    List<Permission> findByUserAndProject(@Param("userId") Integer userId, @Param("projectId") Integer projectId);

    @Query("select rp.permission from CompanyUserRole cur join RolePermission rp on cur.role = rp.role where cur.user.id = :userId and cur.company.id = :companyId")
    List<Permission> findByUserAndCompany(@Param("userId") Integer userId, @Param("companyId") Integer companyId);

    @Query(
            nativeQuery = true,
            value = "SELECT DISTINCT p.* " +
                    "FROM role r " +
                    "JOIN role_permission rp ON r.id = rp.role_id " +
                    "JOIN permission p ON rp.permission_id = p.id " +
                    "WHERE r.name IN(?1)"
    )
    Set<Permission> getPermissionsByRoleNames(Set<String> roleNames);

    @Query("SELECT p FROM Permission p WHERE p.name = :permissionName")
    Permission getPermissionByName(@Param("permissionName") String permissionName);

    // Custom Role(s) =====================

    @Query(
            value = "SELECT p FROM Permission p " +
                    "LEFT JOIN RolePermission rp ON (p.id = rp.permission.id ) " +
                    "WHERE rp.role.id = :roleId"
    )
    List<Permission> findPermissionsByRole(@Param("roleId") Integer roleId);

    @Transactional
    @Modifying
    @Query(
            nativeQuery = true,
            value = "DELETE FROM role_permission WHERE role_id = :roleId"
    )
    void deleteAllRolePermissions(@Param("roleId") Integer roleId);

    @Transactional
    @Modifying
    @Query(
            nativeQuery = true,
            value = "INSERT INTO role_permission (role_id, permission_id) SELECT :roleId, p.id FROM permission p WHERE p.id in (:permissionIds)"
    )
    void saveRolePermissions(@Param("roleId") Integer roleId, @Param("permissionIds") List<Integer> permissionIds);

    default boolean isRoleAdmin(@Param("roleId") Integer roleId) {
        return findPermissionsByRole(roleId).stream().anyMatch(Permission::isAdminPermission);
    }
}
