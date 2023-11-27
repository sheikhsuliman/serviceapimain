package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.dto.CatalogNodeDTO;
import com.siryus.swisscon.api.catalog.dto.SnpAndId;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationRepository;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GlobalCatalogReader {

    private final CatalogNodeRepository nodeRepository;
    private final CatalogVariationRepository variationRepository;
    private final CatalogValidator validator;

    @Autowired
    public GlobalCatalogReader(
            CatalogNodeRepository nodeRepository,
            CatalogVariationRepository variationRepository,
            CatalogValidator validator) {
        this.nodeRepository = nodeRepository;
        this.variationRepository = variationRepository;
        this.validator = validator;
    }

    public List<CatalogNodeDTO> listRoots() {
        return nodeRepository.findByIdInAndDisabledIsNull(
                nodeRepository.listLatestIdsOfRootNodesForCompany(Constants.GLOBAL_CATALOG_COMPANY_ID)
                    .stream().map(SnpAndId::getId).collect(Collectors.toList())
        )
            .stream()
                .map( n -> CatalogNodeDTO.from(n,validator.isNodeLeaf(n)))
                .collect(Collectors.toList());
    }

    public List<CatalogNodeDTO> listChildren(String parentSnp) {
        String validParentSnp = validateSnp(parentSnp);

        return nodeRepository.findByIdInAndDisabledIsNull(
                nodeRepository.listLatestIdsOfChildNodesForCompany(Constants.GLOBAL_CATALOG_COMPANY_ID, validParentSnp)
                .stream().map(SnpAndId::getId).collect(Collectors.toList())
        )
            .stream()
                .map(
                    n -> CatalogNodeDTO.from(
                        n,
                        variationRepository.listLatestVariations(Constants.GLOBAL_CATALOG_COMPANY_ID, n.getId()),
                        validator.isNodeLeaf(n)
                    )
                )
                .collect(Collectors.toList());
    }

    public List<CatalogNodeDTO> expand(String snp) {
        validator.validateNodeAndAllAncestorsExists(snp);
        
        return listRoots().stream().map( n -> expandIfAncestor(n, snp)).collect(Collectors.toList());
    }

    private CatalogNodeDTO expandIfAncestor(CatalogNodeDTO node, String snp) {
        if (SnpHelper.isAncestor(node.getSnp(), snp)) {
            return node.withChildren(
                 listChildren(node.getSnp()).stream().map( n -> expandIfAncestor(n, snp)).collect(Collectors.toList())
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
