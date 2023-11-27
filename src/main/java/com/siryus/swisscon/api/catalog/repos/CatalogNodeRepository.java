package com.siryus.swisscon.api.catalog.repos;

import com.siryus.swisscon.api.catalog.Constants;
import com.siryus.swisscon.api.catalog.dto.SnpAndId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogNodeRepository extends JpaRepository<CatalogNodeEntity, Integer> {

    @Query(
            nativeQuery = true,
            value = "SELECT MAX(n.id) as id FROM global_catalog_node n " +
                    "WHERE n.company_id = :companyId " +
                    "GROUP BY n.snp"
    )
    List<Integer> listAllLatest(@Param("companyId") Integer companyId);

    @Query(
            value = "SELECT new com.siryus.swisscon.api.catalog.dto.SnpAndId(n.snp, MAX(n.id)) FROM CatalogNodeEntity n " +
                    "WHERE n.parentSnp is null AND n.companyId = :companyId AND n.disabled is null " +
                    "GROUP BY n.snp"
    )
    List<SnpAndId> listLatestIdsOfRootNodesForCompanyNotDisabled(@Param("companyId") Integer companyId);

    @Query(
            value = "SELECT new com.siryus.swisscon.api.catalog.dto.SnpAndId(n.snp, MAX(n.id)) FROM CatalogNodeEntity n " +
                    "WHERE n.parentSnp = :parentSnp AND n.companyId = :companyId AND n.disabled is null " +
                    "GROUP BY n.snp"
    )
    List<SnpAndId> listLatestIdsOfChildNodesForCompanyNotDisabled(@Param("companyId") Integer companyId, @Param("parentSnp") String parentSnp);

    @Query(
            value = "SELECT new com.siryus.swisscon.api.catalog.dto.SnpAndId(n.snp, MAX(n.id)) FROM CatalogNodeEntity n " +
                    "WHERE n.parentSnp is null AND n.companyId = :companyId " +
                    "GROUP BY n.snp"
    )
    List<SnpAndId> listLatestIdsOfRootNodesForCompany(@Param("companyId") Integer companyId);

    @Query(
            value = "SELECT new com.siryus.swisscon.api.catalog.dto.SnpAndId(n.snp, MAX(n.id)) FROM CatalogNodeEntity n " +
                    "WHERE n.parentSnp = :parentSnp AND n.companyId = :companyId " +
                    "GROUP BY n.snp"
    )
    List<SnpAndId> listLatestIdsOfChildNodesForCompany(@Param("companyId") Integer companyId, @Param("parentSnp") String parentSnp);

    @Query(
            nativeQuery = true,
            value = "SELECT MAX(n.id) as id FROM global_catalog_node n " +
                    "WHERE n.snp like :snp || '%' AND ( n.company_id = 0 OR n.company_id = :companyId ) " +
                    "GROUP BY n.snp"
    )
    List<Integer> listLatestStartsWith(@Param("companyId") Integer companyId, @Param("snp") String snp);

    @Query(
            nativeQuery = true,
            value = "SELECT n.* FROM global_catalog_node n WHERE n.snp = :snp and n.company_id = :companyId ORDER BY n.id DESC LIMIT 1"
    )
    Optional<CatalogNodeEntity> findLatestWithSnp(@Param("companyId") Integer companyId, @Param("snp") String snp);

    default Optional<CatalogNodeEntity> findLatestWithSnp(String snp) {
        return findLatestWithSnp(Constants.GLOBAL_CATALOG_COMPANY_ID, snp);
    }

    @Query(
            nativeQuery = true,
            value = "SELECT MAX(n.id) as id FROM global_catalog_node n " +
                    "WHERE n.snp in :snps AND n.company_id = :companyId " +
                    "GROUP BY n.snp")
    List<Integer> findLatestInSnps(@Param("snps") List<String> snps, @Param("companyId") Integer companyId);

    // Magic
    List<CatalogNodeEntity> findByIdInAndDisabledIsNull(List<Integer> ids);

    List<CatalogNodeEntity> findByIdIn(List<Integer> ids);
}
