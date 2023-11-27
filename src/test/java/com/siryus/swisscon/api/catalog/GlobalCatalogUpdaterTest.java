package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.dto.CatalogNodeDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogNodeRequest;
import com.siryus.swisscon.api.catalog.dto.CatalogVariationDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogVariationsRequest;
import com.siryus.swisscon.api.catalog.repos.CatalogNodeEntity;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import com.siryus.swisscon.api.general.unit.UnitDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GlobalCatalogUpdaterTest extends AbstractCatalogTestBase {

    private final GlobalCatalogUpdater updater = new GlobalCatalogUpdater(unitService, catalogNodeRepository, variationRepository);

    @BeforeEach
    void doBeforeEach() {
        setupDefaultScenario();
        setupReposSaveAnswers();
    }

    @Test
    void Given_tradeDoesNotExist_When_addTrade_Then_saveNewEntity() {
        CatalogNodeDTO tradeNode = updater.addCatalogNode(CatalogNodeRequest.builder()
                .snp(TRADE_3_SNP)
                .name(TRADE_3_NAME)
                .build());

        assertNotNull(tradeNode);
        assertEquals(TRADE_3_SNP, tradeNode.getSnp());
        assertNull(tradeNode.getParentSnp());

        ArgumentCaptor<CatalogNodeEntity> saveCaptor = ArgumentCaptor.forClass(CatalogNodeEntity.class);
        verify(catalogNodeRepository).save(saveCaptor.capture());

        CatalogNodeEntity savedEntity = saveCaptor.getValue();

        assertNotNull(savedEntity);
        assertEquals(TRADE_3_SNP, savedEntity.getSnp());
    }

    @Test
    void Given_tradeDoesNotExist_When_addGroup_Then_fail() {
        when(catalogNodeRepository.findLatestWithSnp(Constants.GLOBAL_CATALOG_COMPANY_ID, TRADE_1_SNP)).thenReturn(Optional.empty());

        LocalizedResponseStatusException exception = assertThrows(LocalizedResponseStatusException.class, () ->
            updater.addCatalogNode(CatalogNodeRequest.builder()
                .snp(GROUP_1_SNP)
                .name(GROUP_1_NAME)
            .build()));

        assertEquals(CatalogExceptions.ITEM_OR_ITS_ANCESTOR_DOES_NOT_EXIST.getErrorCode(), exception.getLocalizedReason().getErrorCode());
    }

    @Test
    void Given_tradeExists_When_addTrade_Then_fail() {
        LocalizedResponseStatusException exception = assertThrows(LocalizedResponseStatusException.class, () ->
            updater.addCatalogNode(CatalogNodeRequest.builder()
                .snp(TRADE_1_SNP)
                .name(TRADE_1_NAME)
            .build()));

        assertEquals(CatalogExceptions.CATALOG_NODE_ALREADY_EXISTS.getErrorCode(), exception.getLocalizedReason().getErrorCode());
    }

    @Test
    void Given_tradeExistAndNameChanged_When_editNode_Then_saveNewVersion() {
        CatalogNodeDTO tradeNode = updater.editCatalogNode(CatalogNodeRequest.builder()
                .snp(TRADE_1_SNP)
                .name(TRADE_1_NAME + DELTA_1)
                .build());

        assertEquals(TRADE_1_NAME + DELTA_1, tradeNode.getName());
        assertNotEquals(ROOT_NODES_LVL_1.get(0).getId(), tradeNode.getId());

        ArgumentCaptor<CatalogNodeEntity> saveCaptor = ArgumentCaptor.forClass(CatalogNodeEntity.class);
        verify(catalogNodeRepository).save(saveCaptor.capture());

        CatalogNodeEntity savedEntity = saveCaptor.getValue();

        assertNotNull(savedEntity);
        assertEquals(TRADE_1_NAME + DELTA_1, savedEntity.getName());

    }

    @Test
    void Given_tradeExistAndNameDidNotChanged_When_editNode_Then_doNotSaveNewVersion() {
        CatalogNodeDTO tradeNode = updater.editCatalogNode(CatalogNodeRequest.builder()
                .snp(TRADE_1_SNP)
                .name(TRADE_1_NAME)
                .build());

        assertEquals(ROOT_NODES_LVL_1.get(0).getId(), tradeNode.getId());

        ArgumentCaptor<CatalogNodeEntity> saveCaptor = ArgumentCaptor.forClass(CatalogNodeEntity.class);
        verify(catalogNodeRepository, times(0)).save(saveCaptor.capture());
    }

    @Test
    void Given_tradeExists_When_archiveNode_Then_saveNewVersion() {
        CatalogNodeDTO tradeNode = updater.archiveCatalogNode(TRADE_1_SNP);

        assertNotEquals(ROOT_NODES_LVL_1.get(0).getId(), tradeNode.getId());

        ArgumentCaptor<CatalogNodeEntity> saveCaptor = ArgumentCaptor.forClass(CatalogNodeEntity.class);
        verify(catalogNodeRepository).save(saveCaptor.capture());

        CatalogNodeEntity savedEntity = saveCaptor.getValue();

        assertNotNull(savedEntity);
        assertNotNull(savedEntity.getDisabled());
    }

    @Test
    void Given_leafNodeDoesNotExist_When_addCatalogLeafWithVariations_Then_createNewNodeWithVariations() {
        when(catalogNodeRepository.findLatestWithSnp(Constants.GLOBAL_CATALOG_COMPANY_ID, TASK_2_SNP)).thenReturn(Optional.empty());

        CatalogNodeDTO newlyAddedNode = updater.addCatalogLeafWithVariations(
                CatalogVariationsRequest.builder()
                        .snp(TASK_2_SNP)
                        .variations(Arrays.asList(
                                CatalogVariationDTO.builder()
                                        .variationNumber(1)
                                        .description(VARIATION_TASK_NAME)
                                        .variant(VARIATION_1_VARIATION)
                                    .build(),
                                CatalogVariationDTO.builder()
                                        .variationNumber(2)
                                        .description(VARIATION_TASK_NAME)
                                        .variant(VARIATION_2_VARIATION)
                                        .unit(UnitDTO.builder().id(1).name("m2").symbol("m2").build())
                                    .build()
                        ))
                    .build()
        );

        assertNotNull(newlyAddedNode);

        assertEquals(2, newlyAddedNode.getVariations().size());
        assertEquals(VARIATION_TASK_NAME, newlyAddedNode.getVariations().get(0).getDescription());
        assertEquals(VARIATION_TASK_NAME, newlyAddedNode.getVariations().get(1).getDescription());

        assertEquals(VARIATION_1_VARIATION, newlyAddedNode.getVariations().get(0).getVariant());
    }
}
