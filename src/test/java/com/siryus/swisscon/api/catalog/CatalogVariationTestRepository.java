package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface CatalogVariationTestRepository extends JpaRepository<CatalogVariationEntity, Integer> {

    @Modifying
    @Transactional
    @Query("DELETE FROM CatalogVariationEntity variation WHERE variation.snp like :partialSnp%")
    void deleteAllWhichIdsStartsWith(@Param("partialSnp") String partialSnp);

    @Query(
            nativeQuery = true,
            value = "SELECT a.* from catalog_variation a INNER JOIN " +
                    "(SELECT MAX(n.id) as id FROM catalog_variation n " +
                    "WHERE n.snp = :snp AND n.variation_number = :variationNumber AND ( n.company_id = 0 OR n.company_id = :companyId ) " +
                    "GROUP BY n.snp)b ON a.id = b.id"
    )
    Optional<CatalogVariationEntity> findLatestBySnpAndVariationNumber(@Param("snp") String snp, @Param("variationNumber") Integer variationNumber, @Param("companyId") Integer companyId);

}
