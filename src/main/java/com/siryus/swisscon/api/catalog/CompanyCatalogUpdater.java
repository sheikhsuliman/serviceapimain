package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.dto.CatalogNodeDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogVariationDTO;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
class CompanyCatalogUpdater {
    private final CatalogNodeRepository nodeRepository;
    private final CatalogVariationRepository variationRepository;

    private final CatalogValidator validator;
    private final CatalogWriter writer;

    @Autowired
    public CompanyCatalogUpdater(
            UnitService unitService,

            CatalogNodeRepository nodeRepository,
            CatalogVariationRepository variationRepository
    ) {
        this.nodeRepository = nodeRepository;
        this.variationRepository = variationRepository;

        validator = new CatalogValidator(nodeRepository, variationRepository);
        writer = new CatalogWriter(nodeRepository, variationRepository, unitService);
    }

    @Transactional
    public CatalogNodeDTO archiveCatalogNode(Integer companyId, String snp) {
        validateNodeIsNotDisabled(companyId, snp);

        CatalogNodeEntity globalCatalogNode = validator.validateNodeDoesExist(snp);

        CatalogNodeEntity companyCatalogDisabledNode = nodeRepository.save(globalCatalogNode.toBuilder()
            .id(null)
            .companyId(companyId)
            .disabled(LocalDateTime.now())
            .build()
        );

        return CatalogNodeDTO.from(companyCatalogDisabledNode, true);
    }

    @Transactional
    public CatalogNodeDTO restoreCatalogNode(Integer companyId, String snp) {
        validateNodeIsDisabled(companyId, snp);

        CatalogNodeEntity globalCatalogNode = validator.validateNodeDoesExist(snp);

        CatalogNodeEntity companyCatalogRestoredNode = nodeRepository.save(globalCatalogNode.toBuilder()
            .id(null)
            .companyId(companyId)
            .build()
        );

        return CatalogNodeDTO.from(companyCatalogRestoredNode, validator.isNodeLeaf(companyCatalogRestoredNode));
    }

    @Transactional
    public CatalogNodeDTO restoreCatalogVariation(Integer companyId, String snp, Integer variationNumber) {
        CatalogNodeEntity globalNode = validator.validateNodeDoesExist(snp);

        CatalogVariationEntity companyVariationEntity = validator.validateVariationDoesExist(
                companyId, snp, globalNode.getId(), variationNumber);

        CatalogVariationEntity globalVariationEntity = validator.validateVariationDoesExist(
                Constants.GLOBAL_CATALOG_COMPANY_ID, snp, globalNode.getId(),  companyVariationEntity.getVariationNumber());

        CatalogVariationEntity restoredCatalogVariationEntity = resetVariationValues(companyId, globalVariationEntity);

        List<CatalogVariationEntity> newVariations = writer.materializeVariations(companyId, globalNode,
                Collections.singletonList(restoredCatalogVariationEntity));

        return CatalogNodeDTO.from(
                globalNode,
                combine(newVariations, getAllLatestExistingVariations(companyId, globalNode.getId())),
                validator.isNodeLeaf(globalNode),
                countGlobalVariations(globalNode.getId()));
    }

    private CatalogVariationEntity resetVariationValues(Integer companyId, CatalogVariationEntity globalVariationEntity) {
        return globalVariationEntity
                .toBuilder()
                .id(null)
                .active(false)
                .companyId(companyId)
                .build();
    }

    @Transactional
    public CatalogNodeDTO updateCatalogLeafVariations(Integer companyId, CatalogVariationsRequest request) {
        CatalogVariationsRequest validRequest = validateVariationsRequest(request);

        CatalogNodeEntity globalNode = validator.validateGlobalNodeDoesExists(companyId, validRequest.getSnp());

        List<CatalogVariationEntity> allExistingVariations = getAllLatestExistingVariations(companyId, globalNode.getId());

        List<CatalogVariationEntity> variationsToUpdate = writer.prepareVariationsWhichNeedToBeUpdated(
            globalNode,
            allExistingVariations,
            request.getVariations()
        );

        if (! variationsToUpdate.isEmpty()) {
            List<CatalogVariationEntity> newVariations = writer.materializeVariations(companyId, globalNode, variationsToUpdate);

            return CatalogNodeDTO.from(
                    globalNode,
                    combine(newVariations, allExistingVariations),
                    validator.isNodeLeaf(globalNode),
                    countGlobalVariations(globalNode.getId())
            );
        }
        else {
            return CatalogNodeDTO.from(globalNode, allExistingVariations, validator.isNodeLeaf(globalNode), countGlobalVariations(globalNode.getId()));
        }
    }

    private int countGlobalVariations(Integer globalNodeId) {
        return variationRepository.countLatestVariations(Constants.GLOBAL_CATALOG_COMPANY_ID, globalNodeId);
    }

    private List<CatalogVariationEntity> getAllLatestExistingVariations(
            Integer companyId,
            Integer globalNodeId
    ) {
        List<CatalogVariationEntity> allExistingGlobalVariations =
                variationRepository.listLatestVariations(Constants.GLOBAL_CATALOG_COMPANY_ID, globalNodeId);
        List<CatalogVariationEntity> allExistingCompanyVariations =
                variationRepository.listLatestVariations(companyId, globalNodeId);

        return combine(allExistingCompanyVariations, allExistingGlobalVariations);
    }

    private List<CatalogVariationEntity> combine(
            List<CatalogVariationEntity> updatedVariations,
            List<CatalogVariationEntity> allExistingVariations
    ) {
        var existingVariationsMap = allExistingVariations.stream().collect(Collectors.toMap(CatalogVariationEntity::getVariationNumber, v -> v));
        var updatedVariationsMap = updatedVariations.stream().collect(Collectors.toMap(CatalogVariationEntity::getVariationNumber, v -> v));

        var combinedMap = new HashMap<>(existingVariationsMap);
        combinedMap.putAll(updatedVariationsMap);

        return combinedMap.values().stream()
                .sorted(Comparator.comparingInt(CatalogVariationEntity::getVariationNumber))
                .collect(Collectors.toList());
    }

    private void validateNodeIsNotDisabled(Integer companyId, String snp) {
        CatalogNodeEntity companyCatalogNode = nodeRepository.findLatestWithSnp(companyId, snp).orElse(null);

        if (companyCatalogNode != null && companyCatalogNode.getDisabled() != null) {
            throw CatalogExceptions.catalogNodeAlreadyDisabled(companyId, snp);
        }
    }

    private void validateNodeIsDisabled(Integer companyId, String snp) {
        CatalogNodeEntity companyCatalogNode = nodeRepository.findLatestWithSnp(companyId, snp).orElse(null);

        if (companyCatalogNode == null || companyCatalogNode.getDisabled() == null) {
            throw CatalogExceptions.catalogNodeAlreadyEnabled(companyId, snp);
        }
    }

    private CatalogVariationsRequest validateVariationsRequest(CatalogVariationsRequest request) {
        DTOValidator.validateAndThrow(request);

        request.getVariations().forEach(this::validateCompanyVariation);

        return request.toBuilder().snp(validator.validateSnpAndAncestors(request.getSnp())).build();
    }

    private void validateCompanyVariation(CatalogVariationDTO variation) {
        if (! variation.isActive()) {
            return;
        }

        if (variation.getUnit() == null) {
            throw CatalogExceptions.companyVariationHasToHaveUnit(variation.getDescription(), variation.getVariant());
        }
        if (variation.getPrice() == null) {
            throw CatalogExceptions.companyVariationHasToHavePrice(variation.getDescription(), variation.getVariant());
        }
    }
}
