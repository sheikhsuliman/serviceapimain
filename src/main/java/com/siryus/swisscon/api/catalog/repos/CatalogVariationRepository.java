package com.siryus.swisscon.api.catalog.repos;

import com.siryus.swisscon.api.catalog.dto.VariationNumberAndId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface CatalogVariationRepository extends JpaRepository<CatalogVariationEntity, Integer> {

    @Query(
            value = "SELECT new com.siryus.swisscon.api.catalog.dto.VariationNumberAndId(n.variationNumber, MAX(n.id)) FROM CatalogVariationEntity n " +
                    "WHERE n.catalogNodeId = :catalogNodeId AND n.companyId = :companyId " +
                    "GROUP BY n.snp ,n.variationNumber"
    )
    List<VariationNumberAndId> listLatestVariationsForCompanyAndGlobalCatalogNode(@Param("companyId") Integer companyId, @Param("catalogNodeId") Integer catalogNodeId);

    @Query(
            nativeQuery = true,
            value = "SELECT MAX(n.id) as id FROM catalog_variation n " +
                    "WHERE n.snp in :snps AND n.company_id = :companyId " +
                    "GROUP BY n.snp ,n.variation_number")
    List<Integer> findLatestInSnps(@Param("snps") List<String> snps, @Param("companyId") Integer companyId);


    default Integer countLatestVariations(Integer companyId, Integer catalogNodeId) {
        return listLatestVariationsForCompanyAndGlobalCatalogNode(companyId, catalogNodeId).size();
    }

    default  List<CatalogVariationEntity> listLatestVariations(Integer companyId, Integer catalogNodeId) {
        return findAllById(
                listLatestVariationsForCompanyAndGlobalCatalogNode(
                        companyId, catalogNodeId
                )
                        .stream()
                        .map(VariationNumberAndId::getId)
                        .collect(Collectors.toList())
        );
    }

    default Optional<CatalogVariationEntity> findVariation(
            Integer companyId,
            Integer catalogNodeId,
            Integer variationNumber
    ) {
        return listLatestVariations(companyId, catalogNodeId).stream()
                .filter( v -> v.getVariationNumber().equals(variationNumber))
                .findFirst();
    }
}
