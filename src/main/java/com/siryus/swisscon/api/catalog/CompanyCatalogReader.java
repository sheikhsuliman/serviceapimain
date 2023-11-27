package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.dto.CatalogNodeDTO;
import com.siryus.swisscon.api.catalog.dto.SnpAndId;
import com.siryus.swisscon.api.catalog.dto.VariationNumberAndId;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeRepository;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
class CompanyCatalogReader {
    private final CatalogNodeRepository nodeRepository;
    private final CatalogVariationRepository variationRepository;
    private final CatalogValidator validator;

    @Autowired
    CompanyCatalogReader(
            CatalogNodeRepository nodeRepository,
            CatalogVariationRepository variationRepository,
            CatalogValidator validator) {
        this.nodeRepository = nodeRepository;
        this.variationRepository = variationRepository;
        this.validator = validator;
    }

    List<CatalogNodeDTO> listRoots(Integer companyId) {
        return nodeRepository.findByIdIn(
                SnpAndId.combine(
                        nodeRepository.listLatestIdsOfRootNodesForCompany(companyId),
                        nodeRepository.listLatestIdsOfRootNodesForCompanyNotDisabled(Constants.GLOBAL_CATALOG_COMPANY_ID)
                )

        )
                .stream()
                .filter(n -> ( ! n.getCompanyId().equals(Constants.GLOBAL_CATALOG_COMPANY_ID)) || ( n.getDisabled() == null ))
                .map( n -> CatalogNodeDTO.from(n,validator.isNodeLeaf(n)))
                .collect(Collectors.toList());
    }

    List<CatalogNodeDTO> listChildren(Integer companyId, String parentSnp) {
        validator.validateNodeAndAllAncestorsExists(companyId, parentSnp);

        return findLatestChildNodesForCompany(companyId, validateSnp(parentSnp))
                .stream()
                .filter( n -> ( ! n.getCompanyId().equals(Constants.GLOBAL_CATALOG_COMPANY_ID)) || ( n.getDisabled() == null ))
                .map(n -> catalogNodeEntityToDTO(companyId, n))
                .collect(Collectors.toList());
    }

    List<CatalogNodeDTO> expand(Integer companyId, String snp) {
        validator.validateNodeAndAllAncestorsExists(snp);

        return listRoots(companyId).stream().map( n -> expandIfAncestor(companyId, n, snp)).collect(Collectors.toList());
    }

    private List<CatalogNodeEntity> findLatestChildNodesForCompany(Integer companyId, String parentSnp) {
        return nodeRepository.findByIdIn(
                SnpAndId.combine(
                        nodeRepository.listLatestIdsOfChildNodesForCompany(companyId, parentSnp),
                        nodeRepository.listLatestIdsOfChildNodesForCompanyNotDisabled(Constants.GLOBAL_CATALOG_COMPANY_ID, parentSnp)
                )
        );
    }

    private CatalogNodeDTO catalogNodeEntityToDTO(Integer companyId, CatalogNodeEntity nodeEntity) {
        var globalCatalogVariationIds = variationRepository.listLatestVariationsForCompanyAndGlobalCatalogNode(Constants.GLOBAL_CATALOG_COMPANY_ID, nodeEntity.getId());
        var companyCatalogVariationIds = variationRepository.listLatestVariationsForCompanyAndGlobalCatalogNode(companyId, nodeEntity.getId());

        return CatalogNodeDTO.from(
                nodeEntity,
                variationRepository.findAllById(VariationNumberAndId.combine(
                        companyCatalogVariationIds, globalCatalogVariationIds
                )),
                validator.isNodeLeaf(nodeEntity),
                globalCatalogVariationIds.size()
        );
    }

    private CatalogNodeDTO expandIfAncestor(Integer companyId, CatalogNodeDTO node, String snp) {
        if (SnpHelper.isAncestor(node.getSnp(), snp)) {
            return node.withChildren(
                    listChildren(companyId, node.getSnp()).stream().map( n -> expandIfAncestor(companyId, n, snp)).collect(Collectors.toList())
            );
        }
        else {
            return node;
        }
    }

    private String validateSnp(String snp) {
        if (! SnpHelper.isValidSnp(snp)) {
            throw CatalogExceptions.invalidSnp(snp);
        }
        String normalizedSnp = SnpHelper.normalize(snp);

        validator.validateNodeAndAllAncestorsExists(normalizedSnp);

        return normalizedSnp;
    }
}
