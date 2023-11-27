package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.dto.CatalogNodeDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogNodeRequest;
import com.siryus.swisscon.api.catalog.dto.CatalogVariationsRequest;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeRepository;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationRepository;
import com.siryus.swisscon.api.general.unit.UnitService;
import com.siryus.swisscon.api.util.validator.DTOValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
class GlobalCatalogUpdater {

    private final CatalogVariationRepository variationRepository;

    private final CatalogValidator validator;
    private final CatalogWriter writer;

    @Autowired
    public GlobalCatalogUpdater(
            UnitService unitService,

            CatalogNodeRepository nodeRepository,
            CatalogVariationRepository variationRepository
    ) {
        this.variationRepository = variationRepository;

        validator = new CatalogValidator(nodeRepository, variationRepository);
        writer = new CatalogWriter(nodeRepository, variationRepository, unitService);
    }

    @Transactional
    public CatalogNodeDTO addCatalogNode(CatalogNodeRequest request) {
        CatalogNodeRequest validRequest = validateNodeRequest(request);

        validator.validateNodeDoesNotExist(validRequest.getSnp());

        return CatalogNodeDTO.from(writer.materializeCatalogNode(Constants.GLOBAL_CATALOG_COMPANY_ID, validRequest.getSnp(), validRequest.getName()), true);
    }

    @Transactional
    public CatalogNodeDTO editCatalogNode(CatalogNodeRequest request) {
        CatalogNodeRequest validRequest = validateNodeRequest(request);

        CatalogNodeEntity existingNode = validator.validateNodeDoesExist(validRequest.getSnp());

        List<CatalogVariationEntity> variations = variationRepository.listLatestVariations(Constants.GLOBAL_CATALOG_COMPANY_ID, existingNode.getId());

        if(catalogNodeNeedsUpdate(existingNode, validRequest)) {
            CatalogNodeEntity newNode = writer.materializeCatalogNode(Constants.GLOBAL_CATALOG_COMPANY_ID, validRequest.getSnp(), validRequest.getName());

            List<CatalogVariationEntity> newVariations = writer.materializeVariations(newNode, variations);

            return CatalogNodeDTO.from(newNode, newVariations, validator.isNodeLeaf(existingNode) );
        }
        else {
            return CatalogNodeDTO.from(existingNode, variations, validator.isNodeLeaf(existingNode));
        }
    }

    @Transactional
    public CatalogNodeDTO archiveCatalogNode(String snp) {
        String validSnp = validator.validateSnp(snp);

        CatalogNodeEntity existingNode = validator.validateNodeDoesExist(validSnp);

        CatalogNodeEntity newNode = writer.materializeCatalogNode(
                existingNode.toBuilder().disabled(LocalDateTime.now()).build()
        );

        return CatalogNodeDTO.from(newNode, true);
    }

    @Transactional
    public CatalogNodeDTO addCatalogLeafWithVariations(CatalogVariationsRequest request) {
        CatalogVariationsRequest validRequest = validateVariationsRequest(request);

        validator.validateNodeDoesNotExist(validRequest.getSnp());

        CatalogNodeEntity newNode = writer.materializeCatalogNode(Constants.GLOBAL_CATALOG_COMPANY_ID, validRequest.getSnp(), validRequest.getSnp());
        List<CatalogVariationEntity> newVariations = writer.materializeVariations(
                newNode,
                writer.prepareVariations(newNode, validRequest.getVariations())
        );

        return CatalogNodeDTO.from(newNode, newVariations, true);
    }

    @Transactional
    public CatalogNodeDTO editCatalogVariations(CatalogVariationsRequest request) {
        CatalogVariationsRequest validRequest = validateVariationsRequest(request);

        CatalogNodeEntity existingNode = validator.validateNodeDoesExist(validRequest.getSnp());

        List<CatalogVariationEntity> variations = variationRepository.listLatestVariations(Constants.GLOBAL_CATALOG_COMPANY_ID, existingNode.getId());

        if (validator.catalogVariationsNeedUpdate(variations, validRequest)) {
            CatalogNodeEntity newNode = writer.materializeCatalogNode(Constants.GLOBAL_CATALOG_COMPANY_ID, existingNode.getSnp(), existingNode.getName());

            List<CatalogVariationEntity> newVariations = writer.materializeVariations(
                    newNode,
                    writer.prepareVariations(newNode, validRequest.getVariations())
            );

            return CatalogNodeDTO.from(newNode, newVariations, validator.isNodeLeaf(existingNode));
        }
        else {
            return CatalogNodeDTO.from(existingNode, variations, validator.isNodeLeaf(existingNode));
        }
    }

    private CatalogNodeRequest validateNodeRequest(CatalogNodeRequest request) {
        DTOValidator.validateAndThrow(request);

        return request.toBuilder().snp(validator.validateSnpAndAncestors(request.getSnp())).build();
    }

    private CatalogVariationsRequest validateVariationsRequest(CatalogVariationsRequest request) {
        DTOValidator.validateAndThrow(request);

        return request.toBuilder().snp(validator.validateSnpAndAncestors(request.getSnp())).build();
    }


    private boolean catalogNodeNeedsUpdate(CatalogNodeEntity node, CatalogNodeRequest request) {
        return ! Objects.equals(node.getName(),request.getName());
    }
}
