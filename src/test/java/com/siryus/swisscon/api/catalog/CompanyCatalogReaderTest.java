package com.siryus.swisscon.api.catalog;

import com.siryus.swisscon.api.catalog.dto.CatalogNodeDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompanyCatalogReaderTest extends AbstractCatalogTestBase {
    private static final Integer COMPANY_CATALOG_ID = 42;
    public static final String COMPANY_VARIATION_TASK_NAME = "Company " + VARIATION_TASK_NAME;
    public static final String COMPANY_VARIATION_1_VARIATION = "Company " + VARIATION_1_VARIATION;
    public static final String COMPANY_VARIATION_2_VARIATION = "Company " + VARIATION_2_VARIATION;
    public static final String COMPANY_VARIATION_3_VARIATION = "Company " + VARIATION_3_VARIATION;

    private final CompanyCatalogReader reader = new CompanyCatalogReader(catalogNodeRepository, variationRepository,
            new CatalogValidator(catalogNodeRepository, variationRepository));

    @Test
    void Given_noCompanyCatalogNodes_When_listRoots_Then_returnListOfGlobalRoots() {
        TestScenario scenario = setupScenario(defaultScenario());

        List<CatalogNodeDTO> roots = reader.listRoots(COMPANY_CATALOG_ID);

        assertNotNull(roots);
        assertEquals(2, roots.size());

        assertFalse(roots.get(0).isLeaf());
        assertEquals(TRADE_1_NAME, roots.get(0).getName());
        assertEquals(TRADE_1_SNP, roots.get(0).getSnp());

        assertTrue(roots.get(1).isLeaf());
        assertEquals(TRADE_2_NAME, roots.get(1).getName());
        assertEquals(TRADE_2_SNP, roots.get(1).getSnp());
    }

    @Test
    void Given_companyCatalogDisablesRoot_When_listRoots_Then_returnThisRootAsDisabled() {
        TestScenario scenario = defaultScenario();
        scenario.root(
                COMPANY_CATALOG_ID, TRADE_2_SNP, TRADE_2_NAME, true
        );

        setupScenario(scenario);

        List<CatalogNodeDTO> roots = reader.listRoots(COMPANY_CATALOG_ID);

        assertNotNull(roots);
        assertEquals(2, roots.size());

        assertEquals(TRADE_1_SNP, roots.get(0).getSnp());
        assertNull(roots.get(0).getDisabled());

        assertEquals(TRADE_2_SNP, roots.get(1).getSnp());
        assertNotNull(roots.get(1).getDisabled());
    }

    @Test
    void Given_companyCatalogRestoreRoot_When_listRoots_Then_returnThisRootAsNotDisabled() {
        TestScenario scenario = defaultScenario();
        scenario.root(
                COMPANY_CATALOG_ID, TRADE_2_SNP, TRADE_2_NAME, true
        );
        scenario.root(
                COMPANY_CATALOG_ID, TRADE_2_SNP, TRADE_2_NAME, false
        );

        setupScenario(scenario);

        List<CatalogNodeDTO> roots = reader.listRoots(COMPANY_CATALOG_ID);

        assertNotNull(roots);
        assertEquals(2, roots.size());

        assertEquals(TRADE_1_SNP, roots.get(0).getSnp());
        assertNull(roots.get(0).getDisabled());

        assertEquals(TRADE_2_SNP, roots.get(1).getSnp());
        assertNull(roots.get(1).getDisabled());
    }

    @Test
    void Given_noCompanyVariations_When_listChildrenAtLeafLevel_Then_returnGlobalCatalogVariations() {
        TestScenario scenario = setupScenario(defaultScenario());

        List<CatalogNodeDTO> leafs = reader.listChildren(COMPANY_CATALOG_ID, MAIN_TASK_1_SNP);

        assertNotNull(leafs);
        assertEquals(1, leafs.size());

        assertNotNull(leafs.get(0).getVariations());
        assertEquals(2, leafs.get(0).getVariations().size());
        assertEquals(Constants.GLOBAL_CATALOG_COMPANY_ID, leafs.get(0).getVariations().get(0).getCompanyId());
    }

    @Test
    void Given_someCompanyVariations_When_listChildrenAtLeafLevel_Then_returnMixOfCompanyAndGlobalVariaitons() {
        TestScenario scenario = defaultScenario();

        List<TestScenario.TestNode> nodes = scenario.nodes(Constants.GLOBAL_CATALOG_COMPANY_ID, TASK_1_SNP);
        nodes.get(0)
                .variation(COMPANY_CATALOG_ID, 1, COMPANY_VARIATION_TASK_NAME, COMPANY_VARIATION_1_VARIATION)
                .variation(COMPANY_CATALOG_ID, 3, COMPANY_VARIATION_TASK_NAME, COMPANY_VARIATION_3_VARIATION);

        setupScenario(scenario);

        List<CatalogNodeDTO> leafs = reader.listChildren(COMPANY_CATALOG_ID, MAIN_TASK_1_SNP);

        assertNotNull(leafs);
        assertNotNull(leafs.get(0).getVariations());
        assertEquals(2, leafs.get(0).getVariations().size());
        assertEquals(COMPANY_CATALOG_ID, leafs.get(0).getVariations().get(0).getCompanyId());
        assertEquals(COMPANY_VARIATION_TASK_NAME, leafs.get(0).getVariations().get(0).getDescription());
        assertEquals(Constants.GLOBAL_CATALOG_COMPANY_ID, leafs.get(0).getVariations().get(1).getCompanyId());
    }

    @Test
    void Given_globalCatalogLeafNodeWasUpdated_When_listChildrenAtThisNodeParent_Then_returnGlobalCatalog() {
        TestScenario scenario = defaultScenario();

        List<TestScenario.TestNode> nodes = scenario.nodes(Constants.GLOBAL_CATALOG_COMPANY_ID, TASK_1_SNP);
        nodes
            .get(0)
                .variation(COMPANY_CATALOG_ID, 1, COMPANY_VARIATION_TASK_NAME, COMPANY_VARIATION_1_VARIATION)
            .sibling(TASK_1_SNP, TASK_1_NAME)
                .variation(1,  VARIATION_TASK_NAME, VARIATION_1_VARIATION)
                .variation(2,  VARIATION_TASK_NAME, VARIATION_2_VARIATION)
                .variation(3,  VARIATION_TASK_NAME, VARIATION_3_VARIATION)
        ;

        setupScenario(scenario);

        List<CatalogNodeDTO> leafs = reader.listChildren(COMPANY_CATALOG_ID, MAIN_TASK_1_SNP);
        assertNotNull(leafs);
        assertNotNull(leafs.get(0).getVariations());
        assertEquals(3, leafs.get(0).getVariations().size());

        assertEquals(VARIATION_1_VARIATION, leafs.get(0).getVariations().get(0).getVariant());
        assertEquals(VARIATION_2_VARIATION, leafs.get(0).getVariations().get(1).getVariant());
        assertEquals(VARIATION_3_VARIATION, leafs.get(0).getVariations().get(2).getVariant());

    }
}
