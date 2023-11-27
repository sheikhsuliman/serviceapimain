package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.dto.CatalogVariationDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogVariationsRequest;
import com.siryus.swisscon.api.catalog.dto.SnpAndId;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeRepository;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationRepository;
import com.siryus.swisscon.api.general.unit.Unit;
import com.siryus.swisscon.api.general.unit.UnitDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
class CatalogValidator {

    private final CatalogNodeRepository nodeRepository;
    private final CatalogVariationRepository variationRepository;

    @Autowired
    CatalogValidator(CatalogNodeRepository nodeRepository, CatalogVariationRepository variationRepository) {
        this.nodeRepository = nodeRepository;
        this.variationRepository = variationRepository;
    }

    void validateNodeAndAllAncestorsExists(Integer companyId, String snp) {
        if (snp != null) {
            CatalogNodeEntity itemWithId = nodeRepository.findLatestWithSnp(companyId, snp)
                    .orElse(nodeRepository.findLatestWithSnp(Constants.GLOBAL_CATALOG_COMPANY_ID, snp).orElse(null));

            if (itemWithId == null || itemWithId.getDisabled() != null) {
                throw CatalogExceptions.itemOrItsAncestorDoesNotExist(snp);
            }

            validateNodeAndAllAncestorsExists(itemWithId.getParentSnp());
        }
    }

    void validateNodeAndAllAncestorsExists(String snp) {
        if (snp != null) {
            CatalogNodeEntity itemWithId = nodeRepository.findLatestWithSnp(Constants.GLOBAL_CATALOG_COMPANY_ID, snp).orElse(null);

            if (itemWithId == null || itemWithId.getDisabled() != null) {
                throw CatalogExceptions.itemOrItsAncestorDoesNotExist(snp);
            }

            validateNodeAndAllAncestorsExists(itemWithId.getParentSnp());
        }
    }
    void validateAllAncestorsExists(String snp) {
        if (snp != null) {
            validateNodeAndAllAncestorsExists(SnpHelper.parentSnp(snp));
        }
        else {
            throw CatalogExceptions.itemOrItsAncestorDoesNotExist(null);
        }
    }

    String validateSnpAndAncestors(String snp) {
        if (! SnpHelper.isValidSnp(snp)) {
            throw CatalogExceptions.invalidSnp(snp);
        }
        String normalizedSnp = SnpHelper.normalize(snp);

        validateAllAncestorsExists(normalizedSnp);

        return normalizedSnp;
    }

    void validateNodeDoesNotExist(String snp) {
        validateNodeDoesNotExist(Constants.GLOBAL_CATALOG_COMPANY_ID, snp);
    }

    void validateNodeDoesNotExist(Integer companyId, String snp) {
        var existingNode = nodeRepository.findLatestWithSnp(companyId, snp).orElse(null);
        if (existingNode != null &&  existingNode.getDisabled() == null) {
            throw CatalogExceptions.catalogNodeAlreadyExists(snp);
        }
    }

    CatalogVariationEntity validateVariationDoesExist(Integer companyId, String snp, Integer globalNodeId, Integer variationNumber) {
        return variationRepository.findVariation(companyId, globalNodeId, variationNumber)
                .orElseThrow(() -> CatalogExceptions.companyVariationDoesNotExist(snp, variationNumber));
    }

    CatalogNodeEntity validateNodeDoesExist(String snp) {
        return validateNodeDoesExist(Constants.GLOBAL_CATALOG_COMPANY_ID, snp);
    }


    CatalogNodeEntity validateNodeDoesExist(Integer companyId, String snp) {
        var existingNode = nodeRepository.findLatestWithSnp(companyId, snp).orElse(null);
        if (existingNode == null ||  existingNode.getDisabled() != null) {
            throw CatalogExceptions.catalogNodeDoesNotExist(snp);
        }
        return existingNode;
    }

    CatalogNodeEntity validateGlobalNodeDoesExists(Integer companyId, String snp) {
        var companyNode = nodeRepository.findLatestWithSnp(companyId, snp).orElse(null);

        if (companyNode != null && companyNode.getDisabled() != null) {
            throw CatalogExceptions.catalogNodeDoesNotExist(snp);
        }

        return validateNodeDoesExist(Constants.GLOBAL_CATALOG_COMPANY_ID, snp);
    }

    String validateSnp(String snp) {
        if (!SnpHelper.isValidSnp(snp)) {
            throw CatalogExceptions.invalidSnp(snp);
        }
        return SnpHelper.normalize(snp);
    }

    boolean isNodeLeaf(CatalogNodeEntity node) {
        return node.getDisabled() != null || nodeRepository.findByIdInAndDisabledIsNull(
                nodeRepository.listLatestIdsOfChildNodesForCompany(Constants.GLOBAL_CATALOG_COMPANY_ID, node.getSnp())
                    .stream().map(SnpAndId::getId).collect(Collectors.toList())
        ).isEmpty();
    }

    boolean catalogVariationsNeedUpdate(List<CatalogVariationEntity> variations, CatalogVariationsRequest request) {
        if (variations.size() != request.getVariations().size()) {
            return true;
        }

        CatalogVariationEntity[] orderedEntities = variations.toArray(new CatalogVariationEntity[0]);
        Arrays.sort(orderedEntities, Comparator.comparingInt(CatalogVariationEntity::getVariationNumber));

        CatalogVariationDTO[] orderedDTOs = request.getVariations().toArray(new CatalogVariationDTO[0]);
        Arrays.sort(orderedDTOs, Comparator.comparingInt(CatalogVariationDTO::getVariationNumber));

        for( int i = 0; i < orderedEntities.length; i++) {
            if (catalogVariationNeedsUpdate(orderedEntities[i], orderedDTOs[i])) {
                return true;
            }
        }
        return false;
    }

    boolean catalogVariationNeedsUpdate(CatalogVariationEntity entity, CatalogVariationDTO catalogVariationDTO) {
        return ! (
                entity.getTaskName().equals(catalogVariationDTO.getDescription()) &&
                        Objects.equals(entity.getTaskVariation(), catalogVariationDTO.getVariant()) &&
                        Objects.equals(
                                Optional.ofNullable(entity.getUnit()).map(Unit::getId).orElse(null),
                                Optional.ofNullable(catalogVariationDTO.getUnit()).map(UnitDTO::getId).orElse(null)
                        ) &&
                        Objects.equals(entity.getCheckList(), catalogVariationDTO.getCheckListItemsString()) &&
                        Objects.equals(entity.getPrice(), catalogVariationDTO.getPrice()) &&
                        Objects.equals(entity.isActive(), catalogVariationDTO.isActive())
        );
    }

}
