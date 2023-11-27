package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.dto.CatalogVariationDTO;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeRepository;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationRepository;
import com.siryus.swisscon.api.general.unit.UnitService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

class CatalogWriter {
    private final CatalogNodeRepository nodeRepository;
    private final CatalogVariationRepository variationRepository;
    private final UnitService unitService;

    CatalogWriter(
            CatalogNodeRepository nodeRepository,
            CatalogVariationRepository variationRepository,
            UnitService unitService
    ) {
        this.nodeRepository = nodeRepository;
        this.variationRepository = variationRepository;
        this.unitService = unitService;
    }

    CatalogNodeEntity materializeCatalogNode( CatalogNodeEntity newNode ) {
        return nodeRepository.save(newNode);
    }

    CatalogNodeEntity materializeCatalogNode(Integer companyId, String snp, String name) {
        return materializeCatalogNode(
                CatalogNodeEntity.builder()
                        .companyId(companyId)
                        .snp(snp)
                        .parentSnp(SnpHelper.parentSnp(snp))
                        .name(name)
                        .build()
        );
    }

    List<CatalogVariationEntity> prepareVariations(CatalogNodeEntity nodeEntity,  List<CatalogVariationDTO> variations) {
        return prepareVariations(nodeEntity, null, variations);
    }

    List<CatalogVariationEntity> prepareVariations(CatalogNodeEntity nodeEntity, Integer targetCompanyId, List<CatalogVariationDTO> variations) {
        return variations.stream()
                .map( d -> new CatalogVariationEntity(
                        targetCompanyId != null ? targetCompanyId : nodeEntity.getCompanyId(),
                        nodeEntity.getSnp(),
                        nodeEntity.getId(),
                        d.getVariationNumber(),
                        d.isActive(),
                        d.getDescription(),
                        d.getVariant(),
                        d.getCheckListItemsString(),
                        d.getUnit() == null ? null : unitService.findBySymbolName(d.getUnit().getSymbol()),
                        d.getPrice()
                ))
                .collect(Collectors.toList());
    }

    List<CatalogVariationEntity> materializeVariations(CatalogNodeEntity nodeEntity, List<CatalogVariationEntity> variationEntities) {
        return materializeVariations(Constants.GLOBAL_CATALOG_COMPANY_ID, nodeEntity, variationEntities);
    }

    List<CatalogVariationEntity> materializeVariations(Integer companyId, CatalogNodeEntity nodeEntity, List<CatalogVariationEntity> variationEntities) {
        return variationRepository.saveAll(
                variationEntities.stream()
                        .map( v -> v.toBuilder().companyId(companyId).catalogNodeId(nodeEntity.getId()).build())
                        .collect(Collectors.toList())
        );
    }

    List<CatalogVariationEntity> prepareVariationsWhichNeedToBeUpdated(CatalogNodeEntity globalCatalogNode, List<CatalogVariationEntity> existingVariations, List<CatalogVariationDTO> updatedVariations) {
        var existingVariationsMap = existingVariations.stream().collect(Collectors.toMap(CatalogVariationEntity::getVariationNumber, v -> v));

        var entitiesToUpdateMap = new HashMap<Integer, CatalogVariationEntity>();

        updatedVariations.forEach( (dto) -> {
            if( !existingVariationsMap.containsKey(dto.getVariationNumber()) || !existingVariationsMap.get(dto.getVariationNumber()).same(dto) ) {
                entitiesToUpdateMap.put(dto.getVariationNumber(), fromDto(globalCatalogNode, dto));
            }
        });

        return new ArrayList<>(entitiesToUpdateMap.values());
    }

    CatalogVariationEntity fromDto(CatalogNodeEntity globalCatalogNode, CatalogVariationDTO variation) {
        return CatalogVariationEntity.builder()
                .companyId(variation.getCompanyId())
                .active(variation.isActive())
                .snp(globalCatalogNode.getSnp())
                .catalogNodeId(globalCatalogNode.getId())
                .price(variation.getPrice())
                .unit(unitService.findBySymbolName(variation.getUnit().getSymbol()))
                .checkList(variation.getCheckListItemsString())
                .taskName(variation.getDescription())
                .taskVariation(variation.getVariant())
                .variationNumber(variation.getVariationNumber())
                .build();
    }
}
