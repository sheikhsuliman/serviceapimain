package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.dto.CatalogNodeDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogVariationDTO;
import com.siryus.swisscon.api.catalog.dto.CatalogVariationsRequest;
import com.siryus.swisscon.api.catalog.repos.CatalogVariationEntity;
import com.siryus.swisscon.api.exceptions.LocalizedResponseStatusException;
import com.siryus.swisscon.api.general.unit.UnitDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class CompanyCatalogUpdaterTest extends AbstractCatalogTestBase {
    private final Integer CATALOG_COMPANY_ID = 1;
    private final CompanyCatalogUpdater updater = new CompanyCatalogUpdater(unitService, catalogNodeRepository, variationRepository);

    @BeforeEach
    void doBeforeEach() {
        setupDefaultScenario();
        setupReposSaveAnswers();
    }

    @Test
    void Given_priceOrUnitNotDefined_When_updateCatalogLeafVariations_Then_throw() {
        assertThrows(LocalizedResponseStatusException.class, () -> {
            updater.updateCatalogLeafVariations(
                    CATALOG_COMPANY_ID,
                    CatalogVariationsRequest.builder()
                            .snp(TASK_1_SNP)
                            .variations(Arrays.asList(
                                    CatalogVariationDTO.builder()
                                            .variationNumber(1)
                                            .active(true)
                                            .description(VARIATION_TASK_NAME)
                                            .variant(VARIATION_1_VARIATION)
                                            .unit(UnitDTO.builder().id(1).name("m2").symbol("m2").build())
                                            .price(null)
                                            .build()
                            ))
                            .build()
            );
        });
        assertThrows(LocalizedResponseStatusException.class, () -> {
            updater.updateCatalogLeafVariations(
                    CATALOG_COMPANY_ID,
                    CatalogVariationsRequest.builder()
                            .snp(TASK_1_SNP)
                            .variations(Arrays.asList(
                                    CatalogVariationDTO.builder()
                                            .variationNumber(1)
                                            .active(true)
                                            .description(VARIATION_TASK_NAME)
                                            .variant(VARIATION_1_VARIATION)
                                            .unit(null)
                                            .price(BigDecimal.TEN)
                                            .build()
                            ))
                            .build()
            );
        });
    }

    @Test
    public void Given_changedCatalogVariation_When_restoreCatalogVariation_Then_success() {
        TestScenario scenario = defaultScenario();
        scenario.nodes(Constants.GLOBAL_CATALOG_COMPANY_ID, TASK_1_SNP).get(0)
                .variation(CATALOG_COMPANY_ID, 1, "OVERWRITTEN TASK NAME", "OVERWRITTEN VARIATION NAME", true, BigDecimal.ONE);
        setupScenario(scenario);
        when(variationRepository.saveAll(Mockito.any()))
                .then((Answer<List<CatalogVariationEntity>>) invocation -> invocation.getArgument(0));


        CatalogNodeDTO catalogNodeDTO = updater.restoreCatalogVariation(CATALOG_COMPANY_ID, TASK_1_SNP, 1);
        CatalogVariationDTO catalogVariationDTO = catalogNodeDTO.getVariations().stream().filter(v -> v.getVariationNumber().equals(1)).findFirst().orElseThrow();


        assertNull(catalogVariationDTO.getPrice());
        assertFalse(catalogVariationDTO.isActive());
        assertEquals(CATALOG_COMPANY_ID, catalogVariationDTO.getCompanyId());
        assertEquals(VARIATION_TASK_NAME, catalogVariationDTO.getDescription());
        assertEquals(VARIATION_1_VARIATION, catalogVariationDTO.getVariant());
        assertEquals(1, catalogVariationDTO.getVariationNumber());
    }


}
